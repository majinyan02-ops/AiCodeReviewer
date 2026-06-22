package com.aicode.agent.review.prompt;

import com.aicode.rule.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewPromptBuilderTest {

    private final ReviewPromptBuilder builder = new ReviewPromptBuilder();

    @Test
    void shouldBuildPromptWithRuleResults() {
        List<RuleResult> results = List.of(
                RuleResult.builder()
                        .ruleId("RULE-001")
                        .ruleName("Missing Transactional")
                        .severity("ERROR")
                        .className("UserService")
                        .methodName("createUser")
                        .message("缺少@Transactional注解")
                        .build()
        );

        String prompt = builder.build(results);

        assertThat(prompt).contains("RULE-001");
        assertThat(prompt).contains("Missing Transactional");
        assertThat(prompt).contains("ERROR");
        assertThat(prompt).contains("UserService");
        assertThat(prompt).contains("createUser");
        assertThat(prompt).contains("缺少@Transactional注解");
        assertThat(prompt).contains("JSON");
    }

    @Test
    void shouldHandleEmptyResults() {
        String prompt = builder.build(List.of());

        assertThat(prompt).contains("请对以下代码审查结果进行综合分析");
        assertThat(prompt).contains("JSON");
    }
}
