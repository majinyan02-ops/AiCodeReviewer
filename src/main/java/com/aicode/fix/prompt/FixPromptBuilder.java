package com.aicode.fix.prompt;

import com.aicode.rule.model.RuleResult;
import org.springframework.stereotype.Component;

@Component
public class FixPromptBuilder {

    public String build(RuleResult ruleResult) {
        return String.format("""
                请对以下代码违规问题生成修复方案：

                规则编号: %s
                规则名称: %s
                严重程度: %s
                所在类: %s
                所在方法: %s
                所在文件: %s
                行号: %d
                问题描述: %s

                请根据上述问题生成修复后的代码，并返回 JSON 格式的修复建议。
                """,
                ruleResult.getRuleId(),
                ruleResult.getRuleName(),
                ruleResult.getSeverity(),
                ruleResult.getClassName(),
                ruleResult.getMethodName(),
                ruleResult.getFilePath(),
                ruleResult.getLineNumber(),
                ruleResult.getMessage());
    }
}
