package com.aicode.ai.service.impl;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.model.AiReviewRequest;
import com.aicode.ai.model.AiReviewResult;
import com.aicode.ai.service.AiResponseParser;
import com.aicode.ai.service.AiReviewService;
import com.aicode.ai.service.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 审查服务实现
 * <p>
 * 使用 Spring AI ChatClient 调用 DeepSeek/OpenAI，
 * 对规则检测结果进行深度分析。
 * <p>
 * 禁止直接扫描源码，禁止访问数据库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiReviewServiceImpl implements AiReviewService {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptBuilder promptBuilder;
    private final AiResponseParser responseParser;

    @Override
    public AiReviewResult review(AiReviewRequest request) {
        if (request.getRuleResults().isEmpty()) {
            return AiReviewResult.builder()
                    .projectName(request.getProjectName())
                    .overallScore(10)
                    .summary("代码扫描完成，未发现任何违规问题，代码质量良好。")
                    .issues(List.of())
                    .build();
        }

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(request.getRuleResults());

        log.info("AI 审查开始: project={}, issues={}", request.getProjectName(), request.getRuleResults().size());

        try {
            String response = chatClientBuilder.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.debug("AI 原始响应: {}", response);

            List<AiIssueAnalysis> issues = responseParser.parseBatch(response);

            return AiReviewResult.builder()
                    .projectName(request.getProjectName())
                    .overallScore(calculateScore(issues, request.getRuleResults().size()))
                    .summary("AI 深度分析完成，共分析 " + issues.size() + " 个问题。")
                    .issues(issues)
                    .build();

        } catch (Exception e) {
            log.error("AI 调用失败", e);
            return buildFallbackResult(request);
        }
    }

    /**
     * 根据问题严重程度计算评分
     */
    private int calculateScore(List<AiIssueAnalysis> issues, int totalIssues) {
        if (totalIssues == 0) return 10;
        long highCount = issues.stream().filter(i -> "高".equals(i.getRiskLevel())).count();
        long midCount = issues.stream().filter(i -> "中".equals(i.getRiskLevel())).count();
        int score = 10 - (int) (highCount * 2 + midCount);
        return Math.max(1, Math.min(10, score));
    }

    /**
     * AI 调用失败时的降级结果
     */
    private AiReviewResult buildFallbackResult(AiReviewRequest request) {
        List<AiIssueAnalysis> issues = request.getRuleResults().stream()
                .map(r -> AiIssueAnalysis.builder()
                        .ruleId(r.getRuleId())
                        .ruleName(r.getRuleName())
                        .className(r.getClassName())
                        .methodName(r.getMethodName())
                        .riskLevel("中")
                        .reason(r.getMessage())
                        .impact("")
                        .suggestion(r.getSuggestion())
                        .exampleFix("")
                        .build())
                .toList();

        return AiReviewResult.builder()
                .projectName(request.getProjectName())
                .overallScore(5)
                .summary("AI 服务暂时不可用，以下为规则引擎的原始检测结果。")
                .issues(issues)
                .build();
    }
}
