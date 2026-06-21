package com.aicode.rule;

import com.aicode.analysis.model.CallGraph;
import com.aicode.analysis.service.CallGraphService;
import com.aicode.parser.JavaParserService;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则引擎 — 编排所有 RuleChecker 执行检测
 * <p>
 * 输入：源码路径 → 解析为 ProjectCodeModel + CallGraph
 * 输出：所有规则的检测结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngine {

    private final JavaParserService javaParserService;
    private final CallGraphService callGraphService;
    private final List<RuleChecker> ruleCheckers;

    /**
     * 对指定源码目录执行所有规则检测
     *
     * @param projectId  项目 ID
     * @param sourcePath 源码目录
     * @return 所有规则的检测结果
     */
    public List<RuleResult> analyze(Long projectId, String sourcePath) {
        // 1. 解析源码
        ProjectCodeModel model = javaParserService.parseProject(sourcePath);

        // 2. 构建调用图
        CallGraph callGraph = callGraphService.buildFromProjectCodeModel(model, projectId);

        // 3. 执行所有规则
        List<RuleResult> allResults = new ArrayList<>();
        for (RuleChecker checker : ruleCheckers) {
            log.info("执行规则: {} - {}", checker.getRuleId(), checker.getRuleName());
            List<RuleResult> results = checker.check(model, callGraph);
            allResults.addAll(results);
        }

        log.info("规则引擎执行完成: projectId={}, 规则数={}, 发现问题数={}",
                projectId, ruleCheckers.size(), allResults.size());
        return allResults;
    }
}
