package com.aicode.rule.impl;

import com.aicode.analysis.model.CallGraph;
import com.aicode.parser.model.ProjectCodeModel;
import com.aicode.parser.model.ScannedClass;
import com.aicode.parser.model.ScannedMethod;
import com.aicode.rule.RuleChecker;
import com.aicode.rule.model.RuleResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RULE-005: 超长方法检测
 * <p>
 * 检测行数超过阈值的方法，建议拆分以保持可读性和可维护性。
 */
@Slf4j
@Component
public class Rule005LongMethod implements RuleChecker {

    private static final int MAX_LINE_COUNT = 50; // 方法行数阈值

    @Override
    public List<RuleResult> check(ProjectCodeModel model, CallGraph callGraph) {
        List<RuleResult> results = new ArrayList<>();

        for (ScannedClass clazz : model.getClasses()) {
            for (ScannedMethod method : clazz.getMethods()) {
                if (method.getLineCount() > MAX_LINE_COUNT) {
                    String severity = method.getLineCount() > 100 ? "ERROR" : "WARNING";

                    results.add(RuleResult.builder()
                            .ruleId("RULE-005")
                            .ruleName("超长方法检测")
                            .description(String.format("方法行数不应超过 %d 行，过长的应拆分", MAX_LINE_COUNT))
                            .severity(severity)
                            .passed(false)
                            .className(clazz.getClassName())
                            .methodName(method.getMethodName())
                            .filePath(clazz.getFilePath())
                            .lineNumber(method.getStartLine())
                            .message(String.format("方法 %s.%s() 共 %d 行，超过 %d 行阈值",
                                    clazz.getClassName(), method.getMethodName(),
                                    method.getLineCount(), MAX_LINE_COUNT))
                            .suggestion("将方法拆分为多个职责单一的小方法，每个方法不超过 " + MAX_LINE_COUNT + " 行")
                            .build());
                }
            }
        }

        log.info("RULE-005 检测完成: 发现 {} 个问题", results.size());
        return results;
    }

    @Override
    public String getRuleId() { return "RULE-005"; }

    @Override
    public String getRuleName() { return "超长方法检测"; }

    @Override
    public String getDescription() { return "方法行数不应超过 " + MAX_LINE_COUNT + " 行，过长的应拆分"; }
}
