# API Design

Base URL

/api

------

# Auth Module

## 用户注册

POST /auth/register

Request

{
"username":"admin",
"password":"123456",
"email":"[test@qq.com](mailto:test@qq.com)"
}

Response

{
"code":200,
"message":"success"
}

------

## 用户登录

POST /auth/login

Request

{
"username":"admin",
"password":"123456"
}

Response

{
"token":"jwt-token"
}

------

# Project Module

## 创建项目

POST /project

Request

{
"name":"mall-system",
"description":"商城系统",
"gitUrl":"https://github.com/xxx/xxx.git",
"branchName":"main"
}

------

## 项目列表

GET /project/page

------

## 项目详情

GET /project/{id}

------

## 删除项目

DELETE /project/{id}

------

# Git Module

## 同步仓库

POST /git/sync/{projectId}

------

## 获取仓库状态

GET /git/status/{projectId}

------

# Review Module

## 创建审查任务

POST /review/start

Request

{
"projectId":1
}

Response

{
"taskId":1001
}

------

## 查询任务状态

GET /review/task/{taskId}

------

## 获取审查结果

GET /review/result/{taskId}

------

# Rule Module

## 获取规则列表

GET /rule/list

------

## 开启规则

POST /rule/enable/{ruleId}

------

## 禁用规则

POST /rule/disable/{ruleId}

------

# Report Module

## 获取报告

GET /report/{taskId}

------

## 导出PDF

GET /report/export/{taskId}

------

# AI Module

## 获取AI分析结果

GET /ai/result/{reviewResultId}

------

## 重新生成AI建议

POST /ai/reanalyze/{reviewResultId}