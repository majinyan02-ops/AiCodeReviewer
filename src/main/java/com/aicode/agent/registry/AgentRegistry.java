package com.aicode.agent.registry;

import com.aicode.agent.Agent;
import com.aicode.agent.AgentType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 注册中心 — 自动发现 Spring Bean 中的所有 Agent 实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentRegistry {

    private final List<Agent> agentList;

    private final Map<AgentType, Agent> agents = new EnumMap<>(AgentType.class);

    @PostConstruct
    public void init() {
        for (Agent agent : agentList) {
            Agent old = agents.put(agent.getType(), agent);
            if (old != null) {
                throw new IllegalStateException("Duplicate AgentType: " + agent.getType());
            }
            log.info("Agent registered: {} -> {}", agent.getType(), agent.getClass().getSimpleName());
        }
        log.info("AgentRegistry initialized with {} agents", agents.size());
    }

    public Agent getAgent(AgentType type) {
        return agents.get(type);
    }

    public Collection<Agent> getAllAgents() {
        return agents.values();
    }

    public boolean contains(AgentType type) {
        return agents.containsKey(type);
    }
}
