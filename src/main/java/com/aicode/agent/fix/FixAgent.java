package com.aicode.agent.fix;

import com.aicode.agent.Agent;
import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.fix.model.FixItem;
import com.aicode.agent.fix.model.FixStatistics;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.fix.model.FixSuggestion;
import com.aicode.fix.service.AutoFixService;
import com.aicode.patch.model.PatchResult;
import com.aicode.patch.service.PatchService;
import com.aicode.patch.source.SourceCodeReader;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FixAgent — 代码修复 Agent
 * <p>
 * 职责：
 * 1. 从 ReviewAgentResult 获取待修复规则
 * 2. 逐条调用 AutoFixService 生成修复建议
 * 3. 调用 PatchService 生成补丁
 * 4. 汇总 FixAgentResult
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FixAgent implements Agent {

    private final AutoFixService autoFixService;
    private final PatchService patchService;
    private final AnalysisCacheService cacheService;
    private final SourceCodeReader sourceCodeReader;

    @Override
    public AgentType getType() {
        return AgentType.FIX;
    }

    @Override
    public AgentResult execute(AgentContext context) {
        String projectId = context.getProjectId();

        // 1. 查缓存
        Object cached = cacheService.getFixResult(projectId);
        if (cached instanceof FixAgentResult fixResult) {
            log.info("FixAgent 缓存命中: projectId={}", projectId);
            return AgentResult.success(AgentType.FIX, fixResult);
        }

        // 2. 获取 ReviewAgentResult
        Object reviewObj = context.getAttributes().get("reviewResult");
        if (!(reviewObj instanceof ReviewAgentResult reviewResult)) {
            return AgentResult.failure(AgentType.FIX, "ReviewAgentResult not found in context");
        }

        // 3. 提取待修复规则
        List<RuleResult> ruleResults = reviewResult.getRuleResults();
        if (ruleResults == null || ruleResults.isEmpty()) {
            return AgentResult.success(AgentType.FIX, buildEmptyResult());
        }

        // 4. 逐条执行修复
        List<FixItem> fixItems = new ArrayList<>();
        int fixedCount = 0;
        int failedCount = 0;
        long totalAiDuration = 0;

        for (RuleResult ruleResult : ruleResults) {
            if (ruleResult.isPassed()) continue;

            // 填充源码片段
            if (ruleResult.getSourceCode() == null || ruleResult.getSourceCode().isEmpty()) {
                String sourceCode = sourceCodeReader.readMethodSource(
                        ruleResult.getFilePath(),
                        ruleResult.getMethodName(),
                        ruleResult.getLineNumber());
                ruleResult.setSourceCode(sourceCode);
            }

            long aiStart = System.currentTimeMillis();
            FixItem item = processRuleResult(ruleResult);
            long aiDuration = System.currentTimeMillis() - aiStart;
            totalAiDuration += aiDuration;

            item.setGenerateDuration(aiDuration);
            fixItems.add(item);

            if (item.isPatchGenerated()) {
                fixedCount++;
            } else {
                failedCount++;
            }
        }

        // 5. 构建统计
        int total = fixItems.size();
        FixStatistics statistics = FixStatistics.builder()
                .totalIssues(total)
                .fixedIssues(fixedCount)
                .failedIssues(failedCount)
                .successRate(total > 0 ? (double) fixedCount / total : 0.0)
                .totalDuration(totalAiDuration)
                .totalAiDuration(totalAiDuration)
                .build();

        // 6. 构建结果
        FixAgentResult result = FixAgentResult.builder()
                .totalIssues(total)
                .fixedIssues(fixedCount)
                .failedIssues(failedCount)
                .fixItems(fixItems)
                .statistics(statistics)
                .generatedTime(LocalDateTime.now())
                .build();

        // 7. 写入缓存
        cacheService.putFixResult(projectId, result);

        return AgentResult.success(AgentType.FIX, result);
    }

    /**
     * 处理单条规则结果：生成修复建议 + 补丁
     */
    private FixItem processRuleResult(RuleResult ruleResult) {
        try {
            // 调用 AutoFixService 生成修复建议
            FixSuggestion suggestion = autoFixService.generateFix(ruleResult);

            // 调用 PatchService 生成补丁
            PatchResult patchResult = patchService.generatePatch(suggestion);

            return FixItem.builder()
                    .ruleId(ruleResult.getRuleId())
                    .className(ruleResult.getClassName())
                    .methodName(ruleResult.getMethodName())
                    .severity(ruleResult.getSeverity())
                    .issue(ruleResult.getMessage())
                    .suggestion(suggestion != null ? suggestion.getExplanation() : "")
                    .patchContent(patchResult != null ? patchResult.getPatchContent() : "")
                    .patchGenerated(patchResult != null && patchResult.isValid())
                    .build();

        } catch (Exception e) {
            log.error("FixItem 处理失败: ruleId={}", ruleResult.getRuleId(), e);
            return FixItem.builder()
                    .ruleId(ruleResult.getRuleId())
                    .className(ruleResult.getClassName())
                    .methodName(ruleResult.getMethodName())
                    .severity(ruleResult.getSeverity())
                    .issue(ruleResult.getMessage())
                    .suggestion("")
                    .patchContent("")
                    .patchGenerated(false)
                    .build();
        }
    }

    private FixAgentResult buildEmptyResult() {
        return FixAgentResult.builder()
                .totalIssues(0)
                .fixedIssues(0)
                .failedIssues(0)
                .fixItems(List.of())
                .statistics(FixStatistics.builder()
                        .totalIssues(0)
                        .fixedIssues(0)
                        .failedIssues(0)
                        .successRate(0.0)
                        .totalDuration(0L)
                        .totalAiDuration(0L)
                        .build())
                .generatedTime(LocalDateTime.now())
                .build();
    }
}
