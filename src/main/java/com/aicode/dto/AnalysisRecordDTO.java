package com.aicode.dto;

import com.aicode.report.model.IssueSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分析记录列表 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecordDTO {

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

    private String markdownPath;

    private String pdfPath;

    private LocalDateTime createTime;

    /** 问题列表（从 summary_result_json 解析） */
    private List<IssueSummary> issues;
}
