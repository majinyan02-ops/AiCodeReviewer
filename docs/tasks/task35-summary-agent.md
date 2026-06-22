# Task-35 SummaryAgent

## 背景

当前系统已完成：

Task01 ~ Task34

已具备：

- JavaParser AST解析
- ProjectCodeModel
- CallGraph
- RuleEngine
- Spring AI
- Redis Cache
- AutoFix Engine
- Patch Engine
- Agent Framework
- ReviewAgent
- FixAgent

当前Agent链路：

ProjectCodeModel
↓

CallGraph
↓

ReviewAgent
↓

ReviewAgentResult

```
    ↓
```

FixAgent
↓

FixAgentResult

Task35目标：

实现 SummaryAgent。

------

# 核心目标

SummaryAgent 不负责：

- 规则检测
- AI修复
- Patch生成

这些已经由：

ReviewAgent

FixAgent

完成。

SummaryAgent职责：

统一汇总。

生成最终项目分析报告。

------

# 架构定位

ReviewAgentResult
↓

FixAgentResult
↓

SummaryAgent

```
    ↓
```

SummaryAgentResult

------

# Agent职责

SummaryAgent负责：

1. 

汇总 ReviewAgentResult

1. 

汇总 FixAgentResult

1. 

计算整体统计指标

1. 

生成最终项目报告

1. 

输出系统级建议

1. 

为前端 Dashboard 提供统一数据

------

# 新增目录

src/main/java/com/aicode/agent/summary

├── SummaryAgent.java
│
├── model
│ ├── SummaryAgentResult.java
│ ├── SummaryStatistics.java
│ └── ProjectHealthReport.java
│
└── controller
└── SummaryAgentController.java

------

# AgentType

使用：

AgentType.SUMMARY

------

# Agent输入

来自：

AgentContext.attributes

获取：

reviewResult

fixResult

类型：

ReviewAgentResult

FixAgentResult

------

# SummaryAgent执行流程

1. 

获取：

ReviewAgentResult

1. 

获取：

FixAgentResult

1. 

统计：

ERROR数量

WARNING数量

INFO数量

1. 

统计：

总问题数

成功修复数

失败修复数

修复成功率

1. 

计算：

项目健康度

1. 

生成：

ProjectHealthReport

1. 

构建：

SummaryAgentResult

1. 

封装：

AgentResult

返回

------

# ProjectHealthReport

字段：

private String healthLevel;

private Integer healthScore;

private String summary;

private List strengths;

private List weaknesses;

private List recommendations;

------

# HealthScore计算

基础分：

100

ERROR：

-10

WARNING：

-5

INFO：

-1

修复成功：

+2

最终：

0 ~ 100

------

# HealthLevel

90+

EXCELLENT

80~89

GOOD

60~79

FAIR

40~59

POOR

<40

CRITICAL

------

# SummaryStatistics

字段：

private Integer totalIssues;

private Integer errorCount;

private Integer warningCount;

private Integer infoCount;

private Integer fixedIssues;

private Integer failedIssues;

private Double fixSuccessRate;

private Long reviewAiDuration;

private Long fixAiDuration;

private Long totalAiDuration;

------

# SummaryAgentResult

字段：

private SummaryStatistics statistics;

private ProjectHealthReport healthReport;

private ReviewAgentResult reviewResult;

private FixAgentResult fixResult;

private LocalDateTime generatedTime;

------

# Redis缓存

复用：

AnalysisCacheService

新增：

getSummaryResult()

putSummaryResult()

clearSummaryResult()

缓存Key：

agent:summary:{projectId}

TTL：

24小时

------

# AgentOrchestrator集成

支持：

AgentType.SUMMARY

通过：

AgentRegistry

自动发现

禁止：

if else硬编码

------

# Controller

新增：

POST

/api/agent/summary

请求：

{
"projectId":"demo-project"
}

返回：

SummaryAgentResult

------

# Dashboard目标

SummaryAgentResult 要能够直接支撑前端展示：

项目评分

风险等级

问题数量

修复数量

修复成功率

健康度

改进建议

无需前端再次计算。

------

# 架构约束

禁止：

重新执行RuleEngine

重新执行ReviewAgent

重新执行FixAgent

禁止重新扫描源码

禁止调用JavaParser

SummaryAgent只能消费：

ReviewAgentResult

FixAgentResult

------

# 输出要求

先不要编码。

请先输出：

1. 新增文件列表
2. 类图
3. Agent执行流程图
4. SummaryStatistics设计
5. ProjectHealthReport设计
6. Dashboard数据结构设计
7. 风险分析

确认后再开始编码。