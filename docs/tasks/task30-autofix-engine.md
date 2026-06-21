# Task-30 Auto Fix Engine

## 背景

当前系统已经完成：

Task01~Task29

已具备能力：

Git仓库扫描

JavaParser源码解析

ProjectCodeModel构建

CallGraph分析

RuleEngine规则检测

Spring AI问题分析

Markdown/PDF报告

Redis缓存

当前流程：

Source Code
    ↓
JavaParser
    ↓
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
Issue Analysis

系统只能发现问题并给出建议。

下一阶段需要实现：

AI自动代码修复能力。

---

# 目标

实现 Auto Fix Engine。

支持：

RuleResult
    ↓
AI生成修复代码
    ↓
FixSuggestion

注意：

本阶段不修改源码。

本阶段不生成Patch。

本阶段只负责生成修复建议。

---

# 架构设计

新增模块：

com.aicode.fix

目录结构：

src/main/java/com/aicode/fix

├── model
│   └── FixSuggestion.java
│
├── service
│   ├── AutoFixService.java
│   └── impl
│       └── AutoFixServiceImpl.java
│
├── prompt
│   └── FixPromptBuilder.java
│
└── parser
    └── FixResponseParser.java

---

# 数据流

RuleResult
    ↓

FixPromptBuilder

    ↓

Spring AI

    ↓

FixResponseParser

    ↓

FixSuggestion

---

# 新增模型

FixSuggestion

字段：

ruleId

ruleName

severity

originalCode

fixedCode

explanation

confidence

riskLevel

generatedTime

示例：

{
    "ruleId":"RULE-004",

    "ruleName":"SystemOutPrintln",
    
    "originalCode":
    "System.out.println(user);",
    
    "fixedCode":
    "log.info(\"user={}\", user);",
    
    "explanation":
    "使用Slf4j日志框架替代System.out",
    
    "confidence":0.95,
    
    "riskLevel":"LOW"
}

---

# AutoFixService

接口：

public interface AutoFixService {

    FixSuggestion generateFix(
        RuleResult ruleResult
    );

}

---

# PromptBuilder

新增：

FixPromptBuilder

职责：

根据RuleResult构建AI提示词。

输入：

RuleResult

输出：

Prompt String

要求：

包含：

规则信息

问题描述

修复目标

输出格式要求

禁止自由发挥

必须输出JSON

---

# AI输出格式

必须要求AI严格返回：

{
  "originalCode":"",

  "fixedCode":"",

  "explanation":"",

  "confidence":0.90,

  "riskLevel":"LOW"
}

禁止Markdown。

禁止代码块。

禁止自然语言解释。

---

# ResponseParser

新增：

FixResponseParser

职责：

解析AI返回JSON。

转换为：

FixSuggestion

要求：

异常处理

JSON校验

字段缺失校验

日志记录

---

# AutoFixServiceImpl

职责：

1.

调用PromptBuilder

2.

调用Spring AI

3.

获取AI结果

4.

调用ResponseParser

5.

返回FixSuggestion

---

# Controller

新增：

AutoFixController

接口：

POST

/api/fix/generate

请求：

{
    "ruleResultId":1
}

返回：

FixSuggestion

---

# Redis缓存

新增缓存：

Key：

fix:{ruleId}:{hash}

TTL：

24小时

缓存：

FixSuggestion

要求：

先查缓存。

缓存不存在再调用AI。

生成后写入缓存。

---

# 日志

记录：

Fix Generate Start

Fix Generate Success

Fix Generate Fail

Cache Hit

Cache Miss

---

# 单元测试

新增：

AutoFixServiceTest

测试：

正常生成

缓存命中

AI返回异常

JSON解析异常

---

# 架构约束

禁止：

修改源码

生成Patch

执行Git命令

写文件

修改RuleEngine

修改CallGraph

修改JavaParser

---

# 输出要求

完成设计后先输出：

1. 新增文件列表

2. 类图

3. 数据流图

4. API设计

5. Redis Key设计

6. 风险分析

等待确认后再开始编码