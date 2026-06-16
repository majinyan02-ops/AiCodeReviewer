请阅读：

docs/01-product-spec.md
docs/02-system-design.md
docs/03-database-design.md
docs/04-api-design.md
docs/05-task-list.md
docs/06-core-architecture.md
CLAUDE.md

当前已完成：

Task-01 ~ Task-10

====================================================

开始完成：

Task-11

Call Graph 调用链分析

====================================================

目标

基于 Task-08 构建的 AST 模型。

基于 Task-09、Task-10 的 Metadata 设计经验。

实现 Controller → Service → Mapper 调用链分析。

本模块只负责：

建立调用关系图

禁止实现规则检测。

禁止实现 AI 分析。

====================================================

架构约束

Parser Layer 负责源码解析。

Call Graph Layer 基于 ProjectCodeModel 构建。

禁止 Call Graph 重新解析源码。

禁止 Call Graph 访问数据库。

调用链运行时动态计算，不预存 chains。

====================================================

Phase A: Parser 扩展（本次需要实施）

A1. 新增模型：MethodCallInfo

包：com.aicode.analysis.model

字段

targetClass   String   目标类名
targetMethod  String   目标方法名
callType      String   SERVICE_CALL / MAPPER_CALL

使用 Lombok @Data @Builder @NoArgsConstructor @AllArgsConstructor。

A2. 修改 ScannedMethod

新增字段

@Builder.Default
private List<MethodCallInfo> serviceCalls = new ArrayList<>();

注意

不删除现有 mapperCalls 字段。

serviceCalls 使用 List<MethodCallInfo>，不是 List<String>。

A3. 修改 JavaParserServiceImpl

在 buildScannedMethod() 的 VoidVisitorAdapter 中新增 service call 检测：

遍历 MethodCallExpr 时，若 scope 名称以 "Service" 结尾（忽略大小写），
构建 MethodCallInfo：

targetClass  = scope 名称
targetMethod = 方法名
callType     = "SERVICE_CALL"

同时改造 mapperCalls：

将现有的 List<String> mapperCalls 改为同样构建 MethodCallInfo，
callType = "MAPPER_CALL"。

====================================================

Phase B: CallGraph 模型

B1. 新增模型：CallNode

包：com.aicode.analysis.model

字段

nodeId         String   唯一标识: qualifiedClassName.methodName
className      String   简单类名
qualifiedName  String   全限定类名
methodName     String   方法名
classType      String   Controller / Service / Mapper
filePath       String   源文件路径
lineNumber     int      方法起始行号

B2. 新增模型：CallEdge

包：com.aicode.analysis.model

字段

callerId         String   调用方 nodeId
calleeId         String   被调用方 nodeId
callerClassName  String   调用方类名
calleeClassName  String   被调用方类名
callerMethodName String   调用方方法名
calleeMethodName String   被调用方方法名
callType         String   SERVICE_CALL / MAPPER_CALL
lineNumber       int      调用发生的行号（用于规则定位）

B3. 新增模型：CallGraph

包：com.aicode.analysis.model

字段

projectId   Long                 项目 ID
nodes       Map<String, CallNode> 所有节点，key = nodeId
edges       List<CallEdge>        所有有向边
rootNodes   List<String>          入口节点 nodeId 列表（Controller 方法）
totalNodes  int                   节点总数（派生）
totalEdges  int                   边总数（派生）

注意

不包含 chains 字段。

调用链运行时动态计算（从 rootNodes 出发，DFS 遍历 edges）。

====================================================

Phase C: CallGraphService

C1. 接口：CallGraphService

包：com.aicode.analysis.service

方法

CallGraph buildFromScanContext(ScanContext context)

CallGraph buildFromProjectCodeModel(ProjectCodeModel model)

C2. 实现：CallGraphServiceImpl

包：com.aicode.analysis.service.impl

核心逻辑

Step 1: 构建节点索引

遍历所有 Controller、Service、Mapper 类的方法，
为每个方法创建 CallNode，存入 Map<String, CallNode>。

同时构建类名查找索引：

Map<String, String> classNameIndex
  key   = 简单类名（小写）
  value = qualifiedName

例如 "userserviceimpl" → "com.example.service.impl.UserServiceImpl"

Step 2: 构建边

遍历 Controller 方法的 serviceCalls：

根据 targetClass 在 classNameIndex 中查找目标 qualifiedName，
构建 nodeId = qualifiedName.targetMethod，
创建 CallEdge（callType = SERVICE_CALL）。

遍历 Service 方法的 mapperCalls：

同样方式构建 MethodCallInfo，
创建 CallEdge（callType = MAPPER_CALL）。

Step 3: 识别根节点

rootNodes = 所有 Controller 方法的 nodeId，
且没有作为 calleeId 出现在任何边中。

Step 4: 组装 CallGraph

====================================================

类名匹配策略

当 scope 名称为 "userService" 时：

1. 精确匹配：查找 classNameIndex 中 key = "userservice"
2. 后缀匹配：查找 key = "userserviceimpl"
3. 前缀匹配：查找 key 以 "user" 开头且包含 "service"

优先使用全限定名中包路径相近的类。

====================================================

禁止

禁止重新扫描源码

禁止重新调用 JavaParser

禁止访问数据库

禁止实现 RuleChecker

禁止实现 AI 分析

禁止在 CallGraph 中预存 chains

====================================================

单元测试

创建：

CallGraphServiceTest

验证：

识别 Controller → Service 调用边

识别 Service → Mapper 调用边

根节点为 Controller 方法

节点数和边数正确

====================================================

输出要求

1. 输出新增文件列表
2. 输出修改文件列表
3. 输出目录结构
4. 输出 CallGraph 示例
5. 输出单元测试结果
6. 更新 dev-log
7. 更新 Task 状态

====================================================

完成后立即停止开发。

不要实现 Task-12。

不要实现 RuleChecker。

不要实现 Rule Engine。
