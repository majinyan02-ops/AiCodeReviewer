package com.aicode.agent.summary.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * SummaryAgent 控制器
 */
@RestController
@RequestMapping("/api/agent/summary")
@RequiredArgsConstructor
public class SummaryAgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final AnalysisCacheService cacheService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public Result<AgentResult> summary(@RequestBody Map<String, String> request) {
        String projectId = request.get("projectId");

        // 从缓存获取 ReviewAgentResult + FixAgentResult
        Map<String, Object> attributes = new HashMap<>();
        ReviewAgentResult reviewResult = convertTo(cacheService.getReviewResult(projectId), ReviewAgentResult.class);
        if (reviewResult != null) {
            attributes.put("reviewResult", reviewResult);
        }
        FixAgentResult fixResult = convertTo(cacheService.getFixResult(projectId), FixAgentResult.class);
        if (fixResult != null) {
            attributes.put("fixResult", fixResult);
        }

        AgentContext context = AgentContext.builder()
                .projectId(projectId)
                .attributes(attributes)
                .build();
        AgentResult result = agentOrchestrator.executeAgent(AgentType.SUMMARY, context);
        return Result.success(result);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertTo(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        if (clazz.isInstance(obj)) return clazz.cast(obj);
        return objectMapper.convertValue(obj, clazz);
    }
}
