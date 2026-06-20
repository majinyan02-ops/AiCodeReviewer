package com.aicode.rule.impl;

import com.aicode.analysis.model.CallEdge;
import com.aicode.analysis.model.CallGraph;
import com.aicode.analysis.model.CallNode;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScannedClass;
import com.aicode.parser.model.ScannedMethod;
import com.aicode.rule.RuleChecker;
import com.aicode.rule.model.RuleResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RULE-003: Controller 直接访问 Mapper
 * <p>
 * 基于 CallGraph 检测 Controller 方法是否跳过了 Service 层直接调用 Mapper。
 * 标准调用链应为: Controller → Service → Mapper
 */
@Slf4j
@Component
public class Rule003ControllerDirectMapper implements RuleChecker {

    @Override
    public List<RuleResult> check(ProjectCodeModel model, CallGraph callGraph) {
        List<RuleResult> results = new ArrayList<>();

        if (callGraph == null || callGraph.getEdges() == null) {
            log.info("RULE-003: CallGraph 为空，跳过检测");
            return results;
        }

        // 构建方法哈希索引: className+methodName → contentHash
        Map<String, String> hashIndex = new HashMap<>();
        for (ScannedClass clazz : model.getClasses()) {
            for (ScannedMethod method : clazz.getMethods()) {
                hashIndex.put(clazz.getClassName() + "." + method.getMethodName(),
                        method.getContentHash());
            }
        }

        for (CallEdge edge : callGraph.getEdges()) {
            if (!"MAPPER_CALL".equals(edge.getCallType())) continue;

            CallNode callerNode = callGraph.getNodes().get(edge.getCallerId());
            if (callerNode == null) continue;

            if ("Controller".equals(callerNode.getClassType())) {
                String key = callerNode.getClassName() + "." + callerNode.getMethodName();
                results.add(RuleResult.builder()
                        .ruleId("RULE-003")
                        .ruleName("Controller直接访问Mapper")
                        .description("Controller 不应直接调用 Mapper，应通过 Service 层访问")
                        .severity("ERROR")
                        .passed(false)
                        .className(callerNode.getClassName())
                        .methodName(callerNode.getMethodName())
                        .filePath(callerNode.getFilePath())
                        .lineNumber(edge.getLineNumber())
                        .message(String.format("Controller %s.%s() 直接调用了 Mapper %s.%s()，违反了分层架构",
                                edge.getCallerClassName(), edge.getCallerMethodName(),
                                edge.getCalleeClassName(), edge.getCalleeMethodName()))
                        .suggestion("将 Mapper 调用移到对应的 Service 类中，Controller 通过 Service 访问数据层")
                        .contentHash(hashIndex.getOrDefault(key, ""))
                        .build());
            }
        }

        log.info("RULE-003 检测完成: 发现 {} 个问题", results.size());
        return results;
    }

    @Override
    public String getRuleId() { return "RULE-003"; }

    @Override
    public String getRuleName() { return "Controller直接访问Mapper"; }

    @Override
    public String getDescription() { return "Controller 不应直接调用 Mapper，应通过 Service 层访问"; }
}
