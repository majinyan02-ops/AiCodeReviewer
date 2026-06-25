-- ===================================================
-- Phase-9: 分析记录表
-- ===================================================

CREATE TABLE IF NOT EXISTS `analysis_record` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `project_id`          BIGINT       NOT NULL COMMENT '项目ID',
    `project_name`        VARCHAR(100) DEFAULT NULL COMMENT '项目名称(冗余)',
    `task_id`             BIGINT       DEFAULT NULL COMMENT '关联review_task ID',
    `overall_score`       INT          DEFAULT NULL COMMENT '总体评分(0-100)',
    `risk_level`          VARCHAR(20)  DEFAULT NULL COMMENT '风险等级: HIGH/MEDIUM/LOW',
    `health_level`        VARCHAR(20)  DEFAULT NULL COMMENT '健康等级: EXCELLENT/GOOD/FAIR/POOR/CRITICAL',
    `health_score`        INT          DEFAULT NULL COMMENT '健康评分(0-100)',
    `total_issues`        INT          DEFAULT 0 COMMENT '总问题数',
    `error_count`         INT          DEFAULT 0 COMMENT '错误数',
    `warning_count`       INT          DEFAULT 0 COMMENT '警告数',
    `info_count`          INT          DEFAULT 0 COMMENT '信息数',
    `fixed_issues`        INT          DEFAULT 0 COMMENT '已修复数',
    `fix_success_rate`    DOUBLE       DEFAULT 0 COMMENT '修复成功率',
    `summary_result_json` LONGTEXT     DEFAULT NULL COMMENT 'SummaryAgentResult JSON',
    `ai_duration`         BIGINT       DEFAULT 0 COMMENT 'AI总耗时(ms)',
    `status`              VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS/FAILED/PARTIAL',
    `error_message`       TEXT         DEFAULT NULL COMMENT '失败原因',
    `markdown_path`       VARCHAR(500) DEFAULT NULL COMMENT 'Markdown报告路径',
    `pdf_path`            VARCHAR(500) DEFAULT NULL COMMENT 'PDF报告路径',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_health_score` (`health_score`),
    KEY `idx_project_time` (`project_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析记录表';
