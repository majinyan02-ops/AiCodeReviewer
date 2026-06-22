package com.aicode.agent;

import com.aicode.agent.registry.AgentRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AgentRegistryTest {

    @Autowired
    private AgentRegistry agentRegistry;

    @Test
    void shouldRegisterExistingAgents() {
        assertThat(agentRegistry.getAllAgents()).isNotEmpty();
        assertThat(agentRegistry.contains(AgentType.REVIEW)).isTrue();
        assertThat(agentRegistry.contains(AgentType.FIX)).isTrue();
    }

    @Test
    void shouldGetRegisteredAgent() {
        Agent agent = agentRegistry.getAgent(AgentType.REVIEW);
        assertThat(agent).isNotNull();
        assertThat(agent.getType()).isEqualTo(AgentType.REVIEW);
    }

    @Test
    void shouldReturnNullForUnregisteredType() {
        assertThat(agentRegistry.getAgent(AgentType.SUMMARY)).isNull();
    }

    @Test
    void shouldReturnFalseForUnregisteredType() {
        assertThat(agentRegistry.contains(AgentType.SUMMARY)).isFalse();
    }
}
