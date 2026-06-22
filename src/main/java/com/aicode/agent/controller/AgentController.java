package com.aicode.agent.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentExecutionMode;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.dto.AgentExecuteRequest;
import com.aicode.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Agent 控制器 — 框架验证接口
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestrator agentOrchestrator;

    /**
     * 执行单个 Agent
     */
    @PostMapping("/execute")
    public Result<AgentResult> execute(@Valid @RequestBody AgentExecuteRequest request) {
        AgentType type = AgentType.valueOf(request.getAgentType());
        AgentContext context = AgentContext.builder()
                .projectId(request.getProjectId())
                .reviewId(request.getReviewId())
                .build();
        AgentResult result = agentOrchestrator.executeAgent(type, context);
        return Result.success(result);
    }

    /**
     * 批量执行多个 Agent（顺序执行）
     */
    @PostMapping("/execute-batch")
    public Result<List<AgentResult>> executeBatch(@Valid @RequestBody List<AgentExecuteRequest> requests) {
        List<AgentType> types = requests.stream()
                .map(r -> AgentType.valueOf(r.getAgentType()))
                .toList();
        AgentContext context = AgentContext.builder()
                .projectId(requests.get(0).getProjectId())
                .reviewId(requests.get(0).getReviewId())
                .build();
        List<AgentResult> results = agentOrchestrator.executeAgents(types, context, AgentExecutionMode.SEQUENTIAL);
        return Result.success(results);
    }
}
