package com.aicode.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问题摘要 — 报告中的单个问题项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueSummary {

    /** 规则编号 */
    private String ruleId;

    /** 规则名称 */
    private String ruleName;

    /** 严重程度: ERROR / WARNING / INFO */
    private String severity;

    /** 所在类名 */
    private String className;

    /** 所在方法名 */
    private String methodName;

    /** 所在文件路径 */
    private String filePath;

    /** 行号 */
    private int lineNumber;

    /** 问题原因 */
    private String reason;

    /** 影响分析 */
    private String impact;

    /** 修复建议 */
    private String suggestion;
}
