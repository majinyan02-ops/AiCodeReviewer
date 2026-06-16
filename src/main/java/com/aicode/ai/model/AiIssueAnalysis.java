package com.aicode.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 问题分析 — AI 对单个规则违规的深度分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiIssueAnalysis {

    /** 对应的规则编号 */
    private String ruleId;

    /** 违规类名 */
    private String className;

    /** 违规方法名 */
    private String methodName;

    /** 风险等级: 高 / 中 / 低 */
    private String riskLevel;

    /** 问题解释 */
    private String explanation;

    /** 修复建议 */
    private String fixSuggestion;

    /** 修复后的代码示例 */
    private String fixedCode;
}
