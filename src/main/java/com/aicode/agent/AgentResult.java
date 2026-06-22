package com.aicode.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一 Agent 输出
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {

    private AgentType agentType;

    private boolean success;

    private String message;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration;

    private Object payload;

    public static AgentResult success(AgentType agentType, Object payload) {
        LocalDateTime now = LocalDateTime.now();
        return AgentResult.builder()
                .agentType(agentType)
                .success(true)
                .message("success")
                .startTime(now)
                .endTime(now)
                .duration(0L)
                .payload(payload)
                .build();
    }

    public static AgentResult success(AgentType agentType, String message, Object payload) {
        LocalDateTime now = LocalDateTime.now();
        return AgentResult.builder()
                .agentType(agentType)
                .success(true)
                .message(message)
                .startTime(now)
                .endTime(now)
                .duration(0L)
                .payload(payload)
                .build();
    }

    public static AgentResult failure(AgentType agentType, String message) {
        LocalDateTime now = LocalDateTime.now();
        return AgentResult.builder()
                .agentType(agentType)
                .success(false)
                .message(message)
                .startTime(now)
                .endTime(now)
                .duration(0L)
                .build();
    }
}
