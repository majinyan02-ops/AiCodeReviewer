package com.aicode.ai.service.impl;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.service.AiAnalysisService;
import com.aicode.ai.service.AiResponseParser;
import com.aicode.ai.service.PromptService;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 分析服务实现
 * <p>
 * 使用 Spring AI ChatClient 对每个规则违规进行深度分析。
 * 批量分析使用 CompletableFuture 并发执行，提升效率。
 * <p>
 * 禁止直接扫描源码，禁止访问数据库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptService promptService;
    private final AiResponseParser responseParser;

    @Override
    public AiIssueAnalysis analyze(RuleResult ruleResult) {
        if (ruleResult == null) return null;

        String systemPrompt = promptService.getSystemPrompt();
        String userPrompt = promptService.buildUserPrompt(ruleResult);

        log.info("AI 单条分析: {}.{}()", ruleResult.getClassName(), ruleResult.getMethodName());

        try {
            String response = chatClientBuilder.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.debug("AI 原始响应: {}", response);

            return responseParser.parseSingle(response,
                    ruleResult.getRuleId(), ruleResult.getRuleName(),
                    ruleResult.getClassName(), ruleResult.getMethodName());

        } catch (Exception e) {
            log.error("AI 单条分析失败: {}.{}()", ruleResult.getClassName(), ruleResult.getMethodName(), e);
            return fallback(ruleResult);
        }
    }

    @Override
    public List<AiIssueAnalysis> analyzeBatch(List<RuleResult> results) {
        if (results == null || results.isEmpty()) return List.of();

        log.info("AI 批量分析开始: {} 个问题（并发执行）", results.size());

        List<CompletableFuture<AiIssueAnalysis>> futures = results.stream()
                .map(r -> CompletableFuture.supplyAsync(() -> analyze(r)))
                .toList();

        List<AiIssueAnalysis> analyses = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long successCount = analyses.stream()
                .filter(a -> a != null && a.getReason() != null && !a.getReason().startsWith("AI 解析异常"))
                .count();

        log.info("AI 批量分析完成: {}/{} 成功", successCount, results.size());
        return analyses;
    }

    private AiIssueAnalysis fallback(RuleResult r) {
        return AiIssueAnalysis.builder()
                .ruleId(r.getRuleId())
                .ruleName(r.getRuleName())
                .className(r.getClassName())
                .methodName(r.getMethodName())
                .riskLevel("中")
                .reason(r.getMessage())
                .impact("")
                .suggestion(r.getSuggestion())
                .exampleFix("")
                .build();
    }
}
