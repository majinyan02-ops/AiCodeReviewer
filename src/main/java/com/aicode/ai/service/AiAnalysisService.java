package com.aicode.ai.service;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.rule.model.RuleResult;

import java.util.List;

/**
 * AI 分析服务接口
 * <p>
 * 对规则检测结果进行 AI 深度分析，返回结构化的分析结果。
 */
public interface AiAnalysisService {

    /**
     * 单条规则结果分析
     * @param ruleResult 规则检测结果（含 contentHash 用于缓存）
     * @param projectId  项目 ID（用于缓存命名空间）
     */
    AiIssueAnalysis analyze(RuleResult ruleResult, Long projectId);

    /**
     * 批量规则结果分析（并发执行）
     */
    List<AiIssueAnalysis> analyzeBatch(List<RuleResult> results, Long projectId);
}
