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
 * RULE-002: 缺少日志记录
 * <p>
 * 检测 Service 和 Controller 的 public 方法中缺少日志记录的情况。
 */
@Slf4j
@Component
public class Rule002MissingLogging implements RuleChecker {

    private static final int MIN_LINE_COUNT = 3; // 低于此行数的方法不检查（getter/setter 等）

    @Override
    public List<RuleResult> check(ProjectCodeModel model, CallGraph callGraph) {
        List<RuleResult> results = new ArrayList<>();

        for (ScannedClass clazz : model.getClasses()) {
            // 只检查 Controller 和 Service
            if (!"Controller".equals(clazz.getClassType())
                    && !"Service".equals(clazz.getClassType())) continue;

            for (ScannedMethod method : clazz.getMethods()) {
                // 跳过简单方法
                if (method.getLineCount() < MIN_LINE_COUNT) continue;
                // 已有日志记录
                if (method.isHasLogging()) continue;

                results.add(RuleResult.builder()
                        .ruleId("RULE-002")
                        .ruleName("缺少日志记录")
                        .description("业务方法应添加日志记录，便于问题追踪和排查")
                        .severity("WARNING")
                        .passed(false)
                        .className(clazz.getClassName())
                        .methodName(method.getMethodName())
                        .filePath(clazz.getFilePath())
                        .lineNumber(method.getStartLine())
                        .message(String.format("方法 %s.%s() 缺少日志记录",
                                clazz.getClassName(), method.getMethodName()))
                        .suggestion("在方法中添加 log.info() 或 log.debug() 记录关键操作")
                        .contentHash(method.getContentHash())
                        .build());
            }
        }

        log.info("RULE-002 检测完成: 发现 {} 个问题", results.size());
        return results;
    }

    @Override
    public String getRuleId() { return "RULE-002"; }

    @Override
    public String getRuleName() { return "缺少日志记录"; }

    @Override
    public String getDescription() { return "业务方法应添加日志记录，便于问题追踪和排查"; }
}
