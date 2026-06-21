# Development Task List

------

## Phase-1 项目初始化

Task-01

初始化SpringBoot项目

要求：

- Java21
- Maven
- SpringBoot3.5
- MyBatisPlus
- MySQL
- Redis

完成标准：

项目成功启动

------

Task-02

统一返回结果封装

创建：

Result

ResultCode

GlobalExceptionHandler

------

Task-03

JWT认证框架

实现：

登录

注册

Token校验

Spring Security

------

## Phase-2 项目管理

Task-04

完成Project表

Entity

Mapper

Service

Controller

------

Task-05

项目CRUD接口

实现：

新增

删除

修改

查询

分页

------

## Phase-3 Git模块

Task-06

集成JGit

支持：

Clone

Pull

Fetch

------

Task-07

实现仓库同步功能

同步项目源码到本地

------

## Phase-4 Java代码解析

Task-08

集成JavaParser

------

Task-09

扫描Controller

输出：

类名

方法名

注解

------

Task-10

扫描Service

输出：

类名

方法名

事务注解

------

Task-11

扫描Mapper调用关系

建立调用链

------

## Phase-5 规则引擎

Task-12

设计Rule接口

public interface RuleChecker

------

Task-13

实现RULE001

缺少@Transactional

------

Task-14

实现RULE002

缺少日志记录

------

Task-15

实现RULE003

Controller直接访问Mapper

------

Task-16

实现RULE004

System.out.println检测

------

Task-17

实现RULE005

超长方法检测

------

## Phase-6 Spring AI集成

Task-18

集成Spring AI

支持：

DeepSeek

OpenAI

------

Task-19

设计Prompt模板

输入：

规则结果

输出：

风险分析

问题解释

修复建议

------

Task-20

实现AI分析服务

AiAnalysisService

------

## Phase-7 报告系统

Task-21

生成Markdown报告

------

Task-22

生成PDF报告

------

Task-23

报告下载接口

------

## Phase-8 前端

Task-24

登录页

------

Task-25

项目管理页

------

Task-26

代码审查页

------

Task-27

审查结果页

------

Task-28

报告页

------

## Phase-9 优化

Task-29

Redis缓存

------

Task-30

Docker部署

------

## Phase-10 高级功能

Task-31

自动修复代码

------

Task-32

Git Commit

------

Task-33

自动生成PR

------

Task-34

Multi Agent架构



# =========================
# Phase-2 AI Code Reviewer Pro
# =========================

## Task-30 Auto Fix Engine

目标：

基于 RuleResult 自动生成代码修复建议。

功能：

- FixSuggestion模型
- AutoFixService
- FixPromptBuilder
- FixResponseParser
- Redis缓存

输出：

FixSuggestion

状态：

[x] Done

---

## Task-31 Patch Engine

目标：

根据 FixSuggestion 生成标准Git Patch。

功能：

- PatchGenerator
- DiffBuilder
- PatchPreview

输出：

review.patch

状态：

[x] Done

---

## Task-32 Agent Framework

目标：

构建统一Agent框架。

功能：

- Agent接口
- AgentContext
- AgentResult
- AgentType
- AgentOrchestrator

输出：

Agent Framework

状态：

[ ] Todo

---

## Task-33 Review Agent

目标：

封装代码审查能力。

功能：

- RuleEngine集成
- SpringAI集成
- ReviewAgent

输出：

ReviewAgentResult

状态：

[ ] Todo

---

## Task-34 Fix Agent

目标：

封装自动修复能力。

功能：

- AutoFixService集成
- PatchEngine集成
- FixAgent

输出：

FixAgentResult

状态：

[ ] Todo

---

## Task-35 Summary Agent

目标：

汇总多个Agent结果。

功能：

- SummaryAgent
- FinalReviewReport

输出：

统一分析报告

状态：

[ ] Todo