package com.aicode.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码审查报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReport {

    /** 项目名称 */
    private String projectName;

    /** 扫描时间 */
    private LocalDateTime scanTime;

    /** 规则总数 */
    private int totalRules;

    /** 通过的规则数 */
    private int passedRules;

    /** 失败的规则数 */
    private int failedRules;

    /** ERROR 级别问题数 */
    private int errorCount;

    /** WARNING 级别问题数 */
    private int warningCount;

    /** INFO 级别问题数 */
    private int infoCount;

    /** 问题列表 */
    @Builder.Default
    private List<IssueSummary> issues = new ArrayList<>();

    /** 项目总体评价（AI 生成） */
    private String overallSummary;
}
