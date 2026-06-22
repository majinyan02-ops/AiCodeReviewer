# Task-34 FixAgent

## 背景

当前系统已完成：

Task01~Task33

已具备：

- JavaParser
- ProjectCodeModel
- CallGraph
- RuleEngine
- Redis Cache
- AutoFix Engine
- Patch Engine
- Agent Framework
- ReviewAgent

当前架构：

ProjectCodeModel
        ↓
CallGraph
        ↓
RuleEngine
        ↓
ReviewAgent
        ↓
ReviewAgentResult

Task34目标：

实现 FixAgent。

---

# 核心目标

将：

AutoFixEngine

+

PatchEngine

封装为标准Agent。

形成：

ReviewAgent
      ↓
FixAgent

执行链路。

---

# 新增目录

src/main/java/com/aicode/agent/fix

├── FixAgent.java
│
├── model
│   ├── FixAgentResult.java
│   ├── FixItem.java
│   └── FixStatistics.java
│
├── prompt
│   └── FixPromptBuilder.java
│
└── parser
    └── FixResponseParser.java

---

# AgentType

使用已有：

AgentType.FIX

如不存在则新增。

---

# FixAgent

实现：

Agent

返回：

AgentType.FIX

---

# 输入

来自：

AgentContext

使用：

ProjectCodeModel

CallGraph

以及：

ReviewAgentResult

获取方式：

context.attributes

key：

reviewResult

类型：

ReviewAgentResult

---

# FixAgent执行流程

1.

获取：

ReviewAgentResult

2.

提取：

List<RuleResult>

3.

过滤：

passed=false

4.

逐条调用：

AutoFixEngine

生成：

FixSuggestion

5.

调用：

PatchEngine

生成：

PatchResult

6.

汇总：

FixItem

7.

统计：

FixStatistics

8.

构建：

FixAgentResult

9.

封装：

AgentResult

返回

---

# FixItem

字段：

```java
private String ruleId;

private String className;

private String methodName;

private String issue;

private String suggestion;

private String patchContent;

private boolean patchGenerated;

private Long generateDuration;
```

---

# FixStatistics

字段：

```java
private Integer totalIssues;

private Integer fixedIssues;

private Integer failedIssues;

private Double successRate;

private Long totalDuration;
```

---

# FixAgentResult

字段：

```java
private Integer totalIssues;

private Integer fixedIssues;

private Integer failedIssues;

private List<FixItem> fixItems;

private FixStatistics statistics;

private LocalDateTime generatedTime;
```

---

# FixPromptBuilder

职责：

构建修复Prompt。

输入：

RuleResult

输出：

Prompt

要求：

结构化

禁止自由发挥

必须要求返回JSON

---

# AI输出格式

```json
{
  "suggestion":"为save方法添加@Transactional",
  "fixedCode":"@Transactional public void save(){}"
}
```

---

# FixResponseParser

职责：

解析AI返回结果。

校验：

JSON合法性

字段完整性

异常处理

---

# Redis缓存

复用：

AnalysisCacheService

新增：

getFixResult()

putFixResult()

clearFixResult()

缓存Key：

agent:fix:{projectId}

TTL：

24小时

逻辑：

先查缓存

命中直接返回

---

# AgentResult

payload：

FixAgentResult

例如：

AgentResult.builder()
        .agentType(FIX)
        .success(true)
        .payload(fixAgentResult)
        .build();

---

# AgentOrchestrator集成

支持：

AgentType.FIX

通过：

AgentRegistry

自动发现。

禁止：

if else硬编码。

---

# Controller

新增：

POST

/api/agent/fix

请求：

{
    "projectId":"demo-project"
}

返回：

FixAgentResult

---

# 单元测试

新增：

FixAgentTest

测试：

- Agent执行
- AutoFixEngine调用
- PatchEngine调用
- Redis缓存
- AgentResult封装
- 异常处理

新增：

FixPromptBuilderTest

新增：

FixResponseParserTest

---

# 架构约束

禁止：

修改RuleEngine

修改ReviewAgent

修改JavaParser

禁止重新扫描源码

禁止直接访问AgentRegistry内部Map

统一通过AgentOrchestrator

---

# 输出要求

编码前先输出：

1. 新增文件列表

2. 类图

3. Agent执行流程图

4. Redis设计

5. 风险分析

确认后开始编码。