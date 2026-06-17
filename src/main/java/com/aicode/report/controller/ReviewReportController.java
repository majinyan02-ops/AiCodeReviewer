package com.aicode.report.controller;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.service.AiAnalysisService;
import com.aicode.common.Result;
import com.aicode.report.model.ReportResponse;
import com.aicode.report.model.ReviewReport;
import com.aicode.report.service.ReportService;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码审查报告控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReviewReportController {

    private final RuleEngine ruleEngine;
    private final AiAnalysisService aiAnalysisService;
    private final ReportService reportService;

    /**
     * 生成代码审查报告
     *
     * @param projectId 项目 ID
     * @return 审查报告
     */
    @PostMapping("/generate/{projectId}")
    public Result<ReportResponse> generate(@PathVariable Long projectId) {
        log.info("生成审查报告: projectId={}", projectId);

        String sourcePath = "./git-repos/" + projectId;

        // 1. 执行规则检测
        List<RuleResult> ruleResults = ruleEngine.analyze(projectId, sourcePath);

        // 2. AI 批量分析（仅分析违规项）
        List<RuleResult> failedResults = ruleResults.stream()
                .filter(r -> !r.isPassed())
                .toList();
        List<AiIssueAnalysis> analyses = aiAnalysisService.analyzeBatch(failedResults);

        // 3. 生成报告
        String projectName = "project-" + projectId;
        ReviewReport report = reportService.generateReport(projectName, ruleResults, analyses);

        ReportResponse response = ReportResponse.builder()
                .report(report)
                .aiAnalyses(analyses)
                .build();

        return Result.success(response);
    }
}
