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
 * RULE-004: System.out.println / System.err.println 检测
 * <p>
 * 检测代码中是否使用了 System.out.println 或 System.err.println 进行输出。
 * 生产代码应使用日志框架（SLF4J / Logback）而非标准输出。
 */
@Slf4j
@Component
public class Rule004SystemOutPrintln implements RuleChecker {

    @Override
    public List<RuleResult> check(ProjectCodeModel model, CallGraph callGraph) {
        List<RuleResult> results = new ArrayList<>();

        for (ScannedClass clazz : model.getClasses()) {
            for (ScannedMethod method : clazz.getMethods()) {
                if (method.isHasSysOut()) {
                    results.add(RuleResult.builder()
                            .ruleId("RULE-004")
                            .ruleName("System.out.println检测")
                            .description("生产代码应使用日志框架，禁止使用 System.out.println / System.err.println")
                            .severity("WARNING")
                            .passed(false)
                            .className(clazz.getClassName())
                            .methodName(method.getMethodName())
                            .filePath(clazz.getFilePath())
                            .lineNumber(method.getStartLine())
                            .message(String.format("方法 %s.%s() 中使用了 System.out.println 或 System.err.println",
                                    clazz.getClassName(), method.getMethodName()))
                            .suggestion("替换为 SLF4J 日志: log.info() / log.error()，并配置 Logback 输出格式")
                            .contentHash(method.getContentHash())
                            .build());
                }
            }
        }

        log.info("RULE-004 检测完成: 发现 {} 个问题", results.size());
        return results;
    }

    @Override
    public String getRuleId() { return "RULE-004"; }

    @Override
    public String getRuleName() { return "System.out.println检测"; }

    @Override
    public String getDescription() { return "生产代码应使用日志框架，禁止使用 System.out.println / System.err.println"; }
}
