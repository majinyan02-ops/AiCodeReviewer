package com.aicode.ai.service.impl;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.model.AiReviewRequest;
import com.aicode.ai.model.AiReviewResult;
import com.aicode.ai.service.AiReviewService;
import com.aicode.ai.service.PromptBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final ObjectMapper objectMapper;

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
            return parseResponse(response, request.getProjectName());
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            return buildFallbackResult(request);
        }
    }

    /**
     * 解析 AI 返回的 JSON
     */
    private AiReviewResult parseResponse(String response, String projectName) {
        try {
            // 提取 JSON 部分（AI 可能在前后加 markdown 代码块）
            String json = extractJson(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            int overallScore = map.get("overallScore") instanceof Number n
                    ? n.intValue() : 5;
            String summary = (String) map.getOrDefault("summary", "");

            List<AiIssueAnalysis> issues = new ArrayList<>();
            Object issuesObj = map.get("issues");
            if (issuesObj instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> rawMap) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> issueMap = (Map<String, Object>) rawMap;
                        issues.add(AiIssueAnalysis.builder()
                                .ruleId(str(issueMap, "ruleId"))
                                .className(str(issueMap, "className"))
                                .methodName(str(issueMap, "methodName"))
                                .riskLevel(str(issueMap, "riskLevel", "中"))
                                .explanation(str(issueMap, "explanation"))
                                .fixSuggestion(str(issueMap, "fixSuggestion"))
                                .fixedCode(str(issueMap, "fixedCode"))
                                .build());
                    }
                }
            }

            return AiReviewResult.builder()
                    .projectName(projectName)
                    .overallScore(overallScore)
                    .summary(summary)
                    .issues(issues)
                    .build();

        } catch (JsonProcessingException e) {
            log.warn("AI 响应 JSON 解析失败，使用降级结果: {}", e.getMessage());
            return AiReviewResult.builder()
                    .projectName(projectName)
                    .overallScore(5)
                    .summary("AI 分析结果解析异常，请查看原始响应。\n原始响应:\n" + response)
                    .issues(List.of())
                    .build();
        }
    }

    /**
     * 从 AI 响应中提取 JSON 部分
     */
    private String extractJson(String response) {
        // 去掉 markdown 代码块
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        return response;
    }

    /**
     * AI 调用失败时的降级结果
     */
    private AiReviewResult buildFallbackResult(AiReviewRequest request) {
        List<AiIssueAnalysis> issues = new ArrayList<>();
        for (com.aicode.rule.model.RuleResult r : request.getRuleResults()) {
            issues.add(AiIssueAnalysis.builder()
                    .ruleId(r.getRuleId())
                    .className(r.getClassName())
                    .methodName(r.getMethodName())
                    .riskLevel("中")
                    .explanation(r.getMessage())
                    .fixSuggestion(r.getSuggestion())
                    .fixedCode("")
                    .build());
        }

        return AiReviewResult.builder()
                .projectName(request.getProjectName())
                .overallScore(5)
                .summary("AI 服务暂时不可用，以下为规则引擎的原始检测结果。")
                .issues(issues)
                .build();
    }

    private static String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private static String str(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }
}
