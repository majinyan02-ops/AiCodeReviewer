package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分析记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("analysis_record")
public class AnalysisRecord {

    @TableId(type = IdType.AUTO)
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

    private String summaryResultJson;

    private Long aiDuration;

    private String status;

    private String errorMessage;

    private String markdownPath;

    private String pdfPath;

    private LocalDateTime createTime;
}
