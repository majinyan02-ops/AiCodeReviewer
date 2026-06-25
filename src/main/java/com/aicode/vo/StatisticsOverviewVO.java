package com.aicode.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统计概览 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsOverviewVO {

    private int totalProjects;

    private int totalRecords;

    private int totalIssues;

    private int totalFixed;

    private double avgHealthScore;

    private double avgFixRate;

    private List<ProjectHealthItem> projectHealthItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectHealthItem {
        private Long projectId;
        private String projectName;
        private Integer healthScore;
        private String healthLevel;
        private Integer totalRecords;
        private Integer totalIssues;
        private Integer fixedIssues;
        private Double fixRate;
    }
}
