# AI Code Reviewer 开发日志

## 日期

2026-06-16

------

## 今日目标

完成 Task-11 Call Graph + Task-12 RuleChecker 接口 + Task-13~17 五条规则。

------

## 已完成任务

### Task-11 Call Graph 调用链分析

详见 day-03.md。

### Task-12 RuleChecker 接口设计

新增文件：
- `rule/RuleChecker.java` — 规则检测统一接口
- `rule/model/RuleResult.java` — 检测结果模型

### Task-13~17 规则实现

新增文件：
- `rule/RuleEngine.java` — 规则引擎编排
- `rule/impl/Rule001MissingTransactional.java`
- `rule/impl/Rule002MissingLogging.java`
- `rule/impl/Rule003ControllerDirectMapper.java`
- `rule/impl/Rule004SystemOutPrintln.java`
- `rule/impl/Rule005LongMethod.java`

修改文件：
- `parser/model/ScannedMethod.java` — 新增 `hasLogging` 字段
- `parser/impl/JavaParserServiceImpl.java` — 新增日志检测

### 五条规则

| 规则 | 名称 | 严重程度 | 数据来源 |
|---|---|---|---|
| RULE-001 | 缺少@Transactional | ERROR | ScannedMethod.hasTransactional |
| RULE-002 | 缺少日志记录 | WARNING | ScannedMethod.hasLogging |
| RULE-003 | Controller直接访问Mapper | ERROR | CallGraph edges |
| RULE-004 | System.out.println检测 | WARNING | ScannedMethod.hasSysOut |
| RULE-005 | 超长方法检测 | WARNING/ERROR | ScannedMethod.lineCount > 50 |

### 检测结果示例

```
[RULE-001] ERROR   UserServiceImpl.save()   缺少 @Transactional
[RULE-001] ERROR   UserServiceImpl.update() 缺少 @Transactional
[RULE-002] WARNING OrderServiceImpl.query() 缺少日志记录
[RULE-003] ERROR   UserController.delete()  Controller 直接调用 Mapper
[RULE-004] WARNING UserServiceImpl.update() System.out.println
```

3 个测试全部通过。

------

## 目录结构

```
src/main/java/com/aicode/
├── analysis/     (Task-09/10/11)
├── parser/       (Task-08)
├── rule/         (Task-12~17)
│   ├── RuleChecker.java
│   ├── RuleEngine.java
│   ├── model/
│   │   └── RuleResult.java
│   └── impl/
│       ├── Rule001MissingTransactional.java
│       ├── Rule002MissingLogging.java
│       ├── Rule003ControllerDirectMapper.java
│       ├── Rule004SystemOutPrintln.java
│       └── Rule005LongMethod.java
```

------

## 明日计划

Task-18 Spring AI 集成
