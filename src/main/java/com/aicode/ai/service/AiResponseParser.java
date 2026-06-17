package com.aicode.ai.service;

import com.aicode.ai.model.AiIssueAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 响应解析器 — 将 AI 返回的 JSON 解析为结构化对象
 * <p>
 * 支持单条和批量两种模式的解析。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * 解析单条 AI 响应 → AiIssueAnalysis
     */
    public AiIssueAnalysis parseSingle(String response, String ruleId, String ruleName,
                                        String className, String methodName) {
        try {
            String json = extractJson(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            return AiIssueAnalysis.builder()
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .className(className)
                    .methodName(methodName)
                    .riskLevel(str(map, "riskLevel", "中"))
                    .reason(str(map, "reason"))
                    .impact(str(map, "impact"))
                    .suggestion(str(map, "suggestion"))
                    .exampleFix(str(map, "exampleFix"))
                    .build();

        } catch (JsonProcessingException e) {
            log.warn("AI 单条响应 JSON 解析失败", e);
            return AiIssueAnalysis.builder()
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .className(className)
                    .methodName(methodName)
                    .riskLevel("中")
                    .reason("AI 解析异常: " + response)
                    .impact("")
                    .suggestion("")
                    .exampleFix("")
                    .build();
        }
    }

    /**
     * 解析批量 AI 响应 → List<AiIssueAnalysis>
     */
    public List<AiIssueAnalysis> parseBatch(String response) {
        try {
            String json = extractJson(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            List<AiIssueAnalysis> issues = new ArrayList<>();
            Object issuesObj = map.get("issues");
            if (issuesObj instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> rawMap) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> issueMap = (Map<String, Object>) rawMap;
                        issues.add(AiIssueAnalysis.builder()
                                .ruleId(str(issueMap, "ruleId"))
                                .ruleName(str(issueMap, "ruleName"))
                                .className(str(issueMap, "className"))
                                .methodName(str(issueMap, "methodName"))
                                .riskLevel(str(issueMap, "riskLevel", "中"))
                                .reason(str(issueMap, "reason"))
                                .impact(str(issueMap, "impact"))
                                .suggestion(str(issueMap, "suggestion"))
                                .exampleFix(str(issueMap, "exampleFix"))
                                .build());
                    }
                }
            }

            return issues;

        } catch (JsonProcessingException e) {
            log.warn("AI 批量响应 JSON 解析失败", e);
            return List.of();
        }
    }

    /**
     * 从 AI 响应中提取 JSON 部分（去掉 markdown 代码块）
     */
    private String extractJson(String response) {
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        return response;
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
