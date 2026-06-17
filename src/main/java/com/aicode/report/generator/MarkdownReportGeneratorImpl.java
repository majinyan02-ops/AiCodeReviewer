package com.aicode.report.generator;

import com.aicode.report.model.IssueSummary;
import com.aicode.report.model.ReviewReport;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Markdown 报告生成器实现
 */
@Component
public class MarkdownReportGeneratorImpl implements MarkdownReportGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String generate(ReviewReport report) {
        StringBuilder md = new StringBuilder();

        // 标题
        md.append("# 代码审查报告\n\n");
        md.append("**项目名称**: ").append(report.getProjectName()).append("\n\n");
        md.append("**扫描时间**: ").append(report.getScanTime() != null
                ? report.getScanTime().format(FMT) : "-").append("\n\n");
        md.append("---\n\n");

        // 问题统计
        md.append("## 问题统计\n\n");
        md.append("| 指标 | 数量 |\n");
        md.append("|------|------|\n");
        md.append("| 规则总数 | ").append(report.getTotalRules()).append(" |\n");
        md.append("| 通过 | ").append(report.getPassedRules()).append(" |\n");
        md.append("| 失败 | ").append(report.getFailedRules()).append(" |\n");
        md.append("| ERROR | ").append(report.getErrorCount()).append(" |\n");
        md.append("| WARNING | ").append(report.getWarningCount()).append(" |\n");
        md.append("| INFO | ").append(report.getInfoCount()).append(" |\n");
        md.append("\n---\n\n");

        // 统计图表
        md.append("## 严重程度分布\n\n");
        md.append("```\n");
        md.append("ERROR:   ").append(bar(report.getErrorCount(), report.getTotalRules())).append("\n");
        md.append("WARNING: ").append(bar(report.getWarningCount(), report.getTotalRules())).append("\n");
        md.append("INFO:    ").append(bar(report.getInfoCount(), report.getTotalRules())).append("\n");
        md.append("```\n\n");
        md.append("---\n\n");

        // 问题详情
        md.append("## 问题详情\n\n");

        List<IssueSummary> issues = report.getIssues();
        if (issues.isEmpty()) {
            md.append("✅ 未发现任何问题，代码质量良好。\n\n");
        } else {
            // 按严重程度排序
            List<IssueSummary> sorted = issues.stream()
                    .sorted((a, b) -> severityOrder(b.getSeverity()) - severityOrder(a.getSeverity()))
                    .toList();

            for (int i = 0; i < sorted.size(); i++) {
                IssueSummary issue = sorted.get(i);
                String icon = "ERROR".equals(issue.getSeverity()) ? "🔴" :
                        "WARNING".equals(issue.getSeverity()) ? "🟡" : "🔵";

                md.append("### ").append(i + 1).append(". ").append(icon).append(" ")
                        .append(issue.getRuleId()).append(": ").append(issue.getRuleName()).append("\n\n");

                md.append("| 属性 | 值 |\n");
                md.append("|------|------|\n");
                md.append("| 严重程度 | **").append(issue.getSeverity()).append("** |\n");
                md.append("| 所在类 | `").append(issue.getClassName()).append("` |\n");
                md.append("| 所在方法 | `").append(issue.getMethodName()).append("()` |\n");
                md.append("| 文件 | ").append(issue.getFilePath()).append(" |\n");
                md.append("| 行号 | ").append(issue.getLineNumber()).append(" |\n\n");

                if (issue.getReason() != null && !issue.getReason().isEmpty()) {
                    md.append("**问题原因**: ").append(issue.getReason()).append("\n\n");
                }
                if (issue.getImpact() != null && !issue.getImpact().isEmpty()) {
                    md.append("**影响分析**: ").append(issue.getImpact()).append("\n\n");
                }
                if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                    md.append("**修复建议**: ").append(issue.getSuggestion()).append("\n\n");
                }

                md.append("---\n\n");
            }
        }

        // AI 总体建议
        md.append("## AI 总体建议\n\n");
        if (report.getOverallSummary() != null && !report.getOverallSummary().isEmpty()) {
            md.append(report.getOverallSummary()).append("\n\n");
        } else {
            md.append("无\n\n");
        }
        md.append("---\n\n");

        // 页脚
        md.append("*报告由 AI Code Reviewer 自动生成*\n");

        return md.toString();
    }

    private String bar(int count, int total) {
        if (total == 0) return "";
        int len = Math.max(1, count * 20 / total);
        return "#".repeat(len) + " " + count;
    }

    private int severityOrder(String severity) {
        return switch (severity) {
            case "ERROR" -> 3;
            case "WARNING" -> 2;
            case "INFO" -> 1;
            default -> 0;
        };
    }
}
