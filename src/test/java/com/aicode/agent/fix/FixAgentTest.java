package com.aicode.agent.fix;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.fix.model.FixItem;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.fix.model.FixSuggestion;
import com.aicode.fix.service.AutoFixService;
import com.aicode.patch.model.PatchResult;
import com.aicode.patch.service.PatchService;
import com.aicode.rule.model.RuleResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixAgentTest {

    @Mock
    private AutoFixService autoFixService;

    @Mock
    private PatchService patchService;

    @Mock
    private AnalysisCacheService cacheService;

    @InjectMocks
    private FixAgent fixAgent;

    private AgentContext createContextWithReview(ReviewAgentResult reviewResult) {
        return AgentContext.builder()
                .projectId("test-project")
                .attributes(new java.util.HashMap<>() {{
                    put("reviewResult", reviewResult);
                }})
                .build();
    }

    private ReviewAgentResult createReviewResult(List<RuleResult> ruleResults) {
        return ReviewAgentResult.builder()
                .totalRules(ruleResults.size())
                .errorCount(1)
                .warningCount(1)
                .infoCount(0)
                .overallScore(85)
                .riskLevel("MEDIUM")
                .ruleResults(ruleResults)
                .build();
    }

    private List<RuleResult> createRuleResults() {
        return List.of(
                RuleResult.builder()
                        .ruleId("RULE-001")
                        .ruleName("Missing Transactional")
                        .severity("ERROR")
                        .className("UserService")
                        .methodName("createUser")
                        .message("缺少@Transactional注解")
                        .passed(false)
                        .build(),
                RuleResult.builder()
                        .ruleId("RULE-003")
                        .ruleName("Controller Direct Mapper")
                        .severity("ERROR")
                        .className("DemoController")
                        .methodName("hello")
                        .message("Controller直接调用Mapper")
                        .passed(false)
                        .build()
        );
    }

    @Test
    void shouldReturnCachedResultWhenAvailable() {
        FixAgentResult cached = FixAgentResult.builder()
                .totalIssues(2)
                .fixedIssues(2)
                .build();
        when(cacheService.getFixResult("test-project")).thenReturn(cached);

        AgentResult result = fixAgent.execute(createContextWithReview(null));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload()).isEqualTo(cached);
        verify(autoFixService, never()).generateFix(any());
    }

    @Test
    void shouldReturnFailureWhenNoReviewResult() {
        AgentContext context = AgentContext.builder()
                .projectId("test-project")
                .build();

        AgentResult result = fixAgent.execute(context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("ReviewAgentResult not found");
    }

    @Test
    void shouldExecuteFixAndReturnResult() {
        when(cacheService.getFixResult("test-project")).thenReturn(null);

        List<RuleResult> ruleResults = createRuleResults();
        ReviewAgentResult reviewResult = createReviewResult(ruleResults);

        when(autoFixService.generateFix(any(RuleResult.class))).thenReturn(
                FixSuggestion.builder()
                        .explanation("添加@Transactional注解")
                        .build()
        );
        when(patchService.generatePatch(any(FixSuggestion.class))).thenReturn(
                PatchResult.builder()
                        .patchContent("@Transactional")
                        .valid(true)
                        .build()
        );

        AgentResult result = fixAgent.execute(createContextWithReview(reviewResult));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAgentType()).isEqualTo(AgentType.FIX);

        FixAgentResult payload = (FixAgentResult) result.getPayload();
        assertThat(payload.getTotalIssues()).isEqualTo(2);
        assertThat(payload.getFixedIssues()).isEqualTo(2);
        assertThat(payload.getFailedIssues()).isEqualTo(0);
        assertThat(payload.getFixItems()).hasSize(2);
        assertThat(payload.getStatistics().getSuccessRate()).isEqualTo(1.0);
    }

    @Test
    void shouldHandlePatchFailureGracefully() {
        when(cacheService.getFixResult("test-project")).thenReturn(null);

        List<RuleResult> ruleResults = createRuleResults();
        ReviewAgentResult reviewResult = createReviewResult(ruleResults);

        when(autoFixService.generateFix(any(RuleResult.class))).thenReturn(
                FixSuggestion.builder()
                        .explanation("修复建议")
                        .build()
        );
        when(patchService.generatePatch(any(FixSuggestion.class)))
                .thenThrow(new RuntimeException("Patch generation failed"));

        AgentResult result = fixAgent.execute(createContextWithReview(reviewResult));

        assertThat(result.isSuccess()).isTrue();
        FixAgentResult payload = (FixAgentResult) result.getPayload();
        assertThat(payload.getTotalIssues()).isEqualTo(2);
        assertThat(payload.getFixedIssues()).isEqualTo(0);
        assertThat(payload.getFailedIssues()).isEqualTo(2);
        payload.getFixItems().forEach(item ->
                assertThat(item.isPatchGenerated()).isFalse());
    }

    @Test
    void shouldFilterPassedAndNonErrorRules() {
        when(cacheService.getFixResult("test-project")).thenReturn(null);

        List<RuleResult> ruleResults = List.of(
                RuleResult.builder()
                        .ruleId("RULE-001")
                        .severity("ERROR")
                        .className("UserService")
                        .methodName("createUser")
                        .message("问题")
                        .passed(true)
                        .build(),
                RuleResult.builder()
                        .ruleId("RULE-002")
                        .severity("WARNING")
                        .className("DemoController")
                        .methodName("hello")
                        .message("警告")
                        .passed(false)
                        .build()
        );
        ReviewAgentResult reviewResult = createReviewResult(ruleResults);

        AgentResult result = fixAgent.execute(createContextWithReview(reviewResult));

        assertThat(result.isSuccess()).isTrue();
        FixAgentResult payload = (FixAgentResult) result.getPayload();
        assertThat(payload.getTotalIssues()).isEqualTo(0);
        assertThat(payload.getFixItems()).isEmpty();
    }

    @Test
    void shouldReturnCorrectAgentType() {
        assertThat(fixAgent.getType()).isEqualTo(AgentType.FIX);
    }
}
