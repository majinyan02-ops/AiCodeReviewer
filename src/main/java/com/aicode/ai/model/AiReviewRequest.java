package com.aicode.ai.model;

import com.aicode.rule.model.RuleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 审查请求 — 将 RuleResult 列表打包为 AI 分析的输入
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReviewRequest {

    /** 项目名称 */
    private String projectName;

    /** 规则检测结果列表 */
    @Builder.Default
    private List<RuleResult> ruleResults = new ArrayList<>();
}
