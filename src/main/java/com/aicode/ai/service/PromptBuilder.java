package com.aicode.ai.service;

import com.aicode.rule.model.RuleResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Prompt 构建器 — 将 RuleResult 列表转换为 AI 可理解的 Prompt
 * <p>
 * Task-19 会将 Prompt 模板移到 resources/prompts 目录统一管理。
 */
@Component
public class PromptBuilder {

    /**
     * 构建代码审查的 System Prompt
     */
    public String buildSystemPrompt() {
        return """
                你是一位资深 Java 代码审查专家。
                你的任务是对静态代码分析工具发现的违规问题进行深度分析。

                要求：
                1. 对每个问题评估风险等级（高/中/低）
                2. 解释为什么这是问题
                3. 给出具体的修复建议
                4. 提供修复后的代码示例

                请以 JSON 格式返回，格式如下：
                {
                  "overallScore": 7,
                  "summary": "总体评价...",
                  "issues": [
                    {
                      "ruleId": "RULE-001",
                      "className": "UserServiceImpl",
                      "methodName": "save",
                      "riskLevel": "高",
                      "explanation": "缺少事务注解会导致...",
                      "fixSuggestion": "在方法上添加 @Transactional",
                      "fixedCode": "@Transactional\\npublic void save() { ... }"
                    }
                  ]
                }
                """;
    }

    /**
     * 构建包含规则结果的 User Prompt
     */
    public String buildUserPrompt(List<RuleResult> results) {
        if (results.isEmpty()) {
            return "代码扫描完成，未发现任何违规问题。请给出正面评价。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是对项目进行静态代码分析后发现的违规问题，请逐一进行深度分析：\n\n");

        for (int i = 0; i < results.size(); i++) {
            RuleResult r = results.get(i);
            sb.append("--- 问题 ").append(i + 1).append(" ---\n");
            sb.append("规则编号: ").append(r.getRuleId()).append("\n");
            sb.append("规则名称: ").append(r.getRuleName()).append("\n");
            sb.append("严重程度: ").append(r.getSeverity()).append("\n");
            sb.append("所在类: ").append(r.getClassName()).append("\n");
            sb.append("所在方法: ").append(r.getMethodName()).append("\n");
            sb.append("所在文件: ").append(r.getFilePath()).append("\n");
            sb.append("行号: ").append(r.getLineNumber()).append("\n");
            sb.append("问题描述: ").append(r.getMessage()).append("\n\n");
        }

        sb.append("请对以上 ").append(results.size()).append(" 个问题进行深度分析，并返回 JSON 格式的结果。");
        return sb.toString();
    }
}
