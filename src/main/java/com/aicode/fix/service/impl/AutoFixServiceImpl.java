package com.aicode.fix.service.impl;

import com.aicode.fix.model.FixSuggestion;
import com.aicode.fix.parser.FixResponseParser;
import com.aicode.fix.prompt.FixPromptBuilder;
import com.aicode.fix.service.AutoFixService;
import com.aicode.fix.service.FixCacheService;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoFixServiceImpl implements AutoFixService {

    private final ChatClient.Builder chatClientBuilder;
    private final FixPromptBuilder promptBuilder;
    private final FixResponseParser responseParser;
    private final FixCacheService cacheService;
    private final com.aicode.ai.service.PromptService promptService;

    @Override
    public FixSuggestion generateFix(RuleResult ruleResult) {
        if (ruleResult == null) return null;

        String contentHash = ruleResult.getContentHash();

        log.info("Fix Generate Start: ruleId={}, className={}, method={}",
                ruleResult.getRuleId(), ruleResult.getClassName(), ruleResult.getMethodName());

        // 1. 查缓存
        FixSuggestion cached = cacheService.get(ruleResult.getRuleId(), contentHash);
        if (cached != null) {
            log.info("Cache Hit: ruleId={}", ruleResult.getRuleId());
            return cached;
        }
        log.info("Cache Miss: ruleId={}", ruleResult.getRuleId());

        // 2. 构建 Prompt
        String systemPrompt = promptService.getSystemPrompt();
        String userPrompt = promptBuilder.build(ruleResult);

        // 3. 调用 AI
        try {
            String response = chatClientBuilder.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.debug("AI 原始响应: {}", response);

            // 4. 解析结果
            FixSuggestion suggestion = responseParser.parse(
                    response,
                    ruleResult.getRuleId(),
                    ruleResult.getRuleName(),
                    ruleResult.getSeverity());

            // 5. 写入缓存
            cacheService.put(ruleResult.getRuleId(), contentHash, suggestion);

            log.info("Fix Generate Success: ruleId={}", ruleResult.getRuleId());
            return suggestion;

        } catch (Exception e) {
            log.error("Fix Generate Fail: ruleId={}", ruleResult.getRuleId(), e);
            return fallback(ruleResult);
        }
    }

    private FixSuggestion fallback(RuleResult r) {
        return FixSuggestion.builder()
                .ruleId(r.getRuleId())
                .ruleName(r.getRuleName())
                .severity(r.getSeverity())
                .originalCode("")
                .fixedCode("")
                .explanation("AI 生成修复建议失败")
                .confidence(0.0)
                .riskLevel("HIGH")
                .generatedTime(java.time.LocalDateTime.now())
                .build();
    }
}
