package com.aicode.agent.summary.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.common.Result;
import com.aicode.report.service.AgentReportService;
import com.aicode.service.AnalysisRecordService;
import com.aicode.service.ProjectService;
import com.aicode.vo.ProjectVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * SummaryAgent 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/summary")
@RequiredArgsConstructor
public class SummaryAgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final AnalysisCacheService cacheService;
    private final ObjectMapper objectMapper;
    private final AnalysisRecordService analysisRecordService;
    private final AgentReportService agentReportService;
    private final ProjectService projectService;

    @Value("${git.storage.path:./git-repos}")
    private String gitStoragePath;

    @PostMapping
    public Result<AgentResult> summary(@RequestBody Map<String, String> request) {
        String projectId = request.get("projectId");

        // 从缓存获取 ReviewAgentResult + FixAgentResult
        Map<String, Object> attributes = new HashMap<>();
        ReviewAgentResult reviewResult = convertTo(cacheService.getReviewResult(projectId), ReviewAgentResult.class);
        if (reviewResult != null) {
            attributes.put("reviewResult", reviewResult);
        }
        FixAgentResult fixResult = convertTo(cacheService.getFixResult(projectId), FixAgentResult.class);
        if (fixResult != null) {
            attributes.put("fixResult", fixResult);
        }

        AgentContext context = AgentContext.builder()
                .projectId(projectId)
                .attributes(attributes)
                .build();
        AgentResult result = agentOrchestrator.executeAgent(AgentType.SUMMARY, context);

        // 持久化分析记录
        persistRecord(projectId, reviewResult, fixResult, result);

        // 自动生成并保存报告文件
        saveReports(projectId, result);

        return Result.success(result);
    }

    private void persistRecord(String projectId, ReviewAgentResult reviewResult,
                               FixAgentResult fixResult, AgentResult summaryResult) {
        try {
            SummaryAgentResult summary = null;
            if (summaryResult.isSuccess() && summaryResult.getPayload() instanceof SummaryAgentResult s) {
                summary = s;
            }

            analysisRecordService.saveRecord(
                    Long.parseLong(projectId), null, null,
                    reviewResult, fixResult, summary,
                    summaryResult.getDuration(),
                    summaryResult.isSuccess() ? "SUCCESS" : "FAILED",
                    summaryResult.isSuccess() ? null : summaryResult.getMessage()
            );
            log.info("分析记录已持久化: projectId={}", projectId);
        } catch (Exception e) {
            log.error("持久化分析记录失败: projectId={}", projectId, e);
        }
    }

    private void saveReports(String projectId, AgentResult summaryResult) {
        if (!summaryResult.isSuccess()) return;
        if (!(summaryResult.getPayload() instanceof SummaryAgentResult summary)) return;

        try {
            // 获取项目名称
            String projectName = "project-" + projectId;
            try {
                ProjectVO project = projectService.getById(Long.parseLong(projectId));
                if (project != null) projectName = project.getName();
            } catch (Exception ignored) {}

            // 确保报告目录存在
            Path reportDir = Path.of(gitStoragePath, projectId, "reports");
            Files.createDirectories(reportDir);

            // 生成 Markdown 报告
            String markdown = agentReportService.generateMarkdown(summary, projectName);
            Path mdFile = reportDir.resolve("report.md");
            Files.writeString(mdFile, markdown);
            log.info("Markdown 报告已保存: {}", mdFile);

            // 生成 PDF 报告
            byte[] pdf = agentReportService.generatePdf(summary, projectName);
            Path pdfFile = reportDir.resolve("report.pdf");
            Files.write(pdfFile, pdf);
            log.info("PDF 报告已保存: {}", pdfFile);

        } catch (IOException e) {
            log.error("保存报告文件失败: projectId={}", projectId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertTo(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        if (clazz.isInstance(obj)) return clazz.cast(obj);
        return objectMapper.convertValue(obj, clazz);
    }
}
