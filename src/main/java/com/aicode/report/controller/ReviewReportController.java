package com.aicode.report.controller;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.service.AiAnalysisService;
import com.aicode.common.Result;
import com.aicode.report.generator.MarkdownReportGenerator;
import com.aicode.report.generator.PdfReportGenerator;
import com.aicode.report.model.ReportResponse;
import com.aicode.report.model.ReviewReport;
import com.aicode.report.service.ReportService;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final MarkdownReportGenerator markdownGenerator;
    private final PdfReportGenerator pdfGenerator;

    /**
     * 生成代码审查报告
     */
    @PostMapping("/generate/{projectId}")
    public Result<ReportResponse> generate(@PathVariable Long projectId) {
        ReviewReport report = buildReport(projectId);
        ReportResponse response = ReportResponse.builder().report(report).build();
        return Result.success(response);
    }

    /**
     * 下载 Markdown 报告
     */
    @GetMapping("/{projectId}/markdown")
    public ResponseEntity<byte[]> downloadMarkdown(@PathVariable Long projectId) {
        ReviewReport report = buildReport(projectId);
        String markdown = markdownGenerator.generate(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=review-report.md")
                .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
                .body(markdown.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * 下载 PDF 报告
     */
    @GetMapping("/{projectId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long projectId) {
        ReviewReport report = buildReport(projectId);
        byte[] pdfBytes = pdfGenerator.generate(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=review-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private ReviewReport buildReport(Long projectId) {
        String sourcePath = "./git-repos/" + projectId;

        // 1. 规则检测
        List<RuleResult> ruleResults = ruleEngine.analyze(projectId, sourcePath);

        // 2. AI 批量分析
        List<RuleResult> failedResults = ruleResults.stream()
                .filter(r -> !r.isPassed()).toList();
        List<AiIssueAnalysis> analyses = aiAnalysisService.analyzeBatch(failedResults);

        // 3. 生成报告
        return reportService.generateReport("project-" + projectId, ruleResults, analyses);
    }
}
