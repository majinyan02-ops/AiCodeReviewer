package com.aicode.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 审查结果 — AI 分析的整体输出
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReviewResult {

    /** 项目名称 */
    private String projectName;

    /** 总体评分 (1-10) */
    private int overallScore;

    /** 总体评价 */
    private String summary;

    /** 每个问题的 AI 分析 */
    @Builder.Default
    private List<AiIssueAnalysis> issues = new ArrayList<>();
}
