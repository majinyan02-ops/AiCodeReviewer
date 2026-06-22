package com.aicode.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Agent 执行请求 DTO
 */
@Data
public class AgentExecuteRequest {

    @NotBlank(message = "agentType 不能为空")
    private String agentType;

    private String projectId;

    private Long reviewId;
}
