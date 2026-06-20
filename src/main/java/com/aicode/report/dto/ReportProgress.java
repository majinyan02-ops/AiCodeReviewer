package com.aicode.report.dto;

import com.aicode.report.model.ReviewReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报告生成进度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportProgress {

    /** 任务 ID */
    private String taskId;

    /** 状态: PENDING / RUNNING / SUCCESS / FAILED */
    private String status;

    /** 当前阶段描述 */
    private String stage;

    /** 进度百分比 (0-100) */
    private int percent;

    /** 完成后的报告 */
    private ReviewReport report;

    /** 错误信息 */
    private String error;

    // 工厂方法

    public static ReportProgress pending(String taskId) {
        return ReportProgress.builder()
                .taskId(taskId).status("PENDING").stage("等待开始").percent(0).build();
    }

    public static ReportProgress running(String taskId, String stage, int percent) {
        return ReportProgress.builder()
                .taskId(taskId).status("RUNNING").stage(stage).percent(percent).build();
    }

    public static ReportProgress success(String taskId, ReviewReport report) {
        return ReportProgress.builder()
                .taskId(taskId).status("SUCCESS").stage("完成").percent(100).report(report).build();
    }

    public static ReportProgress failed(String taskId, String error) {
        return ReportProgress.builder()
                .taskId(taskId).status("FAILED").stage("失败").error(error).percent(0).build();
    }
}
