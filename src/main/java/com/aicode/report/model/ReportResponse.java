package com.aicode.report.model;

import com.aicode.ai.model.AiIssueAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 报告响应 DTO — Controller 返回给前端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    /** 报告 */
    private ReviewReport report;

    /** AI 分析详情（可选，前端按需展开） */
    @Builder.Default
    private List<AiIssueAnalysis> aiAnalyses = new ArrayList<>();
}
