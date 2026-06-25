package com.aicode.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataVO {

    private Long projectId;

    private String projectName;

    private List<TrendPoint> points;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private Integer overallScore;
        private Integer healthScore;
        private Integer totalIssues;
        private Integer errorCount;
        private Integer warningCount;
        private Integer infoCount;
        private Integer fixedIssues;
        private Double fixSuccessRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectTrendSummary {
        private Long projectId;
        private String projectName;
        private Integer latestScore;
        private Integer latestHealthScore;
        private Double scoreChange;
        private Integer totalRecords;
    }
}
