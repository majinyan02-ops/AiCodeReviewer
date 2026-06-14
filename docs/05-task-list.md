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