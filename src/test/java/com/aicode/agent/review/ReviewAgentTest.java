package com.aicode.agent.review;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.review.parser.ReviewResponseParser;
import com.aicode.agent.review.prompt.ReviewPromptBuilder;
import com.aicode.analysis.model.CallGraph;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewAgentTest {

    @Mock
    private RuleEngine ruleEngine;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ReviewPromptBuilder promptBuilder;

    @Mock
    private ReviewResponseParser responseParser;

    @Mock
    private AnalysisCacheService cacheService;

    @InjectMocks
    private ReviewAgent reviewAgent;

    private AgentContext createContext() {
        return AgentContext.builder()
                .projectId("test-project")
                .projectCodeModel(ProjectCodeModel.builder().build())
                .callGraph(CallGraph.builder().build())
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
                        .build(),
                RuleResult.builder()
                        .ruleId("RULE-004")
                        .ruleName("System.out.println")
                        .severity("WARNING")
                        .className("DemoController")
                        .methodName("hello")
                        .message("存在System.out.println调试输出")
                        .build()
        );
    }

    @Test
    void shouldReturnCachedResultWhenAvailable() {
        ReviewAgentResult cached = ReviewAgentResult.builder()
                .overallScore(85)
                .riskLevel("MEDIUM")
                .build();
        when(cacheService.getReviewResult("test-project")).thenReturn(cached);

        AgentResult result = reviewAgent.execute(createContext());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayload()).isEqualTo(cached);
        verify(ruleEngine, never()).analyze(any(ProjectCodeModel.class), any(CallGraph.class));
    }

    @Test
    void shouldExecuteRuleEngineAndReturnResult() {
        when(cacheService.getReviewResult("test-project")).thenReturn(null);
        when(ruleEngine.analyze(any(ProjectCodeModel.class), any(CallGraph.class)))
                .thenReturn(createRuleResults());
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("AI unavailable"));

        AgentResult result = reviewAgent.execute(createContext());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAgentType()).isEqualTo(AgentType.REVIEW);
        ReviewAgentResult payload = (ReviewAgentResult) result.getPayload();
        assertThat(payload.getTotalRules()).isEqualTo(2);
        assertThat(payload.getErrorCount()).isEqualTo(1);
        assertThat(payload.getWarningCount()).isEqualTo(1);
        assertThat(payload.getInfoCount()).isEqualTo(0);
        assertThat(payload.getOverallScore()).isEqualTo(85);
    }

    @Test
    void shouldCalculateScoreCorrectly() {
        when(cacheService.getReviewResult("test-project")).thenReturn(null);
        when(ruleEngine.analyze(any(ProjectCodeModel.class), any(CallGraph.class)))
                .thenReturn(createRuleResults());
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("AI unavailable"));

        AgentResult result = reviewAgent.execute(createContext());

        ReviewAgentResult payload = (ReviewAgentResult) result.getPayload();
        // 1 ERROR (-10) + 1 WARNING (-5) = 100 - 10 - 5 = 85
        assertThat(payload.getOverallScore()).isEqualTo(85);
    }

    @Test
    void shouldHandleAiFailureGracefully() {
        when(cacheService.getReviewResult("test-project")).thenReturn(null);
        when(ruleEngine.analyze(any(ProjectCodeModel.class), any(CallGraph.class)))
                .thenReturn(List.of());
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("AI unavailable"));

        AgentResult result = reviewAgent.execute(createContext());

        assertThat(result.isSuccess()).isTrue();
        ReviewAgentResult payload = (ReviewAgentResult) result.getPayload();
        assertThat(payload.getOverallScore()).isEqualTo(100);
        assertThat(payload.getSummary()).isEmpty();
    }

    @Test
    void shouldReturnCorrectAgentType() {
        assertThat(reviewAgent.getType()).isEqualTo(AgentType.REVIEW);
    }
}
