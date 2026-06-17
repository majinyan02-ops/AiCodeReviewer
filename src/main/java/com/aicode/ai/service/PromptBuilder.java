package com.aicode.ai.service;

import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Prompt 构建器 — 委托 PromptService 加载模板
 */
@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final PromptService promptService;

    /**
     * 构建代码审查的 System Prompt
     */
    public String buildSystemPrompt() {
        return promptService.getSystemPrompt();
    }

    /**
     * 构建单条 User Prompt
     */
    public String buildUserPrompt(RuleResult result) {
        return promptService.buildUserPrompt(result);
    }

    /**
     * 构建批量 User Prompt
     */
    public String buildUserPrompt(List<RuleResult> results) {
        if (results.isEmpty()) {
            return "代码扫描完成，未发现任何违规问题。请给出正面评价。";
        }
        return promptService.buildBatchUserPrompt(results);
    }
}
