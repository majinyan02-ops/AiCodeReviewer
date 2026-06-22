package com.aicode.agent.summary;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.fix.model.FixStatistics;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.rule.model.RuleResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryAgentTest {

    @Mock
    private AnalysisCacheService cacheService;

    @InjectMocks
    private SummaryAgent summaryAgent;

    private ReviewAgentResult createReviewResult(int errors, int warnings, int infos, long aiDuration) {
        return ReviewAgentResult.builder()
                .totalRules(errors + warnings + infos)
                .errorCount(errors)
                .warningCount(warnings)
                .infoCount(infos)
                .overallScore(100 - errors * 10 - warnings * 5 - infos)
                .aiAnalysisDuration(aiDuration)
                .ruleResults(List.of(
                        RuleResult.builder().ruleId("R1").ruleName("Missing Transactional").severity("ERROR").passed(false).build(),
                        RuleResult.builder().ruleId("R2").ruleName("Missing Transactional").severity("ERROR").passed(false).build(),
                        RuleResult.builder().ruleId("R3").ruleName("System.out.println").severity("WARNING").passed(false).build(),
                        RuleResult.builder().ruleId("R4").ruleName("Missing Logging").severity("INFO").passed(false).build()
                ))
                .build();
    }

    private FixAgentResult createFixResult(int fixed, int failed, long aiDuration) {
        return FixAgentResult.builder()
                .totalIssues(fixed + failed)
                .fixedIssues(fixed)
                .failedIssues(failed)
                .statistics(FixStatistics.builder()
                        .totalIssues(fixed + failed)
                        .fixedIssues(fixed)
                        .failedIssues(failed)
                        .successRate((double) fixed / (fixed + failed))
                        .totalAiDuration(aiDuration)
                        .build())
                .build();
    }

    private AgentContext createContext(ReviewAgentResult review, FixAgentResult fix) {
        var attrs = new HashMap<String, Object>();
        if (review != null) attrs.put("reviewResult", review);
        if (fix != null) attrs.put("fixResult", fix);
        return AgentContext.builder()
                .projectId("test-project")
                .attributes(attrs)
                .build();
    }

    @Test
    void shouldReturnCachedResultWhenAvailable() {
        SummaryAgentResult cached = SummaryAgentResult.builder().build();
        when(cacheService.getSummaryResult("test-project")).thenReturn(cached);

        AgentResult result = summaryAgent.execute(createContext(null, null));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload()).isEqualTo(cached);
    }

    @Test
    void shouldBuildStatisticsFromReviewAndFix() {
        when(cacheService.getSummaryResult("test-project")).thenReturn(null);

        ReviewAgentResult review = createReviewResult(2, 1, 1, 5000);
        FixAgentResult fix = createFixResult(3, 1, 8000);

        AgentResult result = summaryAgent.execute(createContext(review, fix));

        assertThat(result.isSuccess()).isTrue();
        SummaryAgentResult payload = (SummaryAgentResult) result.getPayload();

        assertThat(payload.getStatistics().getErrorCount()).isEqualTo(2);
        assertThat(payload.getStatistics().getWarningCount()).isEqualTo(1);
        assertThat(payload.getStatistics().getInfoCount()).isEqualTo(1);
        assertThat(payload.getStatistics().getTotalIssues()).isEqualTo(4);
        assertThat(payload.getStatistics().getFixedIssues()).isEqualTo(3);
        assertThat(payload.getStatistics().getFailedIssues()).isEqualTo(1);
        assertThat(payload.getStatistics().getFixSuccessRate()).isEqualTo(0.75);
        assertThat(payload.getStatistics().getReviewAiDuration()).isEqualTo(5000);
        assertThat(payload.getStatistics().getFixAiDuration()).isEqualTo(8000);
        assertThat(payload.getStatistics().getTotalAiDuration()).isEqualTo(13000);
    }

    @Test
    void shouldCalculateHealthScore() {
        when(cacheService.getSummaryResult("test-project")).thenReturn(null);

        ReviewAgentResult review = createReviewResult(2, 1, 1, 0);
        FixAgentResult fix = createFixResult(3, 1, 0);

        AgentResult result = summaryAgent.execute(createContext(review, fix));

        SummaryAgentResult payload = (SummaryAgentResult) result.getPayload();
        // 100 - 20 - 5 - 1 + 6 = 80
        assertThat(payload.getHealthReport().getHealthScore()).isEqualTo(80);
        assertThat(payload.getHealthReport().getHealthLevel()).isEqualTo("GOOD");
        assertThat(payload.getHealthReport().getOverallStatus()).isEqualTo("HEALTHY");
    }

    @Test
    void shouldMapOverallStatusCorrectly() {
        when(cacheService.getSummaryResult("test-project")).thenReturn(null);

        ReviewAgentResult review = createReviewResult(0, 0, 0, 0);
        FixAgentResult fix = createFixResult(0, 0, 0);

        AgentResult result = summaryAgent.execute(createContext(review, fix));

        SummaryAgentResult payload = (SummaryAgentResult) result.getPayload();
        assertThat(payload.getHealthReport().getOverallStatus()).isEqualTo("HEALTHY");
    }

    @Test
    void shouldReturnCriticalStatusForLowScore() {
        when(cacheService.getSummaryResult("test-project")).thenReturn(null);

        ReviewAgentResult review = createReviewResult(10, 0, 0, 0);
        FixAgentResult fix = createFixResult(0, 0, 0);

        AgentResult result = summaryAgent.execute(createContext(review, fix));

        SummaryAgentResult payload = (SummaryAgentResult) result.getPayload();
        assertThat(payload.getHealthReport().getHealthScore()).isEqualTo(0);
        assertThat(payload.getHealthReport().getHealthLevel()).isEqualTo("CRITICAL");
        assertThat(payload.getHealthReport().getOverallStatus()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldExtractTopProblems() {
        when(cacheService.getSummaryResult("test-project")).thenReturn(null);

        ReviewAgentResult review = ReviewAgentResult.builder()
                .errorCount(5)
                .warningCount(3)
                .infoCount(2)
                .aiAnalysisDuration(0L)
                .ruleResults(List.of(
                        RuleResult.builder().ruleId("R1").ruleName("Missing Transactional").severity("ERROR").passed(false).build(),
                        RuleResult.builder().ruleId("R2").ruleName("Missing Transactional").severity("ERROR").passed(false).build(),
                        RuleResult.builder().ruleId("R3").ruleName("Missing Transactional").severity("ERROR").passed(false).build(),
                        RuleResult.builder().ruleId("R4").ruleName("System.out.println").severity("WARNING").passed(false).build(),
                        RuleResult.builder().ruleId("R5").ruleName("System.out.println").severity("WARNING").passed(false).build(),
                        RuleResult.builder().ruleId("R6").ruleName("Long Method").severity("INFO").passed(false).build()
                ))
                .build();
        FixAgentResult fix = createFixResult(0, 0, 0);

        AgentResult result = summaryAgent.execute(createContext(review, fix));

        SummaryAgentResult payload = (SummaryAgentResult) result.getPayload();
        List<String> topProblems = payload.getHealthReport().getTopProblems();
        assertThat(topProblems).hasSize(3);
        assertThat(topProblems.get(0)).contains("Missing Transactional");
        assertThat(topProblems.get(0)).contains("3");
    }

    @Test
    void shouldHandleNullReviewAndFix() {
        when(cacheService.getSummaryResult("test-project")).thenReturn(null);

        AgentResult result = summaryAgent.execute(createContext(null, null));

        assertThat(result.isSuccess()).isTrue();
        SummaryAgentResult payload = (SummaryAgentResult) result.getPayload();
        assertThat(payload.getStatistics().getTotalIssues()).isEqualTo(0);
        assertThat(payload.getHealthReport().getHealthScore()).isEqualTo(100);
    }

    @Test
    void shouldReturnCorrectAgentType() {
        assertThat(summaryAgent.getType()).isEqualTo(AgentType.SUMMARY);
    }
}
