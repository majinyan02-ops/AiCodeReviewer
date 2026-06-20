# CLAUDE.md

## Project

AI Code Reviewer

企业级AI代码审查与自动修复平台

------

## Tech Stack

Backend

- Java 21
- Spring Boot 3.5
- Spring AI
- Spring Security
- MyBatis Plus
- MySQL
- Redis

Frontend

- Vue3
- TypeScript
- Element Plus

AI

- Spring AI
- DeepSeek
- OpenAI

------

## Architecture Rules

严格遵守分层架构

Controller

负责：

- 参数接收
- 参数校验

禁止：

业务逻辑

------

Service

负责：

业务逻辑

------

Mapper

负责：

数据库访问

禁止：

业务逻辑

------

DTO

用于接口请求响应

------

Entity

对应数据库表

------

VO

用于前端展示

------

## Coding Rules

使用Lombok

使用构造器注入

禁止字段注入

禁止使用@Autowired

统一使用@RequiredArgsConstructor

------

所有接口返回：

Result

禁止直接返回对象

------

统一异常处理

GlobalExceptionHandler

------

所有数据库操作：

MyBatis Plus

------

所有时间字段：

LocalDateTime

------

## AI Rules

所有Prompt统一管理

目录：

prompt/

禁止在业务代码硬编码Prompt

------

Prompt模板化

支持变量替换

------

AI返回结果必须结构化

JSON格式

禁止自由文本解析

------

## Development Rules

每完成一个Task：

1. 编译通过
2. 单元测试通过
3. 更新README
4. 更新Task状态

------

在开发新Task之前：

先阅读：

docs/

01-product-spec.md

02-system-design.md

03-database-design.md

04-api-design.md

05-task-list.md

严格按照文档开发

禁止擅自修改数据库结构

禁止擅自新增业务模块



从 Task-08 开始：

开发前必须阅读：

docs/06-core-architecture.md

禁止：

重新设计AST模型

重新设计RuleResult

重新设计AnalysisResult

禁止绕过：

ProjectCodeModel

CallGraph

RuleEngine

AiAnalysisService

所有后续开发必须基于：

ProjectCodeModel
CallGraph
RuleResult
AnalysisResult

进行扩展。





我的前端页面：D:\Desktop\AiCodeReviewer-Web