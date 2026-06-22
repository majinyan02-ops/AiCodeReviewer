package com.aicode.agent;

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
    void shouldThrowExceptionForParallelMode() {
        AgentContext context = AgentContext.builder().projectId("1").build();

        assertThatThrownBy(() ->
                agentOrchestrator.executeAgents(
                        List.of(AgentType.REVIEW), context, AgentExecutionMode.PARALLEL))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldExecuteSequentialBatch() {
        AgentContext context = AgentContext.builder()
                .projectId("1")
                .build();

        List<AgentResult> results = agentOrchestrator.executeAgents(
                List.of(AgentType.SUMMARY), context, AgentExecutionMode.SEQUENTIAL);

        assertThat(results).hasSize(1);
    }
}
