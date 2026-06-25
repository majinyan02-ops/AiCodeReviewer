# AI Code Reviewer

 AI 代码审查与自动修复平台

[![Java](https://img.shields.io/badge/Java-21-green)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-blue)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 项目简介

AI Code Reviewer 是一个基于 **规则引擎 + Spring AI + 静态代码分析** 的智能代码审查平台。平台通过自动化分析 Java 项目源码，检测代码规范问题，并利用 AI 生成修复建议和健康评估报告。

### 核心能力

- **Git 项目管理** — 支持 Git 仓库克隆、拉取、同步
- **Java 代码解析** — 基于 JavaParser 的 AST 静态分析
- **规则引擎检测** — 6 条内置规则，自动检测代码违规
- **AI 智能分析** — Spring AI 集成 DeepSeek/OpenAI，生成修复建议
- **Multi-Agent 架构** — ReviewAgent → FixAgent → SummaryAgent 流水线
- **报告生成** — Markdown / PDF 格式报告自动输出
- **趋势统计** — 健康评分、问题分布、修复率趋势图表
- **历史记录** — 分析记录持久化，支持查询、删除

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.5 | 核心框架 |
| Spring AI | 1.0.0-M6 | AI 集成 |
| Spring Security | 6.5 | JWT 认证 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 7.0+ | 缓存 |
| JGit | 7.1.0 | Git 操作 |
| JavaParser | 3.26.3 | Java 代码解析 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 响应式框架 |
| TypeScript | 类型安全 |
| Element Plus | UI 组件库 |
| ECharts | 数据可视化 |
| Pinia | 状态管理 |
| Vite | 构建工具 |

### AI

| 技术 | 说明 |
|------|------|
| DeepSeek | 默认 AI 模型 |
| OpenAI | 备选 AI 模型 |

---

## 项目结构

```
AiCodeReviewer/
├── src/main/java/com/aicode/
│   ├── agent/                    # Multi-Agent 框架
│   │   ├── Agent.java            # Agent 接口
│   │   ├── AgentOrchestrator.java # Agent 调度器
│   │   ├── review/               # ReviewAgent
│   │   ├── fix/                  # FixAgent
│   │   └── summary/              # SummaryAgent
│   ├── ai/                       # AI 分析服务
│   ├── analysis/                 # 调用图分析
│   ├── config/                   # 配置类
│   ├── controller/               # REST 控制器
│   ├── dto/                      # 数据传输对象
│   ├── entity/                   # 数据库实体
│   ├── exception/                # 异常处理
│   ├── fix/                      # 自动修复服务
│   ├── mapper/                   # MyBatis Mapper
│   ├── patch/                    # 补丁生成服务
│   ├── parser/                   # JavaParser 解析
│   ├── project/                  # 项目管理
│   ├── report/                   # 报告生成
│   ├── rule/                     # 规则引擎
│   ├── security/                 # JWT 安全
│   ├── service/                  # 业务服务
│   └── vo/                       # 视图对象
├── src/main/resources/
│   └── application.yml           # 应用配置
├── sql/
│   ├── init.sql                  # 数据库初始化
│   └── V9__add_analysis_record.sql
├── frontend/                     # Vue3 前端
│   ├── src/
│   │   ├── api/                  # API 调用
│   │   ├── views/                # 页面组件
│   │   ├── types/                # TypeScript 类型
│   │   └── router/               # 路由配置
│   └── package.json
└── pom.xml
```

---

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/AiCodeReviewer.git
cd AiCodeReviewer
```

### 2. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
mysql -u root -p ai_code_reviewer < sql/V9__add_analysis_record.sql
```

### 3. 配置环境变量

```bash
# AI 配置
export AI_API_KEY=your-api-key
export AI_BASE_URL=https://api.deepseek.com
export AI_MODEL=deepseek-chat

# JWT 配置（可选，有默认值）
export JWT_SECRET=your-jwt-secret

# Git 存储路径（可选，默认 ./git-repos）
export GIT_STORAGE_PATH=./git-repos
```

或修改 `src/main/resources/application.yml`。

### 4. 启动后端

```bash
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`

---

## 功能模块

### 1. 项目管理

- 创建、编辑、删除项目
- 关联 Git 仓库
- 项目状态管理

### 2. Agent 智能分析

一键执行完整的代码审查流水线：

```
ReviewAgent → FixAgent → SummaryAgent
```

- **ReviewAgent** — 规则引擎检测 + AI 风险分析
- **FixAgent** — AI 生成修复建议 + 补丁预览
- **SummaryAgent** — 健康评分 + 趋势统计

### 3. 报告中心

- 查看所有分析报告
- 问题列表详情
- Markdown / PDF 报告下载

### 4. 历史记录

- 分析记录持久化
- 按项目、风险等级、健康等级筛选
- 支持批量删除

### 5. 趋势统计

- 健康评分趋势图
- 问题数量分布图
- 修复率趋势图
- 项目健康对比图

---

## 规则引擎

平台内置 6 条代码规范检测规则：

| 规则 ID | 规则名称 | 严重等级 |
|---------|---------|---------|
| RULE-001 | 缺少 @Transactional | ERROR |
| RULE-002 | 缺少日志记录 | WARNING |
| RULE-003 | 捕获 Exception 后未处理 | WARNING |
| RULE-004 | 存在 System.out.println | WARNING |
| RULE-005 | 方法长度超过 100 行 | WARNING |
| RULE-006 | Controller 直接操作数据库 | ERROR |

---

## API 接口

### 认证

| Method | Path | 说明 |
|--------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/register | 注册 |

### 项目管理

| Method | Path | 说明 |
|--------|------|------|
| GET | /api/project/page | 分页查询 |
| POST | /api/project | 创建项目 |
| PUT | /api/project/{id} | 更新项目 |
| DELETE | /api/project/{id} | 删除项目 |

### Agent 分析

| Method | Path | 说明 |
|--------|------|------|
| POST | /api/agent/review | 执行 ReviewAgent |
| POST | /api/agent/fix | 执行 FixAgent |
| POST | /api/agent/summary | 执行 SummaryAgent |

### 报告

| Method | Path | 说明 |
|--------|------|------|
| GET | /api/agent/report/markdown/{projectId} | 下载 Markdown 报告 |
| GET | /api/agent/report/pdf/{projectId} | 下载 PDF 报告 |

### 分析记录

| Method | Path | 说明 |
|--------|------|------|
| GET | /api/analysis-record/page | 分页查询 |
| GET | /api/analysis-record/{id} | 详情 |
| DELETE | /api/analysis-record/{id} | 删除 |
| GET | /api/analysis-record/trend/{projectId} | 趋势数据 |
| GET | /api/analysis-record/statistics/overview | 统计概览 |

---

## 数据库设计

### 核心表

- **user** — 用户表
- **project** — 项目表
- **review_task** — 审查任务表
- **review_result** — 审查结果表
- **rule_config** — 规则配置表
- **review_report** — 报告表
- **analysis_record** — 分析记录表（Phase-9 新增）

### ER 关系

```
user → project → review_task → review_result
                               → review_report
                               → analysis_record
rule_config → review_result
```

---

## 开发指南

### 编码规范

- 使用 Lombok 简化代码
- 构造器注入，禁止 `@Autowired`
- 统一返回 `Result<T>` 封装
- 统一异常处理 `GlobalExceptionHandler`
- 时间字段统一使用 `LocalDateTime`

### 分层架构

```
Controller → Service → Mapper
    ↓           ↓         ↓
  参数校验    业务逻辑   数据库访问
```

### 提交规范

每次提交前确保：
1. 编译通过
2. 单元测试通过
3. 更新相关文档

---

## 许可证

MIT License

---

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [JavaParser](https://javaparser.org/)
- [DeepSeek](https://deepseek.com/)
