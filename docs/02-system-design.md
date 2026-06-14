# 系统架构设计

## 项目名称

AI Code Reviewer

AI代码审查与自动修复平台

------

## 技术栈

### 后端

Java 21

Spring Boot 3.5

Spring AI

MyBatis Plus

Spring Security

JWT

JavaParser

JGit

Lombok

MapStruct

------

### 数据库

MySQL 8

Redis

------

### 前端

Vue3

TypeScript

Element Plus

Pinia

Axios

------

## 系统架构

Browser

↓

Vue3 Frontend

↓

SpringBoot Backend

↓

Service Layer

├── UserService

├── ProjectService

├── ReviewService

├── RuleEngineService

├── AiAnalysisService

└── ReportService

↓

MySQL

Redis

OpenAI / DeepSeek

------

## 核心模块

### 用户模块

负责：

- 登录
- 注册
- JWT认证

------

### 项目模块

负责：

- 创建项目
- Git仓库绑定
- 项目同步

------

### Git模块

负责：

- Clone仓库
- Pull更新
- 获取Commit信息

技术：

JGit

------

### 代码解析模块

负责：

解析Java代码

技术：

JavaParser

支持：

- Controller
- Service
- Entity
- Mapper

扫描

- 类
- 方法
- 注解
- SQL调用

------

### 规则引擎模块

负责：

检测代码问题

V1规则：

RULE001

缺少@Transactional

RULE002

缺少日志

RULE003

Controller操作Mapper

RULE004

System.out.println

RULE005

超长方法

------

### AI分析模块

负责：

调用Spring AI

生成：

- 风险说明
- 原因分析
- 修复建议

------

### 报告模块

负责：

生成审查报告

支持：

Markdown

PDF

------

## AI调用流程

用户发起审查

↓

Git同步项目

↓

JavaParser扫描代码

↓

规则引擎发现问题

↓

构建Prompt

↓

Spring AI分析

↓

生成报告

↓

保存数据库

------

## V2升级规划

增加：

GitHub OAuth

GitLab OAuth

自动PR

自动修复

MCP Tool

Multi Agent