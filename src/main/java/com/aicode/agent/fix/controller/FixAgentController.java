package com.aicode.agent.fix.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * FixAgent 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/fix")
@RequiredArgsConstructor
public class FixAgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final AnalysisCacheService cacheService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public Result<AgentResult> fix(@RequestBody Map<String, String> request) {
        String projectId = request.get("projectId");

        // 从缓存获取 ReviewAgentResult
        Object reviewObj = cacheService.getReviewResult(projectId);
        Map<String, Object> attributes = new HashMap<>();
        ReviewAgentResult reviewResult = convertTo(reviewObj, ReviewAgentResult.class);
        if (reviewResult != null) {
            attributes.put("reviewResult", reviewResult);
        } else {
            log.warn("FixAgentController: ReviewAgentResult NOT found for projectId={}", projectId);
        }

        AgentContext context = AgentContext.builder()
                .projectId(projectId)
                .attributes(attributes)
                .build();
        AgentResult result = agentOrchestrator.executeAgent(AgentType.FIX, context);
        return Result.success(result);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertTo(Object obj, Class<T> clazz) {
        if (obj == null) return null;
        if (clazz.isInstance(obj)) return clazz.cast(obj);
        return objectMapper.convertValue(obj, clazz);
    }
}
