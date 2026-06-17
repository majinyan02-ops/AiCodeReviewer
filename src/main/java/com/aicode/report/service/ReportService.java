package com.aicode.report.service;

import com.aicode.ai.model.AiIssueAnalysis;
import com.aicode.report.model.ReviewReport;
import com.aicode.rule.model.RuleResult;

import java.util.List;

/**
 * 报告服务接口
 * <p>
 * 将规则检测结果和 AI 分析结果汇总生成审查报告。
 */
public interface ReportService {

    /**
     * 生成代码审查报告
     *
     * @param projectName 项目名称
     * @param ruleResults 规则检测结果
     * @param analyses    AI 分析结果
     * @return 审查报告
     */
    ReviewReport generateReport(String projectName,
                                 List<RuleResult> ruleResults,
                                 List<AiIssueAnalysis> analyses);
}
