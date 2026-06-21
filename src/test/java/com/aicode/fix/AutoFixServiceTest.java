package com.aicode.fix;

import com.aicode.fix.model.FixSuggestion;
import com.aicode.fix.parser.FixResponseParser;
import com.aicode.fix.prompt.FixPromptBuilder;
import com.aicode.fix.service.AutoFixService;
import com.aicode.fix.service.FixCacheService;
import com.aicode.rule.model.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AutoFixServiceTest {

    @Autowired
    private AutoFixService autoFixService;

    @Autowired
    private FixResponseParser responseParser;

    @Autowired
    private FixPromptBuilder promptBuilder;

    private RuleResult createTestRuleResult() {
        return RuleResult.builder()
                .ruleId("RULE-004")
                .ruleName("System.out.println")
                .severity("WARNING")
                .className("DemoController")
                .methodName("hello")
                .filePath("src/main/java/com/example/DemoController.java")
                .lineNumber(15)
                .message("存在System.out.println调试输出")
                .contentHash("test-hash-001")
                .build();
    }

    @Test
    void testPromptBuilder() {
        RuleResult ruleResult = createTestRuleResult();
        String prompt = promptBuilder.build(ruleResult);

        assertThat(prompt).contains("RULE-004");
        assertThat(prompt).contains("DemoController");
        assertThat(prompt).contains("hello");
        assertThat(prompt).contains("15");
    }

    @Test
    void testResponseParserNormal() {
        String aiResponse = """
                {
                    "originalCode": "System.out.println(user);",
                    "fixedCode": "log.info(\\"user={}\\", user);",
                    "explanation": "使用Slf4j日志框架替代System.out",
                    "confidence": 0.95,
                    "riskLevel": "LOW"
                }
                """;

        FixSuggestion suggestion = responseParser.parse(
                aiResponse, "RULE-004", "System.out.println", "WARNING");

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getRuleId()).isEqualTo("RULE-004");
        assertThat(suggestion.getOriginalCode()).contains("System.out.println");
        assertThat(suggestion.getFixedCode()).contains("log.info");
        assertThat(suggestion.getConfidence()).isEqualTo(0.95);
        assertThat(suggestion.getRiskLevel()).isEqualTo("LOW");
        assertThat(suggestion.getGeneratedTime()).isNotNull();
    }

    @Test
    void testResponseParserWithMarkdown() {
        String aiResponse = """
                ```json
                {
                    "originalCode": "System.out.println(x);",
                    "fixedCode": "log.debug(\\"x={}\\", x);",
                    "explanation": "替换为日志",
                    "confidence": 0.85,
                    "riskLevel": "LOW"
                }
                ```
                """;

        FixSuggestion suggestion = responseParser.parse(
                aiResponse, "RULE-004", "System.out.println", "WARNING");

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getFixedCode()).contains("log.debug");
    }

    @Test
    void testResponseParserInvalidJson() {
        String badResponse = "This is not valid JSON";

        FixSuggestion suggestion = responseParser.parse(
                badResponse, "RULE-004", "System.out.println", "WARNING");

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getConfidence()).isEqualTo(0.0);
        assertThat(suggestion.getRiskLevel()).isEqualTo("HIGH");
        assertThat(suggestion.getExplanation()).contains("AI 解析异常");
    }

    @Test
    void testGenerateFix() {
        RuleResult ruleResult = createTestRuleResult();
        FixSuggestion suggestion = autoFixService.generateFix(ruleResult);

        assertThat(suggestion).isNotNull();
        assertThat(suggestion.getRuleId()).isEqualTo("RULE-004");
        assertThat(suggestion.getRuleName()).isEqualTo("System.out.println");
    }

    @Test
    void testGenerateFixCacheHit() {
        RuleResult ruleResult = createTestRuleResult();

        FixSuggestion first = autoFixService.generateFix(ruleResult);
        FixSuggestion second = autoFixService.generateFix(ruleResult);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getFixedCode()).isEqualTo(second.getFixedCode());
    }
}
