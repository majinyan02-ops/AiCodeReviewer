package com.aicode.ai.service;

import com.aicode.ai.model.AiReviewRequest;
import com.aicode.ai.model.AiReviewResult;

/**
 * AI 审查服务接口
 * <p>
 * 输入：RuleResult 列表
 * 输出：AI 深度分析结果
 * <p>
 * 禁止直接扫描源码，禁止访问数据库。
 */
public interface AiReviewService {

    /**
     * 对规则检测结果进行 AI 深度分析
     *
     * @param request 包含规则检测结果的请求
     * @return AI 分析结果
     */
    AiReviewResult review(AiReviewRequest request);
}
