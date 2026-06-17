package com.aicode.report.generator;

import com.aicode.report.model.IssueSummary;
import com.aicode.report.model.ReviewReport;

/**
 * Markdown 报告生成器
 * <p>
 * 将 ReviewReport 渲染为 Markdown 格式文本。
 */
public interface MarkdownReportGenerator {

    /**
     * 生成 Markdown 格式报告
     *
     * @param report 审查报告
     * @return Markdown 文本
     */
    String generate(ReviewReport report);
}
