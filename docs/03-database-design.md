# 数据库设计

## user

用户表

| 字段        | 类型         |
| ----------- | ------------ |
| id          | bigint       |
| username    | varchar(50)  |
| password    | varchar(255) |
| email       | varchar(100) |
| role        | varchar(20)  |
| status      | tinyint      |
| create_time | datetime     |
| update_time | datetime     |

------

## project

项目表

| 字段        | 类型         |
| ----------- | ------------ |
| id          | bigint       |
| name        | varchar(100) |
| description | varchar(500) |
| git_url     | varchar(500) |
| branch_name | varchar(50)  |
| creator_id  | bigint       |
| status      | tinyint      |
| create_time | datetime     |

------

## review_task

审查任务表

| 字段        | 类型         |
| ----------- | ------------ |
| id          | bigint       |
| project_id  | bigint       |
| task_name   | varchar(100) |
| status      | varchar(20)  |
| start_time  | datetime     |
| end_time    | datetime     |
| create_time | datetime     |

状态：

PENDING

RUNNING

SUCCESS

FAILED

------

## review_result

审查结果表

| 字段              | 类型         |
| ----------------- | ------------ |
| id                | bigint       |
| task_id           | bigint       |
| file_path         | varchar(500) |
| rule_code         | varchar(50)  |
| risk_level        | varchar(20)  |
| problem_desc      | text         |
| ai_analysis       | text         |
| repair_suggestion | text         |
| create_time       | datetime     |

------

## rule_config

规则配置表

| 字段        | 类型         |
| ----------- | ------------ |
| id          | bigint       |
| rule_code   | varchar(50)  |
| rule_name   | varchar(100) |
| description | varchar(500) |
| enabled     | tinyint      |
| create_time | datetime     |

------

## review_report

报告表

| 字段           | 类型         |
| -------------- | ------------ |
| id             | bigint       |
| task_id        | bigint       |
| report_content | longtext     |
| report_url     | varchar(500) |
| create_time    | datetime     |

------

## ER关系

user

↓

project

↓

review_task

↓

review_result

↓

review_report

rule_config

↓

review_result