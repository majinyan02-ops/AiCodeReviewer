package com.aicode.service;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.common.PageResult;
import com.aicode.dto.AnalysisRecordDTO;
import com.aicode.dto.AnalysisRecordDetailDTO;
import com.aicode.dto.AnalysisRecordQueryRequest;
import com.aicode.vo.StatisticsOverviewVO;
import com.aicode.vo.TrendDataVO;

import java.util.List;

/**
 * 分析记录服务
 */
public interface AnalysisRecordService {

    /**
     * 保存一次完整的Agent管线执行结果
     */
    void saveRecord(Long projectId, String projectName, Long taskId,
                    ReviewAgentResult reviewResult,
                    FixAgentResult fixResult,
                    SummaryAgentResult summaryResult,
                    Long aiDuration,
                    String status, String errorMessage);

    /**
     * 分页查询历史记录
     */
    PageResult<AnalysisRecordDTO> queryRecords(AnalysisRecordQueryRequest request);

    /**
     * 查看单条记录详情
     */
    AnalysisRecordDetailDTO getRecordDetail(Long recordId);

    /**
     * 删除单条记录
     */
    void deleteRecord(Long recordId);

    /**
     * 批量删除记录
     */
    void batchDeleteRecords(List<Long> recordIds);

    /**
     * 获取项目趋势数据
     */
    TrendDataVO getProjectTrend(Long projectId, int limit);

    /**
     * 获取所有项目最新记录（概览对比）
     */
    List<TrendDataVO.ProjectTrendSummary> getProjectTrendSummaries();

    /**
     * 统计概览
     */
    StatisticsOverviewVO getStatisticsOverview();

    /**
     * 更新报告路径
     */
    void updateReportPaths(Long recordId, String markdownPath, String pdfPath);
}
