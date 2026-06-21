# AI Code Reviewer 前端开发总结

## 1. 当前完成状态

当前前端已经完成 `frontend-spec.md` 中 `Task-01` 到 `Task-07` 的开发范围，并额外补齐联调必需的注册入口、Git 仓库同步和仓库状态查看能力。系统具备企业级后台的基础架构、登录鉴权、项目管理、代码审查、报告中心、统一 API 层和统一类型定义。

已完成任务：

- Task-01：初始化项目架构
- Task-02：实现登录页面与用户状态
- Task-03：实现 Dashboard
- Task-04：实现项目管理页面
- Task-05：实现代码审查页面
- Task-06：实现报告中心
- Task-07：统一 API 层、类型定义和异常处理

开发日志已写入：

- `dev-log/task01.md`
- `dev-log/task02.md`
- `dev-log/task03.md`
- `dev-log/task04.md`
- `dev-log/task05.md`
- `dev-log/task06.md`
- `dev-log/task07.md`

## 2. 技术栈

当前前端使用：

- Vue 3
- TypeScript
- Vite
- Pinia
- Vue Router
- Axios
- Element Plus
- ECharts

项目构建命令：

```bash
npm run build
```

开发启动命令：

```bash
npm run dev
```

## 3. 项目结构

当前主要目录结构：

```text
src
├── api              # 后端接口封装
├── hooks            # 组合式 Hook
├── layout           # 后台整体布局
├── router           # 路由与鉴权守卫
├── store            # Pinia 状态管理
├── styles           # 全局样式
├── types            # 统一类型定义
├── utils            # 请求、下载、报告缓存等工具
├── views            # 页面模块
└── App.vue
```

关键模块说明：

- `src/api`：统一封装认证、项目、审查、报告接口。
- `src/utils/request.ts`：Axios 实例、请求拦截、响应解包、401 处理。
- `src/store/user.ts`：JWT、用户信息、登录、自动恢复、退出登录状态管理。
- `src/router/index.ts`：路由注册和登录拦截。
- `src/layout`：Header、Sidebar、MainContent、Footer 后台布局。
- `src/types/api.ts`：统一维护前后端交互数据类型。

## 4. 已实现页面

### 4.1 登录页

路径：

```text
/login
```

已实现：

- 用户名、密码表单
- 表单校验
- 登录 loading
- 注册账号弹窗
- 注册表单校验
- 对接后端 `/api/auth/register`
- 对接后端 `/api/auth/login`
- 登录成功后保存 JWT
- 自动获取当前用户信息 `/api/auth/me`
- 登录后跳转目标页或 `/dashboard`

### 4.2 Dashboard

路径：

```text
/dashboard
```

已实现：

- 项目数量
- 扫描数量
- 问题数量
- ERROR / WARNING / INFO 数量
- 最近项目
- 最近报告
- ECharts 问题等级分布图
- ECharts 通过率统计图

数据来源：

- 项目数据来自后端 `/api/project/page`
- 报告统计来自前端报告缓存

### 4.3 项目管理

路径：

```text
/project
```

已实现：

- 项目列表
- 显示项目 ID
- 新增项目
- 编辑项目
- 删除项目
- 分页
- 前端搜索
- Dialog 表单
- 同步 Git 仓库
- 查看 Git 仓库状态

已对接接口：

```text
GET    /api/project/page
POST   /api/project
PUT    /api/project/{id}
DELETE /api/project/{id}
POST   /api/git/sync/{projectId}
GET    /api/git/status/{projectId}
```

### 4.4 代码审查

路径：

```text
/review
```

已实现：

- 选择项目
- 开始扫描
- 扫描步骤展示
- 扫描结果元信息
- 异步提交报告生成任务
- 每 500ms 轮询报告生成进度
- 进度条展示当前阶段和百分比
- SUCCESS 后渲染报告结果
- 规则检测结果表格
- AI 分析结果折叠面板
- 总体评分
- 风险等级
- 问题等级统计图
- 生成报告后写入前端报告缓存

已对接接口：

```text
GET  /api/project/page
GET  /api/scan/{projectId}
POST /api/report/generate/{projectId}/async
GET  /api/report/progress/{taskId}
```

### 4.5 报告中心

路径：

```text
/report
```

已实现：

- 报告列表
- 搜索
- 分页
- 报告详情 Drawer
- Markdown 预览
- Markdown 下载
- PDF 下载
- 本地报告记录删除

已对接接口：

```text
GET /api/report/{projectId}/markdown
GET /api/report/{projectId}/pdf
```

说明：

当前后端暂未提供历史报告分页列表接口，因此报告中心使用前端本地缓存展示已经生成过的报告记录。

## 5. 登录与鉴权流程

当前鉴权流程：

```text
用户登录
  ↓
POST /api/auth/login
  ↓
保存 JWT 到 localStorage
  ↓
GET /api/auth/me 获取用户信息
  ↓
进入后台页面
```

路由守卫规则：

- 未登录访问后台页面：跳转 `/login`
- 已登录访问 `/login`：自动跳转 `/dashboard`
- 页面刷新后：根据本地 JWT 自动恢复用户信息
- Token 失效或后端返回 401：清理登录态并跳转登录页

## 6. API 与数据结构设计

当前所有后端请求都通过 `src/api` 层访问，不在页面中直接使用 Axios。

Axios 统一处理：

- 请求自动携带 `Authorization: Bearer <token>`
- 响应自动解包后端 `Result<T>`
- 非 200 业务码统一提示错误
- 401 自动清理登录态并跳转登录页

统一类型集中在：

```text
src/types/api.ts
```

主要类型包括：

- `ApiPage<T>`
- `Project`
- `ProjectForm`
- `GitStatus`
- `ScanContext`
- `RuleResult`
- `AiIssueAnalysis`
- `IssueSummary`
- `ReviewReport`
- `ReportResponse`
- `ReportTaskProgress`
- `CachedReport`

## 7. 前后端联调配置

前端开发服务：

```text
http://localhost:5173
```

后端服务：

```text
http://localhost:8080
```

Vite 已配置代理：

```text
/api -> http://localhost:8080
```

因此前端代码中统一请求：

```text
/api/xxx
```

联调时需要启动：

```bash
# 后端目录
cd D:\Desktop\AiCodeReviewer
mvn spring-boot:run

# 前端目录
cd D:\Desktop\AiCodeReviewer-Web
npm run dev
```

## 8. 当前构建状态

已执行：

```bash
npm run build
```

构建结果：

- TypeScript 编译通过
- Vite 构建通过
- 前端产物正常生成

当前存在构建警告：

- 依赖包 `@vueuse/core` 的 pure annotation 警告
- 部分 chunk 超过 500KB 的体积警告

这些警告不阻塞构建，也不影响当前联调。

## 9. 当前限制与注意事项

### 9.1 报告历史列表

后端当前没有完整的历史报告分页接口，所以报告中心使用前端本地缓存。

影响：

- 换浏览器或清理缓存后，报告中心历史记录会消失。
- 不影响 Markdown / PDF 下载接口本身。

### 9.2 代码扫描依赖源码目录

后端扫描接口读取路径：

```text
./git-repos/{projectId}
```

因此联调代码审查前，需要确保对应项目 ID 下存在 Java 源码目录。

### 9.3 AI 分析依赖后端环境变量

如果要完整生成 AI 分析，需要后端正确配置：

```text
AI_API_KEY
AI_BASE_URL
AI_MODEL
```

否则报告生成阶段可能失败。

## 10. 推荐测试顺序

建议按以下顺序联调：

1. 注册用户
2. 登录后台
3. 刷新页面验证自动登录
4. 退出登录验证状态清理
5. 新增项目
6. 编辑项目
7. 删除项目
8. Dashboard 查看项目统计
9. 准备 `git-repos/{projectId}` 源码目录
10. 代码审查开始扫描
11. 生成报告
12. 报告中心查看详情
13. 下载 Markdown
14. 下载 PDF

## 11. 后续建议

如果继续增强项目，优先级建议如下：

1. 后端补充历史报告分页接口，替换前端本地报告缓存。
2. 项目管理表格显示项目 ID，方便联调扫描目录。
3. 增加 Git 同步入口，对接后端 Git 模块。
4. Dashboard 改为后端统计接口，避免前端聚合数据不完整。
5. 对 ECharts 做按页面拆包，降低主包体积。