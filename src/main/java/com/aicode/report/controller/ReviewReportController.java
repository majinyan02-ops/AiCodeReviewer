package com.aicode.report.controller;

import com.aicode.common.Result;
import com.aicode.report.dto.ReportProgress;
import com.aicode.report.generator.MarkdownReportGenerator;
import com.aicode.report.generator.PdfReportGenerator;
import com.aicode.report.model.ReportResponse;
import com.aicode.report.model.ReviewReport;
import com.aicode.report.service.ReportService;
import com.aicode.report.service.ReportTaskTracker;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 代码审查报告控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReviewReportController {

    private final RuleEngine ruleEngine;
    private final ReportService reportService;
    private final ReportTaskTracker taskTracker;
    private final MarkdownReportGenerator markdownGenerator;
    private final PdfReportGenerator pdfGenerator;

    /**
     * 提交异步报告生成任务（推荐）
     * 立即返回 taskId，前端轮询 GET /api/report/progress/{taskId}
     */
    @PostMapping("/generate/{projectId}/async")
    public Result<Map<String, String>> generateAsync(@PathVariable Long projectId) {
        String taskId = taskTracker.submit(projectId);
        return Result.success(Map.of("taskId", taskId));
    }

    /**
     * 查询报告生成进度（按 taskId）
     */
    @GetMapping("/progress/{taskId}")
    public Result<ReportProgress> getProgress(@PathVariable String taskId) {
        ReportProgress progress = taskTracker.getProgress(taskId);
        return Result.success(progress);
    }

    /**
     * 查询报告生成进度（按 projectId，跳转页面后无需 taskId）
     */
    @GetMapping("/progress/project/{projectId}")
    public Result<ReportProgress> getProgressByProject(@PathVariable Long projectId) {
        ReportProgress progress = taskTracker.getProgressByProjectId(projectId);
        return Result.success(progress);
    }

    /**
     * 同步生成报告（数据量大时可能超时）
     */
    @PostMapping("/generate/{projectId}")
    public Result<ReportResponse> generate(@PathVariable Long projectId) {
        String taskId = taskTracker.submit(projectId);

        // 轮询等待完成（最多 5 分钟）
        for (int i = 0; i < 300; i++) {
            ReportProgress progress = taskTracker.getProgress(taskId);
            if ("SUCCESS".equals(progress.getStatus())) {
                ReportResponse response = ReportResponse.builder()
                        .report(progress.getReport()).build();
                return Result.success(response);
            }
            if ("FAILED".equals(progress.getStatus())) {
                return Result.fail(progress.getError());
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }
        return Result.fail("报告生成超时");
    }

    /**
     * 下载 Markdown 报告
     */
    @GetMapping("/{taskId}/markdown")
    public ResponseEntity<byte[]> downloadMarkdown(@PathVariable String taskId) {
        ReportProgress progress = taskTracker.getProgress(taskId);
        if (!"SUCCESS".equals(progress.getStatus()) || progress.getReport() == null) {
            throw new RuntimeException("报告未就绪，当前状态: " + progress.getStatus());
        }
        String markdown = markdownGenerator.generate(progress.getReport());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=review-report.md")
                .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
                .body(markdown.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * 下载 PDF 报告
     */
    @GetMapping("/{taskId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String taskId) {
        ReportProgress progress = taskTracker.getProgress(taskId);
        if (!"SUCCESS".equals(progress.getStatus()) || progress.getReport() == null) {
            throw new RuntimeException("报告未就绪，当前状态: " + progress.getStatus());
        }
        byte[] pdfBytes = pdfGenerator.generate(progress.getReport());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=review-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
