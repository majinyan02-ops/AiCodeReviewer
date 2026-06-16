# Task-19 AI Analysis Enhancement

## 背景

当前已完成：

Task01~Task18

系统已具备：

- JavaParser AST解析
- Controller Metadata
- Service Metadata
- CallGraph
- RuleEngine
- 5条内置规则
- Spring AI集成

当前流程：

RuleEngine
↓
RuleResult
↓
PromptBuilder
↓
ChatClient
↓
AiReviewResult

## 目标

增强AI分析能力。

让AI不仅返回简单建议。

而是返回：

风险等级

问题原因

影响分析

修复建议

示例代码

## 新增模型

AiIssueAnalysis

字段：

ruleId

ruleName

riskLevel

reason

impact

suggestion

exampleFix

## 新增服务

AiAnalysisService

接口：

AiIssueAnalysis analyze(RuleResult ruleResult)

List analyzeBatch(List results)

## 实现

AiAnalysisServiceImpl

依赖：

ChatClient

PromptBuilder

## Prompt设计

要求AI输出JSON格式：

{
"riskLevel": "",
"reason": "",
"impact": "",
"suggestion": "",
"exampleFix": ""
}

必须返回结构化结果。

禁止返回自由文本。

## JSON解析

新增：

AiResponseParser

负责：

AI响应

↓

AiIssueAnalysis

## 批量分析

支持：

List

↓

并发分析

↓

List

使用：

CompletableFuture

提升分析效率。

## 禁止

不要实现Controller

不要实现Report

不要实现Markdown导出

不要实现前端

## 输出

1. 新增文件列表
2. 类图
3. Prompt示例
4. AI返回示例
5. 测试结果

完成后停止开发。