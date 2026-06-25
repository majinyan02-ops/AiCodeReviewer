package com.aicode.service.impl;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.ProjectHealthReport;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.agent.summary.model.SummaryStatistics;
import com.aicode.common.PageResult;
import com.aicode.dto.AnalysisRecordDTO;
import com.aicode.dto.AnalysisRecordDetailDTO;
import com.aicode.dto.AnalysisRecordQueryRequest;
import com.aicode.entity.AnalysisRecord;
import com.aicode.mapper.AnalysisRecordMapper;
import com.aicode.report.model.IssueSummary;
import com.aicode.service.AnalysisRecordService;
import com.aicode.vo.StatisticsOverviewVO;
import com.aicode.vo.TrendDataVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisRecordServiceImpl implements AnalysisRecordService {

    private final AnalysisRecordMapper analysisRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void saveRecord(Long projectId, String projectName, Long taskId,
                           ReviewAgentResult reviewResult,
                           FixAgentResult fixResult,
                           SummaryAgentResult summaryResult,
                           Long aiDuration,
                           String status, String errorMessage) {
        try {
            // 提取统计字段
            Integer overallScore = null;
            String riskLevel = null;
            String healthLevel = null;
            Integer healthScore = null;
            int totalIssues = 0, errorCount = 0, warningCount = 0, infoCount = 0;
            int fixedIssues = 0;
            double fixSuccessRate = 0.0;

            if (reviewResult != null) {
                overallScore = reviewResult.getOverallScore();
                riskLevel = reviewResult.getRiskLevel();
                errorCount = reviewResult.getErrorCount();
                warningCount = reviewResult.getWarningCount();
                infoCount = reviewResult.getInfoCount();
                totalIssues = reviewResult.getTotalRules();
            }

            if (summaryResult != null) {
                SummaryStatistics stats = summaryResult.getStatistics();
                ProjectHealthReport health = summaryResult.getHealthReport();

                if (stats != null) {
                    totalIssues = stats.getTotalIssues();
                    errorCount = stats.getErrorCount();
                    warningCount = stats.getWarningCount();
                    infoCount = stats.getInfoCount();
                    fixedIssues = stats.getFixedIssues();
                    fixSuccessRate = stats.getFixSuccessRate();
                }

                if (health != null) {
                    healthLevel = health.getHealthLevel();
                    healthScore = health.getHealthScore();
                }
            }

            // 序列化SummaryAgentResult为JSON
            String summaryJson = null;
            if (summaryResult != null) {
                summaryJson = objectMapper.writeValueAsString(summaryResult);
            }

            AnalysisRecord record = AnalysisRecord.builder()
                    .projectId(projectId)
                    .projectName(projectName)
                    .taskId(taskId)
                    .overallScore(overallScore)
                    .riskLevel(riskLevel)
                    .healthLevel(healthLevel)
                    .healthScore(healthScore)
                    .totalIssues(totalIssues)
                    .errorCount(errorCount)
                    .warningCount(warningCount)
                    .infoCount(infoCount)
                    .fixedIssues(fixedIssues)
                    .fixSuccessRate(fixSuccessRate)
                    .summaryResultJson(summaryJson)
                    .aiDuration(aiDuration != null ? aiDuration : 0L)
                    .status(status)
                    .errorMessage(errorMessage)
                    .createTime(LocalDateTime.now())
                    .build();

            analysisRecordMapper.insert(record);
            log.info("分析记录已保存: projectId={}, recordId={}", projectId, record.getId());

        } catch (JsonProcessingException e) {
            log.error("序列化SummaryAgentResult失败: projectId={}", projectId, e);
        } catch (Exception e) {
            log.error("保存分析记录失败: projectId={}", projectId, e);
        }
    }

    @Override
    public PageResult<AnalysisRecordDTO> queryRecords(AnalysisRecordQueryRequest request) {
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<>();

        if (request.getProjectId() != null) {
            wrapper.eq(AnalysisRecord::getProjectId, request.getProjectId());
        }
        if (StringUtils.hasText(request.getRiskLevel())) {
            wrapper.eq(AnalysisRecord::getRiskLevel, request.getRiskLevel());
        }
        if (StringUtils.hasText(request.getHealthLevel())) {
            wrapper.eq(AnalysisRecord::getHealthLevel, request.getHealthLevel());
        }
        if (StringUtils.hasText(request.getStartDate())) {
            wrapper.ge(AnalysisRecord::getCreateTime, LocalDateTime.parse(request.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (StringUtils.hasText(request.getEndDate())) {
            wrapper.le(AnalysisRecord::getCreateTime, LocalDateTime.parse(request.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1));
        }

        wrapper.orderByDesc(AnalysisRecord::getCreateTime);

        Page<AnalysisRecord> page = new Page<>(request.getPage(), request.getSize());
        Page<AnalysisRecord> result = analysisRecordMapper.selectPage(page, wrapper);

        List<AnalysisRecordDTO> dtoList = result.getRecords().stream()
                .map(this::toDTO)
                .toList();

        return PageResult.<AnalysisRecordDTO>builder()
                .records(dtoList)
                .total(result.getTotal())
                .size((int) result.getSize())
                .current((int) result.getCurrent())
                .pages((int) result.getPages())
                .build();
    }

    @Override
    public AnalysisRecordDetailDTO getRecordDetail(Long recordId) {
        AnalysisRecord record = analysisRecordMapper.selectById(recordId);
        if (record == null) return null;
        return toDetailDTO(record);
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        analysisRecordMapper.deleteById(recordId);
    }

    @Override
    @Transactional
    public void batchDeleteRecords(List<Long> recordIds) {
        analysisRecordMapper.deleteBatchIds(recordIds);
    }

    @Override
    public TrendDataVO getProjectTrend(Long projectId, int limit) {
        List<AnalysisRecord> records = analysisRecordMapper.selectTrendData(projectId, limit);

        // 反转为时间正序
        List<TrendDataVO.TrendPoint> points = new ArrayList<>();
        for (int i = records.size() - 1; i >= 0; i--) {
            AnalysisRecord r = records.get(i);
            points.add(TrendDataVO.TrendPoint.builder()
                    .date(r.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .overallScore(r.getOverallScore())
                    .healthScore(r.getHealthScore())
                    .totalIssues(r.getTotalIssues())
                    .errorCount(r.getErrorCount())
                    .warningCount(r.getWarningCount())
                    .infoCount(r.getInfoCount())
                    .fixedIssues(r.getFixedIssues())
                    .fixSuccessRate(r.getFixSuccessRate())
                    .build());
        }

        String projectName = records.isEmpty() ? "" : records.get(0).getProjectName();

        return TrendDataVO.builder()
                .projectId(projectId)
                .projectName(projectName)
                .points(points)
                .build();
    }

    @Override
    public List<TrendDataVO.ProjectTrendSummary> getProjectTrendSummaries() {
        List<AnalysisRecord> records = analysisRecordMapper.selectLatestPerProject();

        return records.stream()
                .map(r -> TrendDataVO.ProjectTrendSummary.builder()
                        .projectId(r.getProjectId())
                        .projectName(r.getProjectName())
                        .latestScore(r.getOverallScore())
                        .latestHealthScore(r.getHealthScore())
                        .scoreChange(0.0)
                        .totalRecords(1)
                        .build())
                .toList();
    }

    @Override
    public StatisticsOverviewVO getStatisticsOverview() {
        List<AnalysisRecord> allRecords = analysisRecordMapper.selectList(null);

        if (allRecords.isEmpty()) {
            return StatisticsOverviewVO.builder()
                    .totalProjects(0)
                    .totalRecords(0)
                    .totalIssues(0)
                    .totalFixed(0)
                    .avgHealthScore(0)
                    .avgFixRate(0)
                    .projectHealthItems(List.of())
                    .build();
        }

        int totalProjects = (int) allRecords.stream()
                .map(AnalysisRecord::getProjectId)
                .distinct()
                .count();
        int totalRecords = allRecords.size();
        int totalIssues = allRecords.stream().mapToInt(r -> r.getTotalIssues() != null ? r.getTotalIssues() : 0).sum();
        int totalFixed = allRecords.stream().mapToInt(r -> r.getFixedIssues() != null ? r.getFixedIssues() : 0).sum();
        double avgHealthScore = allRecords.stream()
                .filter(r -> r.getHealthScore() != null)
                .mapToInt(AnalysisRecord::getHealthScore)
                .average().orElse(0);
        double avgFixRate = allRecords.stream()
                .filter(r -> r.getFixSuccessRate() != null)
                .mapToDouble(AnalysisRecord::getFixSuccessRate)
                .average().orElse(0);

        // 每个项目的最新一条
        List<AnalysisRecord> latestRecords = analysisRecordMapper.selectLatestPerProject();
        List<StatisticsOverviewVO.ProjectHealthItem> items = latestRecords.stream()
                .map(r -> StatisticsOverviewVO.ProjectHealthItem.builder()
                        .projectId(r.getProjectId())
                        .projectName(r.getProjectName())
                        .healthScore(r.getHealthScore())
                        .healthLevel(r.getHealthLevel())
                        .totalRecords(1)
                        .totalIssues(r.getTotalIssues())
                        .fixedIssues(r.getFixedIssues())
                        .fixRate(r.getFixSuccessRate())
                        .build())
                .toList();

        return StatisticsOverviewVO.builder()
                .totalProjects(totalProjects)
                .totalRecords(totalRecords)
                .totalIssues(totalIssues)
                .totalFixed(totalFixed)
                .avgHealthScore(Math.round(avgHealthScore * 10.0) / 10.0)
                .avgFixRate(Math.round(avgFixRate * 10.0) / 10.0)
                .projectHealthItems(items)
                .build();
    }

    @Override
    public void updateReportPaths(Long recordId, String markdownPath, String pdfPath) {
        AnalysisRecord record = analysisRecordMapper.selectById(recordId);
        if (record == null) return;
        record.setMarkdownPath(markdownPath);
        record.setPdfPath(pdfPath);
        analysisRecordMapper.updateById(record);
    }

    // ============ 私有转换方法 ============

    private AnalysisRecordDTO toDTO(AnalysisRecord record) {
        List<IssueSummary> issues = parseIssuesFromJson(record.getSummaryResultJson());

        return AnalysisRecordDTO.builder()
                .id(record.getId())
                .projectId(record.getProjectId())
                .projectName(record.getProjectName())
                .taskId(record.getTaskId())
                .overallScore(record.getOverallScore())
                .riskLevel(record.getRiskLevel())
                .healthLevel(record.getHealthLevel())
                .healthScore(record.getHealthScore())
                .totalIssues(record.getTotalIssues())
                .errorCount(record.getErrorCount())
                .warningCount(record.getWarningCount())
                .infoCount(record.getInfoCount())
                .fixedIssues(record.getFixedIssues())
                .fixSuccessRate(record.getFixSuccessRate())
                .aiDuration(record.getAiDuration())
                .status(record.getStatus())
                .markdownPath(record.getMarkdownPath())
                .pdfPath(record.getPdfPath())
                .createTime(record.getCreateTime())
                .issues(issues)
                .build();
    }

    private List<IssueSummary> parseIssuesFromJson(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        try {
            SummaryAgentResult summary = objectMapper.readValue(json, SummaryAgentResult.class);
            if (summary == null || summary.getReviewResult() == null) return List.of();
            ReviewAgentResult review = summary.getReviewResult();
            if (review.getRuleResults() == null) return List.of();

            return review.getRuleResults().stream()
                    .filter(r -> !r.isPassed())
                    .map(r -> IssueSummary.builder()
                            .ruleId(r.getRuleId())
                            .ruleName(r.getRuleName())
                            .severity(r.getSeverity())
                            .className(r.getClassName())
                            .methodName(r.getMethodName())
                            .filePath(r.getFilePath())
                            .lineNumber(r.getLineNumber())
                            .reason(r.getMessage())
                            .suggestion(r.getSuggestion())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.warn("解析summaryResultJson失败", e);
            return List.of();
        }
    }

    private AnalysisRecordDetailDTO toDetailDTO(AnalysisRecord record) {
        SummaryStatistics summaryStatistics = null;
        ProjectHealthReport healthReport = null;

        if (StringUtils.hasText(record.getSummaryResultJson())) {
            try {
                SummaryAgentResult summaryResult = objectMapper.readValue(
                        record.getSummaryResultJson(), SummaryAgentResult.class);
                if (summaryResult != null) {
                    summaryStatistics = summaryResult.getStatistics();
                    healthReport = summaryResult.getHealthReport();
                }
            } catch (JsonProcessingException e) {
                log.warn("反序列化summaryResultJson失败: recordId={}", record.getId(), e);
            }
        }

        return AnalysisRecordDetailDTO.builder()
                .id(record.getId())
                .projectId(record.getProjectId())
                .projectName(record.getProjectName())
                .taskId(record.getTaskId())
                .overallScore(record.getOverallScore())
                .riskLevel(record.getRiskLevel())
                .healthLevel(record.getHealthLevel())
                .healthScore(record.getHealthScore())
                .totalIssues(record.getTotalIssues())
                .errorCount(record.getErrorCount())
                .warningCount(record.getWarningCount())
                .infoCount(record.getInfoCount())
                .fixedIssues(record.getFixedIssues())
                .fixSuccessRate(record.getFixSuccessRate())
                .aiDuration(record.getAiDuration())
                .status(record.getStatus())
                .errorMessage(record.getErrorMessage())
                .markdownPath(record.getMarkdownPath())
                .pdfPath(record.getPdfPath())
                .createTime(record.getCreateTime())
                .summaryStatistics(summaryStatistics)
                .healthReport(healthReport)
                .build();
    }
}
