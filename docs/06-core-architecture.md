# Core Architecture Design

## Purpose

本项目从 Task-08 开始进入核心能力开发阶段。

后续模块：

- JavaParser
- Call Graph
- Rule Engine
- Spring AI
- Auto Fix
- Multi Agent

必须遵循统一架构。

禁止后续任务绕过本设计。

------

# Overall Flow

Source Code

↓

JavaParser

↓

ProjectCodeModel

↓

CallGraph

↓

Rule Engine

↓

RuleResult

↓

AI Analysis

↓

AnalysisResult

↓

Report

------

# Layer Definition

## Parser Layer

负责：

源码解析

禁止：

规则检测

AI分析

数据库操作

------

输入：

源码目录

输出：

ProjectCodeModel

------

# Core Model

## ProjectCodeModel

表示整个项目源码结构。

包含：

ClassModel列表

------

## ClassModel

表示类信息。

字段：

- packageName
- className
- fullClassName
- classType
- annotations
- fields
- methods

------

classType支持：

- CONTROLLER
- SERVICE
- ENTITY
- MAPPER
- COMPONENT
- NORMAL

------

## MethodModel

表示方法信息。

字段：

- methodName
- returnType
- annotations
- methodCalls
- startLine
- endLine
- lineCount

------

## FieldModel

表示字段信息。

字段：

- fieldName
- fieldType
- annotations

------

## AnnotationModel

表示注解信息。

字段：

- name
- attributes

------

# Call Graph Layer

Task-11实现。

负责：

建立调用关系。

例如：

UserController

↓

UserService

↓

UserMapper

输出：

CallGraph

------

CallGraph结构：

Map<String,List>

------

# Rule Engine Layer

Task-12实现。

负责：

执行规则检测。

禁止直接解析源码。

必须基于：

ProjectCodeModel

CallGraph

进行分析。

------

统一接口：

RuleChecker

------

定义：

RuleResult

------

所有规则必须实现：

RuleChecker

------

# AI Layer

Task-18实现。

负责：

解释规则结果。

禁止直接扫描源码。

输入：

RuleResult

输出：

AnalysisResult

------

统一入口：

AiAnalysisService

------

# Prompt Layer

Task-19实现。

所有Prompt统一管理。

目录：

resources/prompts

------

禁止：

业务代码硬编码Prompt

------

必须：

PromptService统一加载

------

# Report Layer

输入：

RuleResult

AnalysisResult

输出：

Markdown

PDF

------

# Development Constraints

禁止：

Parser直接调用AI

Rule直接调用数据库

AI直接解析源码

Controller直接调用RuleChecker

------

必须遵循：

Parser

↓

ProjectCodeModel

↓

RuleEngine

↓

AiAnalysisService

↓

ReportService

------

# Future Extension

支持：

- Auto Fix
- Git Commit
- Pull Request
- Multi Agent

所有扩展必须基于：

ProjectCodeModel

CallGraph

RuleResult

AnalysisResult

不得重新设计数据结构。