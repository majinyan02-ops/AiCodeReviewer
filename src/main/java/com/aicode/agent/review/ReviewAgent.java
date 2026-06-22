package com.aicode.agent.review;

import com.aicode.agent.Agent;
import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.review.parser.ReviewResponseParser;
import com.aicode.agent.review.prompt.ReviewPromptBuilder;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.rule.RuleEngine;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReviewAgent — 代码审查 Agent
 * <p>
 * 职责：
 * 1. 基于 ProjectCodeModel + CallGraph 执行规则检测
 * 2. 计算评分与风险等级
 * 3. 调用 AI 生成综合分析
 * 4. 封装 ReviewAgentResult
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAgent implements Agent {

    private final RuleEngine ruleEngine;
    private final ChatClient.Builder chatClientBuilder;
    private final ReviewPromptBuilder promptBuilder;
    private final ReviewResponseParser responseParser;
    private final AnalysisCacheService cacheService;

    @Override
    public AgentType getType() {
        return AgentType.REVIEW;
    }

    @Override
    public AgentResult execute(AgentContext context) {
        String projectId = context.getProjectId();

        // 1. 查缓存
        Object cached = cacheService.getReviewResult(projectId);
        if (cached instanceof ReviewAgentResult reviewResult) {
            log.info("ReviewAgent 缓存命中: projectId={}", projectId);
            return AgentResult.success(AgentType.REVIEW, reviewResult);
        }

        // 2. 规则检测（直接消费 ProjectCodeModel + CallGraph，不重新扫描）
        List<RuleResult> ruleResults = ruleEngine.analyze(
                context.getProjectCodeModel(), context.getCallGraph());

        // 3. 统计与评分
        int errors = (int) ruleResults.stream().filter(r -> "ERROR".equals(r.getSeverity())).count();
        int warnings = (int) ruleResults.stream().filter(r -> "WARNING".equals(r.getSeverity())).count();
        int infos = (int) ruleResults.stream().filter(r -> "INFO".equals(r.getSeverity())).count();
        int score = Math.max(0, 100 - errors * 10 - warnings * 5 - infos * 1);

        // 4. AI 分析
        long aiStart = System.currentTimeMillis();
        String summary = "";
        String riskLevel = calculateRiskLevel(score);

        try {
            String prompt = promptBuilder.build(ruleResults);
            String response = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            ReviewResponseParser.ReviewAnalysis analysis = responseParser.parse(response);
            summary = analysis.getSummary();
            riskLevel = analysis.getRiskLevel();
        } catch (Exception e) {
            log.error("ReviewAgent AI 分析失败", e);
        }
        long aiDuration = System.currentTimeMillis() - aiStart;

        // 5. 构建结果
        ReviewAgentResult result = ReviewAgentResult.builder()
                .totalRules(ruleResults.size())
                .errorCount(errors)
                .warningCount(warnings)
                .infoCount(infos)
                .overallScore(score)
                .riskLevel(riskLevel)
                .summary(summary)
                .ruleResults(ruleResults)
                .aiAnalysisDuration(aiDuration)
                .generatedTime(LocalDateTime.now())
                .build();

        // 6. 写入缓存
        cacheService.putReviewResult(projectId, result);

        return AgentResult.success(AgentType.REVIEW, result);
    }

    private String calculateRiskLevel(int score) {
        if (score >= 90) return "LOW";
        if (score >= 70) return "MEDIUM";
        if (score >= 50) return "HIGH";
        return "CRITICAL";
    }
}
