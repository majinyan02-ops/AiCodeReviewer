# AI Code Reviewer Frontend Spec

## 项目背景

当前后端已经完成。

项目名称：

AI Code Reviewer

后端技术栈：

SpringBoot 3

Spring Security

JWT

MyBatis Plus

MySQL

Redis

JavaParser

Spring AI

后端能力：

- 用户登录
- JWT鉴权
- 项目管理
- Git仓库管理
- JavaParser源码解析
- CallGraph调用链分析
- RuleEngine规则检测
- Spring AI问题分析
- Markdown报告
- PDF报告
- 报告下载

前端需要完整实现企业级管理后台。

------

# 技术栈

必须使用：

Vue3

TypeScript

Vite

Pinia

Vue Router

Axios

Element Plus

ECharts

禁止：

Vue2

JavaScript

jQuery

Mock框架

------

# 项目结构

src

├── api

├── assets

├── components

├── hooks

├── layout

├── router

├── store

├── styles

├── types

├── utils

├── views

└── App.vue

------

# 页面规划

## Login

路径：

/login

功能：

用户登录

JWT保存

自动跳转

Token失效处理

------

## Dashboard

路径：

/dashboard

展示：

项目数量

扫描数量

问题数量

ERROR数量

WARNING数量

INFO数量

最近扫描记录

最近报告

ECharts统计图

------

## Project

路径：

/project

功能：

项目列表

新增项目

编辑项目

删除项目

分页

搜索

字段：

项目名称

Git仓库地址

分支

状态

创建时间

------

## Code Review

路径：

/review

功能：

选择项目

开始扫描

查看扫描进度

展示规则检测结果

展示AI分析结果

展示总体评分

展示风险等级

展示统计图

模块：

RuleResult

AiIssueAnalysis

OverallSummary

------

## Report Center

路径：

/report

功能：

历史报告

查看详情

Markdown预览

PDF下载

删除报告

分页

搜索

------

# Layout设计

采用经典后台布局

Header

Sidebar

MainContent

Footer

Header展示：

用户信息

退出登录

Sidebar展示：

Dashboard

项目管理

代码审查

报告中心

------

# Router设计

/public

/login

/private

/dashboard

/project

/review

/report

使用路由守卫。

未登录：

自动跳转Login

------

# Pinia设计

## UserStore

字段：

token

userInfo

功能：

登录

退出

获取用户信息

------

# Axios封装

创建：

src/utils/request.ts

实现：

Request Interceptor

Response Interceptor

Token自动携带

401自动跳转登录

统一错误提示

禁止页面直接使用axios。

所有请求必须通过api层访问。

------

# API模块

src/api

auth.ts

project.ts

review.ts

report.ts

------

# UI风格

企业级后台

现代化

简洁

适合作为校招项目展示

统一使用：

Element Plus

Card

Table

Tag

Statistic

Drawer

Dialog

Pagination

ECharts

------

# Task-01

初始化项目架构

实现：

Router

Pinia

Axios

Layout

Sidebar

Header

登录拦截

项目可运行

完成后更新：

dev-log/task01.md

------

# Task-02

实现登录页面

Login.vue

Pinia用户状态

JWT保存

自动登录

退出登录

完成后更新：

dev-log/task02.md

------

# Task-03

实现Dashboard

统计卡片

图表

最近记录

完成后更新：

dev-log/task03.md

------

# Task-04

实现项目管理页面

项目CRUD

搜索

分页

Dialog

完成后更新：

dev-log/task04.md

------

# Task-05

实现代码审查页面

扫描按钮

结果展示

规则检测结果

AI分析结果

统计图

完成后更新：

dev-log/task05.md

------

# Task-06

实现报告中心

报告列表

Markdown预览

PDF下载

删除

完成后更新：

dev-log/task06.md

------

# Task-07

统一API层重构

统一Type定义

统一异常处理

代码优化

完成后更新：

dev-log/task07.md

------

# 开发规则

每完成一个Task：

1. 更新dev-log
2. 执行npm run build
3. 修复编译错误
4. 等待下一步确认

禁止一次性实现全部功能。

严格按照Task顺序开发。

优先保证代码质量和架构设计。