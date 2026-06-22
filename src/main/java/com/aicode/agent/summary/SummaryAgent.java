package com.aicode.agent.summary;

import com.aicode.agent.Agent;
import com.aicode.agent.AgentContext;
import com.aicode.agent.AgentResult;
import com.aicode.agent.AgentType;
import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.fix.model.FixStatistics;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.ProjectHealthReport;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.agent.summary.model.SummaryStatistics;
import com.aicode.ai.service.AnalysisCacheService;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SummaryAgent — 项目汇总 Agent
 * <p>
 * 纯聚合职责：仅消费 ReviewAgentResult + FixAgentResult，
 * 禁止调用 RuleEngine / ReviewAgent / FixAgent / JavaParser。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryAgent implements Agent {

    private final AnalysisCacheService cacheService;

    @Override
    public AgentType getType() {
        return AgentType.SUMMARY;
    }

    @Override
    public AgentResult execute(AgentContext context) {
        String projectId = context.getProjectId();

        // 1. 查缓存
        Object cached = cacheService.getSummaryResult(projectId);
        if (cached instanceof SummaryAgentResult summaryResult) {
            log.info("SummaryAgent 缓存命中: projectId={}", projectId);
            return AgentResult.success(AgentType.SUMMARY, summaryResult);
        }

        // 2. 获取 ReviewAgentResult + FixAgentResult
        ReviewAgentResult reviewResult = extractReviewResult(context);
        FixAgentResult fixResult = extractFixResult(context);

        // 3. 汇总统计
        SummaryStatistics statistics = buildStatistics(reviewResult, fixResult);

        // 4. 生成健康度报告
        ProjectHealthReport healthReport = buildHealthReport(reviewResult, fixResult, statistics);

        // 5. 构建结果
        SummaryAgentResult result = SummaryAgentResult.builder()
                .statistics(statistics)
                .healthReport(healthReport)
                .reviewResult(reviewResult)
                .fixResult(fixResult)
                .generatedTime(LocalDateTime.now())
                .build();

        // 6. 写入缓存
        cacheService.putSummaryResult(projectId, result);

        return AgentResult.success(AgentType.SUMMARY, result);
    }

    private ReviewAgentResult extractReviewResult(AgentContext context) {
        Object obj = context.getAttributes().get("reviewResult");
        return obj instanceof ReviewAgentResult r ? r : null;
    }

    private FixAgentResult extractFixResult(AgentContext context) {
        Object obj = context.getAttributes().get("fixResult");
        return obj instanceof FixAgentResult f ? f : null;
    }

    private SummaryStatistics buildStatistics(ReviewAgentResult review, FixAgentResult fix) {
        int errors = review != null ? review.getErrorCount() : 0;
        int warnings = review != null ? review.getWarningCount() : 0;
        int infos = review != null ? review.getInfoCount() : 0;
        int totalIssues = errors + warnings + infos;

        int fixed = fix != null ? fix.getFixedIssues() : 0;
        int failed = fix != null ? fix.getFailedIssues() : 0;
        double successRate = (fixed + failed) > 0 ? (double) fixed / (fixed + failed) : 0.0;

        long reviewDuration = review != null && review.getAiAnalysisDuration() != null
                ? review.getAiAnalysisDuration() : 0L;
        long fixDuration = fix != null && fix.getStatistics() != null
                ? fix.getStatistics().getTotalAiDuration() : 0L;

        return SummaryStatistics.builder()
                .totalIssues(totalIssues)
                .errorCount(errors)
                .warningCount(warnings)
                .infoCount(infos)
                .fixedIssues(fixed)
                .failedIssues(failed)
                .fixSuccessRate(successRate)
                .reviewAiDuration(reviewDuration)
                .fixAiDuration(fixDuration)
                .totalAiDuration(reviewDuration + fixDuration)
                .build();
    }

    private ProjectHealthReport buildHealthReport(
            ReviewAgentResult review, FixAgentResult fix, SummaryStatistics stats) {

        int score = calculateHealthScore(stats);
        String level = healthLevel(score);
        String status = overallStatus(level);

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (stats.getFixedIssues() > 0) {
            strengths.add("已自动修复 " + stats.getFixedIssues() + " 个问题");
        }
        if (stats.getErrorCount() == 0 && stats.getWarningCount() == 0) {
            strengths.add("无严重违规");
        }

        if (stats.getErrorCount() > 0) {
            weaknesses.add("存在 " + stats.getErrorCount() + " 个严重违规 (ERROR)");
            recommendations.add("优先修复 ERROR 级别问题");
        }
        if (stats.getWarningCount() > 0) {
            weaknesses.add("存在 " + stats.getWarningCount() + " 个警告 (WARNING)");
            recommendations.add("关注 WARNING 级别问题，提升代码规范");
        }
        if (stats.getFixSuccessRate() < 0.5 && (stats.getFixedIssues() + stats.getFailedIssues()) > 0) {
            weaknesses.add("修复成功率偏低 (" + String.format("%.0f", stats.getFixSuccessRate() * 100) + "%)");
            recommendations.add("检查修复方案准确性，考虑手动介入");
        }

        List<String> topProblems = extractTopProblems(review);

        String summary = generateSummary(stats, level);

        return ProjectHealthReport.builder()
                .healthLevel(level)
                .healthScore(score)
                .overallStatus(status)
                .summary(summary)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .recommendations(recommendations)
                .topProblems(topProblems)
                .build();
    }

    private int calculateHealthScore(SummaryStatistics stats) {
        int score = 100;
        score -= stats.getErrorCount() * 10;
        score -= stats.getWarningCount() * 5;
        score -= stats.getInfoCount() * 1;
        score += stats.getFixedIssues() * 2;
        return Math.max(0, Math.min(100, score));
    }

    private String healthLevel(int score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "GOOD";
        if (score >= 60) return "FAIR";
        if (score >= 40) return "POOR";
        return "CRITICAL";
    }

    private String overallStatus(String healthLevel) {
        return switch (healthLevel) {
            case "EXCELLENT", "GOOD" -> "HEALTHY";
            case "FAIR" -> "ATTENTION";
            case "POOR" -> "AT_RISK";
            default -> "CRITICAL";
        };
    }

    private List<String> extractTopProblems(ReviewAgentResult review) {
        if (review == null || review.getRuleResults() == null) return List.of();

        Map<String, Long> ruleCounts = review.getRuleResults().stream()
                .filter(r -> !r.isPassed())
                .collect(Collectors.groupingBy(RuleResult::getRuleName, Collectors.counting()));

        return ruleCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + "次)")
                .toList();
    }

    private String generateSummary(SummaryStatistics stats, String level) {
        return switch (level) {
            case "EXCELLENT" -> "项目代码质量优秀，无明显问题";
            case "GOOD" -> "项目整体质量良好，存在少量需关注的问题";
            case "FAIR" -> "项目存在一些问题，建议进行修复";
            case "POOR" -> "项目问题较多，建议尽快修复";
            default -> "项目存在严重问题，需要立即处理";
        };
    }
}
