开始实现 Task18 Spring AI Integration

目标：

将 RuleEngine 输出的 RuleResult 接入 Spring AI。

架构：

RuleResult
↓
PromptBuilder
↓
Spring AI ChatClient
↓
AiReviewService
↓
AiReviewResult

要求：

1. 新建 ai 包
2. 创建：

AiReviewRequest
AiReviewResult
AiIssueAnalysis

1. 创建：

PromptBuilder

负责将 RuleResult 转换为 Prompt

1. 创建：

AiReviewService

1. 创建：

AiReviewServiceImpl

使用 Spring AI ChatClient

1. 支持：

RULE-001
RULE-002
RULE-003
RULE-004
RULE-005

统一分析

1. 返回结构化结果

禁止：

不要实现 Controller

不要实现 Report

不要实现前端

不要实现 Markdown 导出

完成后停止开发。