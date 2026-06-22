package com.aicode.agent.summary.model;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SummaryAgent 执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryAgentResult {

    private SummaryStatistics statistics;

    private ProjectHealthReport healthReport;

    private ReviewAgentResult reviewResult;

    private FixAgentResult fixResult;

    private LocalDateTime generatedTime;
}
