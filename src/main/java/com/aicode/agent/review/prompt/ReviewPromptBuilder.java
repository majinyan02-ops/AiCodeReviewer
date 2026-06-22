package com.aicode.agent.review.prompt;

import com.aicode.rule.model.RuleResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Review Agent Prompt 构建器
 * <p>
 * 构建 AI 分析所需的结构化 Prompt，禁止自由输出 Markdown。
 */
@Component
public class ReviewPromptBuilder {

    public String build(List<RuleResult> ruleResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下代码审查结果进行综合分析，返回 JSON 格式。\n\n");
        sb.append("规则检测结果：\n");

        for (RuleResult r : ruleResults) {
            sb.append(String.format("""
                    ---
                    规则编号: %s
                    规则名称: %s
                    严重程度: %s
                    所在类: %s
                    所在方法: %s
                    问题描述: %s
                    """,
                    r.getRuleId(),
                    r.getRuleName(),
                    r.getSeverity(),
                    r.getClassName(),
                    r.getMethodName(),
                    r.getMessage()));
        }

        sb.append("""
                ---

                请根据上述规则检测结果，返回如下 JSON 格式（禁止输出其他内容）：
                {
                  "summary": "对项目代码质量的简要总结（50字以内）",
                  "riskLevel": "LOW 或 MEDIUM 或 HIGH 或 CRITICAL"
                }
                """);

        return sb.toString();
    }
}
