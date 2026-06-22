package com.aicode.agent;

import com.aicode.agent.registry.AgentRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AgentRegistryTest {

    @Autowired
    private AgentRegistry agentRegistry;

    @Test
    void shouldRegisterNoAgentsWhenNoneExist() {
        assertThat(agentRegistry.getAllAgents()).isEmpty();
    }

    @Test
    void shouldReturnNullForUnregisteredType() {
        assertThat(agentRegistry.getAgent(AgentType.REVIEW)).isNull();
    }

    @Test
    void shouldReturnFalseForUnregisteredType() {
        assertThat(agentRegistry.contains(AgentType.REVIEW)).isFalse();
    }
}
