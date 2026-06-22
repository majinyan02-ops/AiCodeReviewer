package com.aicode.agent.review.model;

import com.aicode.rule.model.RuleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReviewAgent 执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAgentResult {

    private Integer totalRules;

    private Integer errorCount;

    private Integer warningCount;

    private Integer infoCount;

    private Integer overallScore;

    private String riskLevel;

    private String summary;

    private List<RuleResult> ruleResults;

    private Long aiAnalysisDuration;

    private LocalDateTime generatedTime;
}
