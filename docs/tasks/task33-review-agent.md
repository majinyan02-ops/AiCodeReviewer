# Task-33 ReviewAgent

## 背景

当前系统已完成：

Task01~Task32

已具备：

- JavaParser
- ProjectCodeModel
- CallGraph
- RuleEngine
- Spring AI
- Redis Cache
- Auto Fix Engine
- Patch Engine
- Agent Framework

当前Agent Framework：

- Agent
- AgentType
- AgentContext
- AgentResult
- AgentRegistry
- AgentOrchestrator

已经支持统一Agent调度。

------

# 目标

实现：

ReviewAgent

将现有：

RuleEngine

- 

Spring AI分析能力

封装为标准Agent。

------

# 架构定位

ProjectCodeModel
↓

CallGraph
↓

RuleEngine
↓

RuleResult
↓

Spring AI
↓

ReviewAgent

```
    ↓
```

ReviewAgentResult

------

# 新增目录

src/main/java/com/aicode/agent/review

├── ReviewAgent.java
│
├── model
│ └── ReviewAgentResult.java
│
├── prompt
│ └── ReviewPromptBuilder.java
│
└── parser
└── ReviewResponseParser.java

------

# AgentType

新增：

```java
REVIEW
```

若已存在无需修改。

------

# ReviewAgent

实现：

```java
Agent
```

接口：

```java
public class ReviewAgent implements Agent
```

实现：

```java
AgentType getType()
```

返回：

```java
AgentType.REVIEW
```

------

# Agent输入

来自：

AgentContext

使用：

```java
ProjectCodeModel

CallGraph
```

------

# Agent执行流程

步骤：

1. 

读取：

ProjectCodeModel

1. 

读取：

CallGraph

1. 

调用：

RuleEngine

1. 

获取：

List

1. 

调用：

Spring AI

1. 

生成：

ReviewAgentResult

1. 

封装：

AgentResult

返回

------

# ReviewAgentResult

字段：

```java
private Integer totalRules;

private Integer errorCount;

private Integer warningCount;

private Integer infoCount;

private Integer overallScore;

private String riskLevel;

private String summary;

private List<RuleResult> ruleResults;

private LocalDateTime generatedTime;
```

------

# 评分规则

建议：

100分起步。

ERROR：

-10

WARNING：

-5

INFO：

-1

最低：

0

例如：

3 ERROR

2 WARNING

100

- 

30

- 

10

=

60

------

# 风险等级

90+

LOW

70~89

MEDIUM

50~69

HIGH

<50

CRITICAL

------

# ReviewPromptBuilder

职责：

构建AI分析Prompt。

输入：

```java
List<RuleResult>
```

输出：

Prompt String

要求：

结构化。

禁止自由发挥。

禁止输出Markdown。

------

# AI输出格式

严格要求：

```json
{
  "summary":"项目存在事务缺失问题",
  "riskLevel":"HIGH"
}
```

------

# ReviewResponseParser

职责：

解析AI结果。

校验：

JSON合法性

字段完整性

异常处理

------

# Redis缓存

Key：

```text
agent:review:{projectId}
```

TTL：

24小时

缓存：

ReviewAgentResult

逻辑：

先查缓存。

缓存不存在再执行。

------

# AgentResult

payload：

```java
ReviewAgentResult
```

例如：

```java
AgentResult.builder()
        .agentType(REVIEW)
        .success(true)
        .payload(reviewAgentResult)
        .build();
```

------

# AgentOrchestrator集成

支持：

```java
AgentType.REVIEW
```

自动执行。

无需硬编码if else。

通过：

AgentRegistry

获取Agent。

------

# Controller

新增：

POST

/api/agent/review

请求：

```json
{
  "projectId":"demo-project"
}
```

返回：

ReviewAgentResult

------

# 单元测试

新增：

ReviewAgentTest

测试：

- Agent执行
- RuleEngine调用
- AI分析
- Redis缓存
- AgentResult封装
- 异常处理

------

# 架构约束

禁止：

修改RuleEngine

修改CallGraph

修改JavaParser

修改AutoFixService

修改PatchEngine

禁止直接调用AgentRegistry内部Map。

统一通过AgentOrchestrator。

------

# 输出要求

先输出：

1. 新增文件列表
2. 类图
3. Agent执行流程图
4. Redis设计
5. 风险分析

等待确认后再开始编码。