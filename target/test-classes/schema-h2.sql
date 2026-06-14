-- H2 Test Database Schema
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(255) NOT NULL,
    `email`       VARCHAR(100),
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'DEVELOPER',
    `status`      TINYINT      NOT NULL DEFAULT 1,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `project` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `git_url`     VARCHAR(500),
    `branch_name` VARCHAR(50)  DEFAULT 'main',
    `creator_id`  BIGINT       NOT NULL,
    `status`      TINYINT      NOT NULL DEFAULT 1,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `review_task` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `project_id`  BIGINT       NOT NULL,
    `task_name`   VARCHAR(100) NOT NULL,
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    `start_time`  DATETIME,
    `end_time`    DATETIME,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `review_result` (
    `id`                BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id`           BIGINT       NOT NULL,
    `file_path`         VARCHAR(500) NOT NULL,
    `rule_code`         VARCHAR(50)  NOT NULL,
    `risk_level`        VARCHAR(20)  NOT NULL DEFAULT 'INFO',
    `problem_desc`      TEXT,
    `ai_analysis`       TEXT,
    `repair_suggestion` TEXT,
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `rule_config` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `rule_code`   VARCHAR(50)  NOT NULL,
    `rule_name`   VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `enabled`     TINYINT      NOT NULL DEFAULT 1,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `review_report` (
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id`        BIGINT NOT NULL,
    `report_content` LONGTEXT,
    `report_url`     VARCHAR(500),
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
