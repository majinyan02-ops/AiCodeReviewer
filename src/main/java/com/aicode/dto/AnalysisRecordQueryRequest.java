package com.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分析记录查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecordQueryRequest {

    private Long projectId;

    private String keyword;

    private String riskLevel;

    private String healthLevel;

    private String startDate;

    private String endDate;

    private Integer page = 1;

    private Integer size = 20;
}
