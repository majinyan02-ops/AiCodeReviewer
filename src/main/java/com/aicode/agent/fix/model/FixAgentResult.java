package com.aicode.agent.fix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FixAgent 执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixAgentResult {

    private Integer totalIssues;

    private Integer fixedIssues;

    private Integer failedIssues;

    private List<FixItem> fixItems;

    private FixStatistics statistics;

    private LocalDateTime generatedTime;
}
