package com.aicode.agent.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentExecutionMode;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.dto.AgentExecuteRequest;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.common.Result;
import com.aicode.service.AnalysisRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Agent 控制器 — 框架验证接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final AnalysisRecordService analysisRecordService;

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
     * 批量执行多个 Agent（顺序执行）+ 持久化
     */
    @PostMapping("/execute-batch")
    public Result<List<AgentResult>> executeBatch(@Valid @RequestBody List<AgentExecuteRequest> requests) {
        List<AgentType> types = requests.stream()
                .map(r -> AgentType.valueOf(r.getAgentType()))
                .toList();
        String projectId = requests.get(0).getProjectId();
        AgentContext context = AgentContext.builder()
                .projectId(projectId)
                .reviewId(requests.get(0).getReviewId())
                .build();
        List<AgentResult> results = agentOrchestrator.executeAgents(types, context, AgentExecutionMode.SEQUENTIAL);

        // 异步持久化分析记录
        persistResults(projectId, results);

        return Result.success(results);
    }

    @Async
    void persistResults(String projectId, List<AgentResult> results) {
        try {
            ReviewAgentResult reviewResult = null;
            FixAgentResult fixResult = null;
            SummaryAgentResult summaryResult = null;
            Long totalDuration = 0L;

            for (AgentResult result : results) {
                totalDuration += result.getDuration() != null ? result.getDuration() : 0;
                if (!result.isSuccess()) continue;

                switch (result.getAgentType()) {
                    case REVIEW -> {
                        if (result.getPayload() instanceof ReviewAgentResult r) reviewResult = r;
                    }
                    case FIX -> {
                        if (result.getPayload() instanceof FixAgentResult f) fixResult = f;
                    }
                    case SUMMARY -> {
                        if (result.getPayload() instanceof SummaryAgentResult s) summaryResult = s;
                    }
                }
            }

            boolean allSuccess = results.stream().allMatch(AgentResult::isSuccess);
            String status = allSuccess ? "SUCCESS" : "PARTIAL";
            String errorMessage = allSuccess ? null : "部分Agent执行失败";

            analysisRecordService.saveRecord(
                    Long.parseLong(projectId), null, null,
                    reviewResult, fixResult, summaryResult,
                    totalDuration, status, errorMessage
            );
        } catch (Exception e) {
            log.error("持久化分析记录失败: projectId={}", projectId, e);
        }
    }
}
