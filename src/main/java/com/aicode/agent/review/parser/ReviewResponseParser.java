package com.aicode.agent.review.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Review Agent AI 响应解析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewResponseParser {

    private final ObjectMapper objectMapper;

    public ReviewAnalysis parse(String response) {
        try {
            String json = extractJson(response);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            return ReviewAnalysis.builder()
                    .summary(str(map, "summary"))
                    .riskLevel(str(map, "riskLevel", "MEDIUM"))
                    .build();

        } catch (JsonProcessingException e) {
            log.warn("Review AI 响应 JSON 解析失败", e);
            return ReviewAnalysis.builder()
                    .summary("AI 解析异常")
                    .riskLevel("MEDIUM")
                    .build();
        }
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

    @lombok.Builder
    @lombok.Data
    public static class ReviewAnalysis {
        private String summary;
        private String riskLevel;
    }
}
