# Task-32 Agent Framework（Final Version）

请基于当前项目代码实现 Task-32。

在开始编码前请先阅读：

- CLAUDE.md
- docs/02-system-design.md
- docs/03-database-design.md
- docs/05-task-list.md
- docs/06-core-architecture.md
- docs/tasks/task30-autofix-engine.md
- docs/tasks/task31-patch-engine.md

并遵循项目已有代码风格。

------

# 目标

实现 Multi-Agent Framework。

注意：

本阶段仅实现 Agent Framework。

禁止实现：

- ReviewAgent
- FixAgent
- SummaryAgent

这些属于 Task33~35。

当前仅搭建框架。

------

# 架构目标

后续系统架构：

ProjectCodeModel
↓

CallGraph
↓

RuleEngine
↓

AgentOrchestrator

```
    ↓
```

┌─────────────┐
│ ReviewAgent │
└─────────────┘

┌─────────────┐
│ FixAgent │
└─────────────┘

┌─────────────┐
│SummaryAgent │
└─────────────┘

当前只实现：

AgentOrchestrator

AgentRegistry

AgentContext

AgentResult

AgentType

Agent接口

------

# 新增目录

src/main/java/com/aicode/agent

├── Agent.java
├── AgentContext.java
├── AgentResult.java
├── AgentType.java
├── AgentExecutionMode.java
├── AgentOrchestrator.java
│
└── registry
└── AgentRegistry.java

------

# Agent接口

统一Agent规范：

```java
public interface Agent {

    AgentType getType();

    default List<AgentType> dependencies() {
        return Collections.emptyList();
    }

    AgentResult execute(
            AgentContext context
    );
}
```

要求：

- 所有Agent必须实现
- dependencies()用于后续Task33~35
- 当前默认空实现

------

# AgentType

```java
public enum AgentType {

    REVIEW,

    FIX,

    SUMMARY
}
```

要求：

预留未来扩展：

- SECURITY
- PERFORMANCE
- ARCHITECTURE

暂时不要实现。

------

# AgentExecutionMode

新增：

```java
public enum AgentExecutionMode {

    SEQUENTIAL,

    PARALLEL
}
```

说明：

SEQUENTIAL：

Review
↓
Fix
↓
Summary

PARALLEL：

多个Agent并行执行。

本阶段仅预留能力。

不实现并行线程池。

------

# AgentContext

统一Agent输入。

字段：

```java
String projectId;

Long reviewId;

ProjectCodeModel projectCodeModel;

CallGraph callGraph;

List<RuleResult> ruleResults;

Map<String,Object> attributes;
```

要求：

- Lombok
- Builder
- 支持扩展

------

# AgentResult

统一Agent输出。

禁止使用：

```java
Map<String,Object> data
```

作为核心返回对象。

改为：

```java
public class AgentResult {

    private AgentType agentType;

    private boolean success;

    private String message;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration;

    private Object payload;
}
```

说明：

payload用于承载：

- ReviewAgentResult
- FixAgentResult
- SummaryAgentResult

后续Task33~35直接扩展。

------

# AgentRegistry

职责：

统一管理Agent。

要求：

使用Spring自动发现。

推荐实现：

```java
@RequiredArgsConstructor
@Component
public class AgentRegistry {

    private final List<Agent> agentList;

}
```

禁止：

ApplicationContext手动扫描。

------

# Agent注册逻辑

启动时：

```java
@PostConstruct
public void init()
```

遍历：

```java
List<Agent>
```

注册到：

```java
EnumMap<AgentType, Agent>
```

------

# 重复Agent检测

必须实现：

```java
Agent old =
    registry.put(
        agent.getType(),
        agent
    );

if (old != null) {
    throw new IllegalStateException(
        "Duplicate AgentType: "
        + agent.getType()
    );
}
```

要求：

系统启动即发现配置错误。

禁止覆盖注册。

------

# AgentRegistry接口

至少提供：

```java
Agent getAgent(
        AgentType type
);

Collection<Agent> getAllAgents();

boolean contains(
        AgentType type
);
```

------

# AgentOrchestrator

职责：

统一调度Agent。

新增：

```java
executeAgent()

executeAgents()
```

支持：

单Agent执行。

多Agent执行。

顺序执行。

------

# executeAgent

```java
AgentResult executeAgent(
        AgentType type,
        AgentContext context
)
```

流程：

Registry
↓
获取Agent
↓
执行Agent
↓
返回结果

------

# executeAgents

```java
List<AgentResult> executeAgents(
        List<AgentType> types,
        AgentContext context,
        AgentExecutionMode mode
)
```

当前：

仅实现：

SEQUENTIAL

PARALLEL模式：

抛出：

```java
UnsupportedOperationException
```

等待未来实现。

------

# Controller

新增测试接口：

POST

/api/agent/execute

请求：

```json
{
  "agentType":"REVIEW"
}
```

返回：

AgentResult

仅用于框架验证。

------

# 单元测试

新增：

AgentRegistryTest

验证：

- Agent注册
- 重复Agent检测
- 获取Agent

新增：

AgentOrchestratorTest

验证：

- 单Agent执行
- 多Agent执行
- 顺序执行
- 异常处理

------

# 架构约束

禁止：

实现ReviewAgent

实现FixAgent

实现SummaryAgent

修改RuleEngine

修改CallGraph

修改AutoFixService

修改PatchEngine

------

# 输出要求

完成后输出：

1. 新增文件列表
2. Agent类图
3. Agent执行流程图
4. Agent注册机制说明
5. 扩展方案
6. 测试结果
7. git diff统计

确认编译通过后停止开发。