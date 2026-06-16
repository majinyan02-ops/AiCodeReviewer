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
 * RULE-001: Service 方法缺少 @Transactional
 * <p>
 * 检测 Service 中执行写操作（insert/update/delete）但缺少事务注解的方法。
 */
@Slf4j
@Component
public class Rule001MissingTransactional implements RuleChecker {

    private static final int WRITE_THRESHOLD = 3; // 方法行数 >= 3 行且调用 Mapper 才检查

    @Override
    public List<RuleResult> check(ProjectCodeModel model, CallGraph callGraph) {
        List<RuleResult> results = new ArrayList<>();

        for (ScannedClass clazz : model.getClasses()) {
            if (!"Service".equals(clazz.getClassType())) continue;

            // 类级是否有 @Transactional
            boolean classHasTransactional = clazz.getAnnotations().stream()
                    .anyMatch(a -> "Transactional".equals(a.getName())
                            || (a.getQualifiedName() != null && a.getQualifiedName().endsWith(".Transactional")));

            for (ScannedMethod method : clazz.getMethods()) {
                // 已有事务（类级或方法级），跳过
                if (classHasTransactional || method.isHasTransactional()) continue;

                // 有 Mapper 调用且非简单 getter/setter
                if (!method.getMapperCalls().isEmpty() && method.getLineCount() >= WRITE_THRESHOLD) {
                    results.add(RuleResult.builder()
                            .ruleId("RULE-001")
                            .ruleName("缺少@Transactional")
                            .description("Service 中执行数据写操作的方法应添加 @Transactional 注解")
                            .severity("ERROR")
                            .passed(false)
                            .className(clazz.getClassName())
                            .methodName(method.getMethodName())
                            .filePath(clazz.getFilePath())
                            .lineNumber(method.getStartLine())
                            .message(String.format("方法 %s.%s() 调用了 Mapper 但缺少 @Transactional 注解",
                                    clazz.getClassName(), method.getMethodName()))
                            .suggestion("在方法或类上添加 @Transactional 注解，确保数据操作的原子性")
                            .build());
                }
            }
        }

        log.info("RULE-001 检测完成: 发现 {} 个问题", results.size());
        return results;
    }

    @Override
    public String getRuleId() { return "RULE-001"; }

    @Override
    public String getRuleName() { return "缺少@Transactional"; }

    @Override
    public String getDescription() { return "Service 中执行数据写操作的方法应添加 @Transactional 注解"; }
}
