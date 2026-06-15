# AI Code Reviewer 开发日志

## 日期

2026-06-15 (续)

------

## 今日目标

完成 Task-09 Controller 元数据提取、Task-10 Service 元数据提取。

------

## 已完成任务

### Task-09 Controller 元数据提取

新增文件：

- `analysis/model/EndpointInfo.java`
- `analysis/model/ControllerMetadata.java`
- `analysis/service/ControllerAnalysisService.java`
- `analysis/service/impl/ControllerAnalysisServiceImpl.java`

成果：

```
GET    /api/users/save   -> save()   (line 9)
PUT    /api/users/update -> update() (line 14)
DELETE /api/users/{id}   -> delete() (line 19)
```

------

### Task-10 Service 元数据提取

新增文件：

- `analysis/model/ServiceMethodInfo.java`
- `analysis/model/ServiceMetadata.java`
- `analysis/service/ServiceAnalysisService.java`
- `analysis/service/impl/ServiceAnalysisServiceImpl.java`

成果：

```
UserServiceImpl:
  hasServiceAnnotation: true
  hasTransactional (class): true
  totalMethodCount: 4
  transactionalMethodCount: 4   ← 类级事务，所有方法都是事务方法
  totalLineCount: 14
  save()  transactional=true  mapperCalls=[userMapper.insert]
  update() transactional=true  mapperCalls=[userMapper.update]
  query()  transactional=true  mapperCalls=[userMapper.select]
  delete() transactional=true  mapperCalls=[userMapper.delete]

OrderServiceImpl:
  hasServiceAnnotation: true
  hasTransactional (class): false
  totalMethodCount: 3
  transactionalMethodCount: 1   ← 仅 cancel() 方法有 @Transactional
  totalLineCount: 10
  create() transactional=false mapperCalls=[orderMapper.insert]
  cancel() transactional=true  mapperCalls=[orderMapper.update]
  query()  transactional=false mapperCalls=[orderMapper.select]
```

3 个测试全部通过（含 Task-08 回归测试）。

------

## 架构进展

```
Source Code
    ↓
JavaParser (Task-08)
    ↓
ProjectCodeModel / ScanContext
    ↓
┌─────────────────────────────────┐
│ ControllerAnalysisService (T09) │ → ControllerMetadata
│ ServiceAnalysisService (T10)    │ → ServiceMetadata
└─────────────────────────────────┘
    ↓
CallGraph (Task-11, 待开发)
    ↓
Rule Engine (Task-12+, 待开发)
```

------

## 目录结构

```
src/main/java/com/aicode/analysis/
├── model/
│   ├── ControllerMetadata.java
│   ├── EndpointInfo.java
│   ├── ServiceMetadata.java
│   └── ServiceMethodInfo.java
├── service/
│   ├── ControllerAnalysisService.java
│   ├── ServiceAnalysisService.java
│   └── impl/
│       ├── ControllerAnalysisServiceImpl.java
│       └── ServiceAnalysisServiceImpl.java
```

------

## 明日计划

Task-11 Call Graph 调用链分析

- MethodCallInfo 模型
- ScannedMethod.serviceCalls 扩展
- CallGraph / CallNode / CallEdge 模型
- CallGraphService
