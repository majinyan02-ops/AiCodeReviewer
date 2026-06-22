package com.aicode.agent.fix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修复统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixStatistics {

    private Integer totalIssues;

    private Integer fixedIssues;

    private Integer failedIssues;

    private Double successRate;

    private Long totalDuration;

    private Long totalAiDuration;
}
