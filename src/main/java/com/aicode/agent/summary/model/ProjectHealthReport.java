package com.aicode.agent.summary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目健康度报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectHealthReport {

    private String healthLevel;

    private Integer healthScore;

    private String overallStatus;

    private String summary;

    private List<String> strengths;

    private List<String> weaknesses;

    private List<String> recommendations;

    private List<String> topProblems;
}
