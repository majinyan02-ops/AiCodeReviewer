package com.aicode.report.controller;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.common.Result;
import com.aicode.report.service.AgentReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Agent 报告下载控制器
 */
@RestController
@RequestMapping("/api/agent/report")
@RequiredArgsConstructor
public class AgentReportController {

    private final AnalysisCacheService cacheService;
    private final AgentReportService agentReportService;
    private final ObjectMapper objectMapper;

    /**
     * 下载 Markdown 报告
     */
    @GetMapping("/markdown/{projectId}")
    public ResponseEntity<String> downloadMarkdown(@PathVariable String projectId,
                                                   @RequestParam(defaultValue = "") String projectName) {
        SummaryAgentResult summaryResult = getSummaryResult(projectId);
        if (summaryResult == null) {
            return ResponseEntity.notFound().build();
        }

        String markdown = agentReportService.generateMarkdown(summaryResult, projectName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-" + projectId + ".md\"")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(markdown);
    }

    /**
     * 下载 PDF 报告
     */
    @GetMapping("/pdf/{projectId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String projectId,
                                              @RequestParam(defaultValue = "") String projectName) {
        SummaryAgentResult summaryResult = getSummaryResult(projectId);
        if (summaryResult == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = agentReportService.generatePdf(summaryResult, projectName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"report-" + projectId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private SummaryAgentResult getSummaryResult(String projectId) {
        Object cached = cacheService.getSummaryResult(projectId);
        if (cached instanceof SummaryAgentResult r) return r;
        if (cached != null) return objectMapper.convertValue(cached, SummaryAgentResult.class);
        return null;
    }
}
