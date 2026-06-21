# Task-31 Patch Engine

## 背景

当前系统已经完成：

Task01~Task30

已具备能力：

- Git仓库扫描
- JavaParser源码解析
- ProjectCodeModel构建
- CallGraph分析
- RuleEngine规则检测
- Spring AI问题分析
- Redis缓存
- Auto Fix Engine

当前流程：

Source Code
↓
RuleEngine
↓
RuleResult
↓
AutoFixService
↓
FixSuggestion

系统已经能够生成修复建议。

下一阶段需要实现：

标准 Git Patch 生成能力。

------

# 目标

实现 Patch Engine。

支持：

FixSuggestion
↓
PatchGenerator
↓
PatchResult

输出标准 Diff/Patch 内容。

注意：

本阶段不修改源码。

本阶段不执行 Git 命令。

本阶段不自动提交 Commit。

仅生成 Patch。

------

# 架构设计

新增模块：

com.aicode.patch

目录结构：

src/main/java/com/aicode/patch

├── model
│ ├── PatchResult.java
│ └── PatchFile.java
│
├── service
│ ├── PatchService.java
│ └── impl
│ └── PatchServiceImpl.java
│
├── generator
│ └── PatchGenerator.java
│
├── validator
│ └── PatchValidator.java
│
└── controller
└── PatchController.java

------

# 数据流

FixSuggestion
↓

PatchGenerator

```
  ↓
```

PatchValidator

```
  ↓
```

PatchResult

------

# PatchResult

字段：

patchId

filePath

originalCode

fixedCode

patchContent

valid

generatedTime

示例：

{
"patchId":"PATCH-001",

```
"filePath":"UserService.java",

"originalCode":
"System.out.println(user);",

"fixedCode":
"log.info(\"user={}\", user);",

"patchContent":
"--- UserService.java\n+++ UserService.java\n@@\n- System.out.println(user);\n+ log.info(\"user={}\", user);",

"valid":true
```

}

------

# PatchFile

字段：

filePath

patchContent

lineCount

------

# PatchGenerator

职责：

根据：

FixSuggestion

生成：

标准 Diff 格式。

输出：

PatchResult

示例：

--- UserService.java
+++ UserService.java

@@

- System.out.println(user);

- log.info("user={}", user);

------

# PatchValidator

职责：

校验 Patch 合法性。

校验内容：

1. 

originalCode不能为空

1. 

fixedCode不能为空

1. 

patchContent不能为空

1. 

Diff格式正确

1. 

长度限制校验

返回：

boolean

------

# PatchService

接口：

public interface PatchService {

```
PatchResult generatePatch(
    FixSuggestion suggestion
);
```

}

------

# PatchServiceImpl

职责：

1. 

接收 FixSuggestion

1. 

调用 PatchGenerator

1. 

调用 PatchValidator

1. 

返回 PatchResult

------

# Controller

新增：

PatchController

接口：

POST

/api/patch/generate

请求：

{
"fixSuggestionId":1
}

返回：

PatchResult

------

# Redis缓存

新增：

Key：

patch:{fixId}

TTL：

24小时

缓存：

PatchResult

逻辑：

先查缓存

缓存不存在再生成

生成成功写入缓存

------

# 日志

记录：

Patch Generate Start

Patch Generate Success

Patch Generate Fail

Patch Validate Success

Patch Validate Fail

Cache Hit

Cache Miss

------

# 单元测试

新增：

PatchServiceTest

测试：

1. 

正常生成Patch

1. 

缓存命中

1. 

非法Patch

1. 

空代码内容

1. 

Patch格式校验

------

# 架构约束

禁止：

修改源码

执行Git命令

提交Commit

Push代码

修改RuleEngine

修改CallGraph

修改AutoFixService

------

# 输出要求

先输出：

1. 新增文件列表
2. 类图
3. 数据流图
4. Patch格式设计
5. Redis Key设计
6. 风险分析

等待确认后再开始编码