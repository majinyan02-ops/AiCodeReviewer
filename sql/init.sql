-- ===================================================
-- AI Code Reviewer - Database Initialization Script
-- ===================================================

CREATE DATABASE IF NOT EXISTS ai_code_reviewer
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE ai_code_reviewer;

-- ===================================================
-- User Table
-- ===================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'DEVELOPER' COMMENT '角色: ADMIN/DEVELOPER',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ===================================================
-- Project Table
-- ===================================================
CREATE TABLE IF NOT EXISTS `project` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    `git_url`     VARCHAR(500) DEFAULT NULL COMMENT 'Git仓库地址',
    `branch_name` VARCHAR(50)  DEFAULT 'main' COMMENT '分支名称',
    `creator_id`  BIGINT       NOT NULL COMMENT '创建者ID',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ===================================================
-- Review Task Table
-- ===================================================
CREATE TABLE IF NOT EXISTS `review_task` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `project_id`  BIGINT       NOT NULL COMMENT '项目ID',
    `task_name`   VARCHAR(100) NOT NULL COMMENT '任务名称',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/RUNNING/SUCCESS/FAILED',
    `start_time`  DATETIME     DEFAULT NULL COMMENT '开始时间',
    `end_time`    DATETIME     DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审查任务表';

-- ===================================================
-- Review Result Table
-- ===================================================
CREATE TABLE IF NOT EXISTS `review_result` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    `task_id`           BIGINT       NOT NULL COMMENT '任务ID',
    `file_path`         VARCHAR(500) NOT NULL COMMENT '文件路径',
    `rule_code`         VARCHAR(50)  NOT NULL COMMENT '规则编码',
    `risk_level`        VARCHAR(20)  NOT NULL DEFAULT 'INFO' COMMENT '风险等级: HIGH/MEDIUM/LOW/INFO',
    `problem_desc`      TEXT         DEFAULT NULL COMMENT '问题描述',
    `ai_analysis`       TEXT         DEFAULT NULL COMMENT 'AI分析结果',
    `repair_suggestion` TEXT         DEFAULT NULL COMMENT '修复建议',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审查结果表';

-- ===================================================
-- Rule Config Table
-- ===================================================
CREATE TABLE IF NOT EXISTS `rule_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_code`   VARCHAR(50)  NOT NULL COMMENT '规则编码',
    `rule_name`   VARCHAR(100) NOT NULL COMMENT '规则名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
    `enabled`     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则配置表';

-- ===================================================
-- Review Report Table
-- ===================================================
CREATE TABLE IF NOT EXISTS `review_report` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    `task_id`        BIGINT       NOT NULL COMMENT '任务ID',
    `report_content` LONGTEXT     DEFAULT NULL COMMENT '报告内容(Markdown)',
    `report_url`     VARCHAR(500) DEFAULT NULL COMMENT '报告文件URL',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审查报告表';

-- ===================================================
-- Initial Rule Data
-- ===================================================
INSERT INTO `rule_config` (`rule_code`, `rule_name`, `description`, `enabled`) VALUES
('RULE-001', '缺少@Transactional', 'Service层方法缺少@Transactional事务注解', 1),
('RULE-002', '缺少日志记录', 'Service/Controller方法缺少日志记录', 1),
('RULE-003', '捕获Exception后未处理', '捕获Exception后未进行日志记录或重新抛出', 1),
('RULE-004', '存在System.out.println', '代码中存在System.out.println调试输出', 1),
('RULE-005', '方法长度超过100行', '方法长度超过100行，建议拆分', 1),
('RULE-006', 'Controller直接操作数据库', 'Controller层直接调用Mapper操作数据库', 1);
