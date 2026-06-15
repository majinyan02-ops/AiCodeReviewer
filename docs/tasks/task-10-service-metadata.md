请阅读：

docs/01-product-spec.md
docs/02-system-design.md
docs/03-database-design.md
docs/04-api-design.md
docs/05-task-list.md
docs/06-core-architecture.md
CLAUDE.md

当前已完成：

Task-01 ~ Task-09

====================================================

开始完成：

Task-10

Service Metadata Analysis

====================================================

目标

基于 Task-08 构建的 AST 模型。

基于 Task-09 的 Metadata 设计经验。

实现 Service 元数据提取模块。

本模块只负责：

提取

分析

结构化输出

禁止实现规则检测。

禁止实现调用链分析。

禁止实现 AI 分析。

====================================================

新增包

analysis

├── model

├── service

└── service/impl

====================================================

创建：

ServiceMetadata

====================================================

字段

className

qualifiedName

classAnnotations

hasServiceAnnotation

hasTransactional

methods

totalMethodCount

transactionalMethodCount

totalLineCount

====================================================

创建：

ServiceMethodInfo

====================================================

字段

methodName

returnType

startLine

endLine

lineCount

hasTransactional

annotations

mapperCalls

====================================================

注意

mapperCalls 使用已有 AST 模型数据。

禁止重新解析源码。

====================================================

创建：

ServiceAnalysisService

====================================================

接口

List extractFromScanContext(ScanContext context)

List extractFromProjectCodeModel(ProjectCodeModel model)

====================================================

实现：

ServiceAnalysisServiceImpl

====================================================

规则

识别：

@Service

@Transactional

支持：

类级事务

方法级事务

====================================================

统计：

总方法数

事务方法数

总代码行数

====================================================

禁止

禁止重新扫描源码

禁止重新调用 JavaParser

禁止访问数据库

禁止实现 RuleChecker

禁止实现 CallGraph

====================================================

单元测试

创建：

ServiceAnalysisServiceTest

====================================================

验证：

识别 Service 类

识别事务方法

识别 Mapper 调用

统计信息正确

====================================================

输出要求

1. 输出新增文件列表
2. 输出目录结构
3. 输出 ServiceMetadata 示例
4. 输出统计结果示例
5. 输出单元测试结果
6. 更新 README
7. 更新 Task 状态

====================================================

完成后立即停止开发。

不要实现 Task-11。

不要实现 CallGraph。

不要新增 CallGraph 相关模型。