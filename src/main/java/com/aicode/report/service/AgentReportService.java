package com.aicode.report.service;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.report.generator.MarkdownReportGenerator;
import com.aicode.report.generator.PdfReportGenerator;
import com.aicode.report.model.IssueSummary;
import com.aicode.report.model.ReviewReport;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 报告服务 — 从 Agent 结果生成 Markdown/PDF 报告
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentReportService {

    private final MarkdownReportGenerator markdownGenerator;
    private final PdfReportGenerator pdfGenerator;

    public ReviewReport buildReport(SummaryAgentResult summaryResult, String projectName) {
        ReviewAgentResult review = summaryResult != null ? summaryResult.getReviewResult() : null;

        int totalRules = review != null ? review.getTotalRules() : 0;
        int errorCount = review != null ? review.getErrorCount() : 0;
        int warningCount = review != null ? review.getWarningCount() : 0;
        int infoCount = review != null ? review.getInfoCount() : 0;
        int failedRules = errorCount + warningCount + infoCount;

        List<IssueSummary> issues = new ArrayList<>();
        if (review != null && review.getRuleResults() != null) {
            for (RuleResult r : review.getRuleResults()) {
                if (r.isPassed()) continue;
                issues.add(IssueSummary.builder()
                        .ruleId(r.getRuleId())
                        .ruleName(r.getRuleName())
                        .severity(r.getSeverity())
                        .className(r.getClassName())
                        .methodName(r.getMethodName())
                        .filePath(r.getFilePath())
                        .lineNumber(r.getLineNumber())
                        .reason(r.getMessage())
                        .impact("")
                        .suggestion(r.getSuggestion())
                        .build());
            }
        }

        String overallSummary = summaryResult != null && summaryResult.getHealthReport() != null
                ? summaryResult.getHealthReport().getSummary()
                : "代码审查完成";

        return ReviewReport.builder()
                .projectName(projectName)
                .scanTime(LocalDateTime.now())
                .totalRules(totalRules)
                .passedRules(totalRules - failedRules)
                .failedRules(failedRules)
                .errorCount(errorCount)
                .warningCount(warningCount)
                .infoCount(infoCount)
                .issues(issues)
                .overallSummary(overallSummary)
                .build();
    }

    public String generateMarkdown(SummaryAgentResult summaryResult, String projectName) {
        return markdownGenerator.generate(buildReport(summaryResult, projectName));
    }

    public byte[] generatePdf(SummaryAgentResult summaryResult, String projectName) {
        return pdfGenerator.generate(buildReport(summaryResult, projectName));
    }
}
