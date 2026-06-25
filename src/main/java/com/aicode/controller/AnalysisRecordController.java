package com.aicode.controller;

import com.aicode.common.PageResult;
import com.aicode.common.Result;
import com.aicode.dto.AnalysisRecordDTO;
import com.aicode.dto.AnalysisRecordDetailDTO;
import com.aicode.dto.AnalysisRecordQueryRequest;
import com.aicode.service.AnalysisRecordService;
import com.aicode.vo.StatisticsOverviewVO;
import com.aicode.vo.TrendDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分析记录控制器
 */
@RestController
@RequestMapping("/api/analysis-record")
@RequiredArgsConstructor
public class AnalysisRecordController {

    private final AnalysisRecordService analysisRecordService;

    /**
     * 分页查询历史记录
     */
    @GetMapping("/page")
    public Result<PageResult<AnalysisRecordDTO>> queryRecords(AnalysisRecordQueryRequest request) {
        return Result.success(analysisRecordService.queryRecords(request));
    }

    /**
     * 查看记录详情
     */
    @GetMapping("/{id}")
    public Result<AnalysisRecordDetailDTO> getRecordDetail(@PathVariable Long id) {
        AnalysisRecordDetailDTO detail = analysisRecordService.getRecordDetail(id);
        if (detail == null) {
            return Result.fail("记录不存在");
        }
        return Result.success(detail);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        analysisRecordService.deleteRecord(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteRecords(@RequestBody List<Long> ids) {
        analysisRecordService.batchDeleteRecords(ids);
        return Result.success();
    }

    /**
     * 项目趋势数据
     */
    @GetMapping("/trend/{projectId}")
    public Result<TrendDataVO> getProjectTrend(@PathVariable Long projectId,
                                               @RequestParam(defaultValue = "30") int limit) {
        return Result.success(analysisRecordService.getProjectTrend(projectId, limit));
    }

    /**
     * 所有项目趋势概览
     */
    @GetMapping("/trend/overview")
    public Result<List<TrendDataVO.ProjectTrendSummary>> getProjectTrendOverview() {
        return Result.success(analysisRecordService.getProjectTrendSummaries());
    }

    /**
     * 统计概览
     */
    @GetMapping("/statistics/overview")
    public Result<StatisticsOverviewVO> getStatisticsOverview() {
        return Result.success(analysisRecordService.getStatisticsOverview());
    }
}
