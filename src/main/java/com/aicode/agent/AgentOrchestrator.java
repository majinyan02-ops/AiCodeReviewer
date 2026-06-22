package com.aicode.agent;

import com.aicode.agent.registry.AgentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 调度器 — 统一调度 Agent 执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final AgentRegistry agentRegistry;

    /**
     * 单 Agent 执行
     */
    public AgentResult executeAgent(AgentType type, AgentContext context) {
        Agent agent = agentRegistry.getAgent(type);
        if (agent == null) {
            return AgentResult.failure(type, "Agent not found: " + type);
        }

        LocalDateTime startTime = LocalDateTime.now();
        try {
            AgentResult result = agent.execute(context);
            result.setStartTime(startTime);
            result.setEndTime(LocalDateTime.now());
            result.setDuration(java.time.Duration.between(startTime, result.getEndTime()).toMillis());
            log.info("Agent {} executed in {}ms", type, result.getDuration());
            return result;
        } catch (Exception e) {
            log.error("Agent {} execution failed", type, e);
            return AgentResult.failure(type, "Agent execution failed: " + e.getMessage());
        }
    }

    /**
     * 多 Agent 顺序执行
     */
    public List<AgentResult> executeAgents(List<AgentType> types, AgentContext context, AgentExecutionMode mode) {
        if (mode == AgentExecutionMode.PARALLEL) {
            throw new UnsupportedOperationException("PARALLEL mode is not yet implemented");
        }

        List<AgentResult> results = new ArrayList<>();
        for (AgentType type : types) {
            AgentResult result = executeAgent(type, context);
            results.add(result);
            if (!result.isSuccess()) {
                log.warn("Agent {} failed, stopping sequence", type);
                break;
            }
        }
        return results;
    }
}
