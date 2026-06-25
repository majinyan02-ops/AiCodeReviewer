package com.aicode.mapper;

import com.aicode.entity.AnalysisRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnalysisRecordMapper extends BaseMapper<AnalysisRecord> {

    /**
     * 查询项目趋势数据（仅统计列，不加载JSON）
     */
    @Select("""
        SELECT id, project_id, project_name, overall_score, health_score,
               total_issues, error_count, warning_count, info_count,
               fixed_issues, fix_success_rate, create_time
        FROM analysis_record
        WHERE project_id = #{projectId} AND status = 'SUCCESS'
        ORDER BY create_time DESC
        LIMIT #{limit}
    """)
    List<AnalysisRecord> selectTrendData(@Param("projectId") Long projectId,
                                         @Param("limit") int limit);

    /**
     * 查询所有项目的最新一条记录（概览对比）
     */
    @Select("""
        SELECT ar.id, ar.project_id, ar.project_name, ar.overall_score,
               ar.health_score, ar.total_issues, ar.error_count, ar.warning_count,
               ar.info_count, ar.fixed_issues, ar.fix_success_rate, ar.create_time
        FROM analysis_record ar
        INNER JOIN (
            SELECT project_id, MAX(create_time) AS max_time
            FROM analysis_record
            WHERE status = 'SUCCESS'
            GROUP BY project_id
        ) latest ON ar.project_id = latest.project_id AND ar.create_time = latest.max_time
        WHERE ar.status = 'SUCCESS'
        ORDER BY ar.health_score DESC
    """)
    List<AnalysisRecord> selectLatestPerProject();
}
