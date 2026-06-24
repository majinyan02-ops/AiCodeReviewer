package com.aicode.agent.review.controller;

import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentOrchestrator;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.analysis.model.CallGraph;
import com.aicode.analysis.service.CallGraphService;
import com.aicode.common.Result;
import com.aicode.parser.JavaParserService;
import com.aicode.parser.model.ProjectCodeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ReviewAgent 控制器
 */
@RestController
@RequestMapping("/api/agent/review")
@RequiredArgsConstructor
public class ReviewAgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final JavaParserService javaParserService;
    private final CallGraphService callGraphService;

    @PostMapping
    public Result<AgentResult> review(@RequestBody Map<String, String> request) {
        String projectId = request.get("projectId");
        Long projectIdLong = Long.parseLong(projectId);

        // 解析源码 + 构建调用图
        String sourcePath = "./git-repos/" + projectId;
        ProjectCodeModel model = javaParserService.parseProject(sourcePath);
        CallGraph callGraph = callGraphService.buildFromProjectCodeModel(model, projectIdLong);

        AgentContext context = AgentContext.builder()
                .projectId(projectId)
                .projectCodeModel(model)
                .callGraph(callGraph)
                .build();
        AgentResult result = agentOrchestrator.executeAgent(AgentType.REVIEW, context);
        return Result.success(result);
    }
}
