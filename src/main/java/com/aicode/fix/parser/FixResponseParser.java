package com.aicode.fix.parser;

import com.aicode.fix.model.FixSuggestion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixResponseParser {

    private final ObjectMapper objectMapper;

    public FixSuggestion parse(String response, String ruleId, String ruleName, String severity) {
        try {
            String json = extractJson(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            return FixSuggestion.builder()
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .severity(severity)
                    .originalCode(str(map, "originalCode"))
                    .fixedCode(str(map, "fixedCode"))
                    .explanation(str(map, "explanation"))
                    .confidence(dbl(map, "confidence", 0.5))
                    .riskLevel(str(map, "riskLevel", "MEDIUM"))
                    .generatedTime(LocalDateTime.now())
                    .build();

        } catch (JsonProcessingException e) {
            log.warn("Fix AI 响应 JSON 解析失败", e);
            return fallback(ruleId, ruleName, severity, response);
        }
    }

    private FixSuggestion fallback(String ruleId, String ruleName, String severity, String response) {
        return FixSuggestion.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .severity(severity)
                .originalCode("")
                .fixedCode("")
                .explanation("AI 解析异常: " + response)
                .confidence(0.0)
                .riskLevel("HIGH")
                .generatedTime(LocalDateTime.now())
                .build();
    }

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

    private static double dbl(Map<String, Object> map, String key, double defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return defaultVal;
    }
}
