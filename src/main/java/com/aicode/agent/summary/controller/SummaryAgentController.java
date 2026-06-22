package com.aicode.agent.summary.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SummaryAgent 控制器
 */
@RestController
@RequestMapping("/api/agent/summary")
@RequiredArgsConstructor
public class SummaryAgentController {

    private final AgentOrchestrator agentOrchestrator;

    @PostMapping
    public Result<AgentResult> summary(@RequestBody Map<String, String> request) {
        String projectId = request.get("projectId");
        AgentContext context = AgentContext.builder()
                .projectId(projectId)
                .build();
        AgentResult result = agentOrchestrator.executeAgent(AgentType.SUMMARY, context);
        return Result.success(result);
    }
}
