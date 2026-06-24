package com.aicode.fix.prompt;

import com.aicode.rule.model.RuleResult;
import org.springframework.stereotype.Component;

@Component
public class FixPromptBuilder {

    public String build(RuleResult ruleResult) {
        String sourceCodeSection = "";
        if (ruleResult.getSourceCode() != null && !ruleResult.getSourceCode().isEmpty()) {
            sourceCodeSection = String.format("""
                    当前代码：
                    ```java
                    %s
                    ```
                    """, ruleResult.getSourceCode());
        }

        return String.format("""
                请对以下代码违规问题生成修复方案。

                规则编号: %s
                规则名称: %s
                严重程度: %s
                所在类: %s
                所在方法: %s
                所在文件: %s
                行号: %d
                问题描述: %s
                %s
                请根据上述问题生成修复方案，返回如下 JSON 格式（不要返回其他内容）：
                {
                    "originalCode": "修复前的代码片段",
                    "fixedCode": "修复后的代码片段",
                    "explanation": "修复说明",
                    "confidence": 0.85,
                    "riskLevel": "LOW"
                }
                """,
                ruleResult.getRuleId(),
                ruleResult.getRuleName(),
                ruleResult.getSeverity(),
                ruleResult.getClassName(),
                ruleResult.getMethodName(),
                ruleResult.getFilePath(),
                ruleResult.getLineNumber(),
                ruleResult.getMessage(),
                sourceCodeSection);
    }
}
