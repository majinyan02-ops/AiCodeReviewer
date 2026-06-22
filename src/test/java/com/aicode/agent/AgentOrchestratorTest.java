package com.aicode.agent;

import com.aicode.agent.registry.AgentRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AgentOrchestratorTest {

    @Autowired
    private AgentOrchestrator agentOrchestrator;

    @Test
    void shouldReturnFailureWhenAgentNotFound() {
        AgentContext context = AgentContext.builder().projectId("1").build();
        AgentResult result = agentOrchestrator.executeAgent(AgentType.REVIEW, context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Agent not found");
    }

    @Test
    void shouldReturnFailureForBatchWhenAgentNotFound() {
        AgentContext context = AgentContext.builder().projectId("1").build();
        List<AgentResult> results = agentOrchestrator.executeAgents(
                List.of(AgentType.REVIEW), context, AgentExecutionMode.SEQUENTIAL);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isSuccess()).isFalse();
    }

    @Test
    void shouldThrowExceptionForParallelMode() {
        AgentContext context = AgentContext.builder().projectId("1").build();

        assertThatThrownBy(() ->
                agentOrchestrator.executeAgents(
                        List.of(AgentType.REVIEW), context, AgentExecutionMode.PARALLEL))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
