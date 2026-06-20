package com.aicode.report.service;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.service.AiAnalysisService;
import com.aicode.report.dto.ReportProgress;
import com.aicode.report.model.ReviewReport;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报告任务跟踪器 — 异步生成报告并跟踪进度
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportTaskTracker {

    private final RuleEngine ruleEngine;
    private final AiAnalysisService aiAnalysisService;
    private final ReportService reportService;

    /** 进度条目 + 最后更新时间 */
    private final Map<String, ReportProgress> progressMap = new ConcurrentHashMap<>();
    private final Map<String, Instant> updateTimeMap = new ConcurrentHashMap<>();

    /** projectId → 最新 taskId */
    private final Map<Long, String> projectTaskMap = new ConcurrentHashMap<>();

    /** 已完成的任务保留 10 分钟后自动清理 */
    private static final long TTL_MINUTES = 10;

    /**
     * 提交异步报告生成任务，返回 taskId
     */
    public String submit(Long projectId) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        progressMap.put(taskId, ReportProgress.pending(taskId));
        updateTimeMap.put(taskId, Instant.now());
        projectTaskMap.put(projectId, taskId);

        CompletableFuture.runAsync(() -> {
            try {
                String sourcePath = "./git-repos/" + projectId;

                updateProgress(taskId, "正在执行规则检测...", 10);
                List<RuleResult> ruleResults = ruleEngine.analyze(projectId, sourcePath);

                List<RuleResult> failedResults = ruleResults.stream()
                        .filter(r -> !r.isPassed()).toList();
                int total = failedResults.size();

                if (total > 0) {
                    updateProgress(taskId, "正在 AI 分析 (" + total + " 个问题)...", 20);

                    List<AiIssueAnalysis> analyses = new java.util.ArrayList<>();
                    int done = 0;
                    for (RuleResult r : failedResults) {
                        AiIssueAnalysis a = aiAnalysisService.analyze(r, projectId);
                        analyses.add(a);
                        done++;
                        int pct = 20 + (done * 60 / total);
                        updateProgress(taskId,
                                "AI 分析中: " + done + "/" + total, pct);
                    }

                    updateProgress(taskId, "正在生成报告...", 85);
                    ReviewReport report = reportService.generateReport(
                            "project-" + projectId, ruleResults, analyses);

                    progressMap.put(taskId, ReportProgress.success(taskId, report));
                } else {
                    updateProgress(taskId, "正在生成报告...", 85);
                    ReviewReport report = reportService.generateReport(
                            "project-" + projectId, ruleResults, List.of());
                    progressMap.put(taskId, ReportProgress.success(taskId, report));
                }
                updateTimeMap.put(taskId, Instant.now());

                log.info("报告生成完成: taskId={}, projectId={}", taskId, projectId);

            } catch (Exception e) {
                log.error("报告生成失败: taskId={}, projectId={}", taskId, projectId, e);
                progressMap.put(taskId, ReportProgress.failed(taskId, e.getMessage()));
                updateTimeMap.put(taskId, Instant.now());
            }
        });

        return taskId;
    }

    /**
     * 查询进度
     */
    public ReportProgress getProgress(String taskId) {
        return progressMap.getOrDefault(taskId,
                ReportProgress.builder().taskId(taskId).status("NOT_FOUND").build());
    }

    /**
     * 按 projectId 查询最新任务的进度
     */
    public ReportProgress getProgressByProjectId(Long projectId) {
        String taskId = projectTaskMap.get(projectId);
        if (taskId == null) {
            return ReportProgress.builder().taskId("").status("NOT_FOUND").build();
        }
        return getProgress(taskId);
    }

    /**
     * 每 5 分钟清理超过 10 分钟的已完成/失败任务
     */
    @Scheduled(fixedRate = 300_000)
    public void cleanExpired() {
        Instant cutoff = Instant.now().minusSeconds(TTL_MINUTES * 60);
        int before = progressMap.size();

        updateTimeMap.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoff)) {
                progressMap.remove(entry.getKey());
                return true;
            }
            return false;
        });

        int removed = before - progressMap.size();
        if (removed > 0) {
            log.info("清理过期任务: 移除 {} 个, 剩余 {}", removed, progressMap.size());
        }
    }

    private void updateProgress(String taskId, String stage, int percent) {
        progressMap.put(taskId, ReportProgress.running(taskId, stage, percent));
        updateTimeMap.put(taskId, Instant.now());
    }
}
