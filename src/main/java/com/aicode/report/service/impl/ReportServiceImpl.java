package com.aicode.report.service.impl;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.ai.service.PromptService;
import com.aicode.report.model.IssueSummary;
import com.aicode.report.model.ReviewReport;
import com.aicode.report.service.ReportService;
import com.aicode.rule.model.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告服务实现
 * <p>
 * 汇总规则检测和 AI 分析结果，生成结构化审查报告。
 * 使用 Spring AI 生成项目总体评价。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptService promptService;

    @Override
    public ReviewReport generateReport(String projectName,
                                        List<RuleResult> ruleResults,
                                        List<AiIssueAnalysis> analyses) {
        // 统计
        int totalRules = ruleResults.size();
        int errorCount = (int) ruleResults.stream().filter(r -> "ERROR".equals(r.getSeverity())).count();
        int warningCount = (int) ruleResults.stream().filter(r -> "WARNING".equals(r.getSeverity())).count();
        int infoCount = (int) ruleResults.stream().filter(r -> "INFO".equals(r.getSeverity())).count();
        int failedRules = (int) ruleResults.stream().filter(r -> !r.isPassed()).count();
        int passedRules = totalRules - failedRules;

        // 构建问题摘要
        List<IssueSummary> issues = buildIssueSummaries(ruleResults, analyses);

        // AI 生成总体评价
        String overallSummary = generateOverallSummary(projectName, issues);

        log.info("报告生成完成: project={}, errors={}, warnings={}, infos={}",
                projectName, errorCount, warningCount, infoCount);

        return ReviewReport.builder()
                .projectName(projectName)
                .scanTime(LocalDateTime.now())
                .totalRules(totalRules)
                .passedRules(passedRules)
                .failedRules(failedRules)
                .errorCount(errorCount)
                .warningCount(warningCount)
                .infoCount(infoCount)
                .issues(issues)
                .overallSummary(overallSummary)
                .build();
    }

    private List<IssueSummary> buildIssueSummaries(List<RuleResult> ruleResults,
                                                    List<AiIssueAnalysis> analyses) {
        List<IssueSummary> summaries = new ArrayList<>();

        for (RuleResult r : ruleResults) {
            // 找到对应的 AI 分析
            AiIssueAnalysis analysis = analyses.stream()
                    .filter(a -> a.getRuleId() != null && a.getRuleId().equals(r.getRuleId())
                            && a.getClassName() != null && a.getClassName().equals(r.getClassName())
                            && a.getMethodName() != null && a.getMethodName().equals(r.getMethodName()))
                    .findFirst()
                    .orElse(null);

            summaries.add(IssueSummary.builder()
                    .ruleId(r.getRuleId())
                    .ruleName(r.getRuleName())
                    .severity(r.getSeverity())
                    .className(r.getClassName())
                    .methodName(r.getMethodName())
                    .filePath(r.getFilePath())
                    .lineNumber(r.getLineNumber())
                    .reason(analysis != null ? analysis.getReason() : r.getMessage())
                    .impact(analysis != null ? analysis.getImpact() : "")
                    .suggestion(analysis != null ? analysis.getSuggestion() : r.getSuggestion())
                    .build());
        }

        return summaries;
    }

    /**
     * 使用 AI 生成项目总体评价
     */
    private String generateOverallSummary(String projectName, List<IssueSummary> issues) {
        try {
            String systemPrompt = promptService.getSystemPrompt();
            String userPrompt = buildSummaryPrompt(projectName, issues);

            return chatClientBuilder.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("AI 生成总体评价失败，使用默认评价", e);
            return buildDefaultSummary(issues);
        }
    }

    private String buildSummaryPrompt(String projectName, List<IssueSummary> issues) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对项目「").append(projectName).append("」的代码审查结果进行总体评价。\n\n");

        long errors = issues.stream().filter(i -> "ERROR".equals(i.getSeverity())).count();
        long warnings = issues.stream().filter(i -> "WARNING".equals(i.getSeverity())).count();

        sb.append("共发现 ").append(issues.size()).append(" 个问题（")
                .append(errors).append(" 个错误，")
                .append(warnings).append(" 个警告）。\n\n");

        sb.append("请从以下维度评价：\n");
        sb.append("1. 项目整体风险评估\n");
        sb.append("2. 架构质量评价\n");
        sb.append("3. 代码规范评价\n");
        sb.append("4. 优先修复建议\n\n");

        sb.append("请直接返回评价文本，不需要 JSON 格式。");

        return sb.toString();
    }

    private String buildDefaultSummary(List<IssueSummary> issues) {
        long errors = issues.stream().filter(i -> "ERROR".equals(i.getSeverity())).count();
        long warnings = issues.stream().filter(i -> "WARNING".equals(i.getSeverity())).count();

        if (issues.isEmpty()) {
            return "代码扫描完成，未发现任何违规问题，代码质量良好。";
        }

        return String.format("""
                        代码审查完成，共发现 %d 个问题（%d 个错误，%d 个警告）。
                        建议优先修复 ERROR 级别的问题，然后逐步处理 WARNING 级别的问题。
                        """,
                issues.size(), errors, warnings);
    }
}
