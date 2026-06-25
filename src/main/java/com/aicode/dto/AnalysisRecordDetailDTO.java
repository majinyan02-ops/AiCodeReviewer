package com.aicode.dto;

import com.aicode.agent.summary.model.ProjectHealthReport;
import com.aicode.agent.summary.model.SummaryStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分析记录详情 DTO（含反序列化后的JSON字段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecordDetailDTO {

    private Long id;

    private Long projectId;

    private String projectName;

    private Long taskId;

    private Integer overallScore;

    private String riskLevel;

    private String healthLevel;

    private Integer healthScore;

    private Integer totalIssues;

    private Integer errorCount;

    private Integer warningCount;

    private Integer infoCount;

    private Integer fixedIssues;

    private Double fixSuccessRate;

    private Long aiDuration;

    private String status;

    private String errorMessage;

    private String markdownPath;

    private String pdfPath;

    private LocalDateTime createTime;

    // 反序列化后的Agent结果
    private SummaryStatistics summaryStatistics;

    private ProjectHealthReport healthReport;
}
