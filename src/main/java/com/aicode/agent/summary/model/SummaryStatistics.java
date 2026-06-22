package com.aicode.agent.summary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 汇总统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryStatistics {

    private Integer totalIssues;

    private Integer errorCount;

    private Integer warningCount;

    private Integer infoCount;

    private Integer fixedIssues;

    private Integer failedIssues;

    private Double fixSuccessRate;

    private Long reviewAiDuration;

    private Long fixAiDuration;

    private Long totalAiDuration;
}
