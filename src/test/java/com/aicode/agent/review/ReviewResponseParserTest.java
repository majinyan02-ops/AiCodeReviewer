package com.aicode.agent.review.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewResponseParserTest {

    private final ReviewResponseParser parser = new ReviewResponseParser(new ObjectMapper());

    @Test
    void shouldParseValidJson() {
        String response = """
                {
                  "summary": "项目存在事务缺失问题",
                  "riskLevel": "HIGH"
                }
                """;

        ReviewResponseParser.ReviewAnalysis result = parser.parse(response);

        assertThat(result.getSummary()).isEqualTo("项目存在事务缺失问题");
        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    void shouldParseJsonWrappedInMarkdown() {
        String response = """
                ```json
                {
                  "summary": "代码质量良好",
                  "riskLevel": "LOW"
                }
                ```
                """;

        ReviewResponseParser.ReviewAnalysis result = parser.parse(response);

        assertThat(result.getSummary()).isEqualTo("代码质量良好");
        assertThat(result.getRiskLevel()).isEqualTo("LOW");
    }

    @Test
    void shouldHandleInvalidJson() {
        String response = "This is not valid JSON";

        ReviewResponseParser.ReviewAnalysis result = parser.parse(response);

        assertThat(result.getSummary()).contains("AI 解析异常");
        assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void shouldHandleMissingFields() {
        String response = """
                {
                  "summary": "测试"
                }
                """;

        ReviewResponseParser.ReviewAnalysis result = parser.parse(response);

        assertThat(result.getSummary()).isEqualTo("测试");
        assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
    }
}
