# Task-29 Redis Cache Layer

## 背景

当前已完成：

Task01~Task28

系统已具备：

ProjectCodeModel

CallGraph

RuleEngine

Spring AI

ReviewReport

前端系统

当前存在问题：

重复扫描同一项目时性能较差。

JavaParser

CallGraph

RuleEngine

AI分析

存在重复计算。

------

## 目标

引入Redis缓存层。

减少重复解析。

减少AI调用。

提升系统性能。

------

## 技术栈

Spring Cache

Redis

Jackson

------

## 缓存对象

### 一级缓存

ProjectCodeModel

Key：

project:{projectId}:code-model

TTL：

30分钟

------

### 二级缓存

CallGraph

Key：

project:{projectId}:callgraph

TTL：

30分钟

------

### 三级缓存

RuleResult

Key：

project:{projectId}:rule-result

TTL：

30分钟

------

### 四级缓存

AiIssueAnalysis

Key：

ai:analysis:{hash}

TTL：

24小时

------

## 新增配置

RedisConfig

配置：

RedisTemplate

Jackson序列化

CacheManager

统一TTL

------

## 新增服务

CacheService

接口：

get

put

evict

exists

------

## 集成位置

ProjectAnalysisService

CallGraphService

RuleEngine

AiReviewService

优先读取缓存。

缓存不存在再执行计算。

执行成功后写入缓存。

------

## 缓存失效

项目重新扫描：

自动删除：

code-model

callgraph

rule-result

AI缓存保留

------

## 日志

记录：

Cache Hit

Cache Miss

缓存命中率

------

## 输出

新增文件列表

缓存架构图

Redis Key设计

测试结果

性能优化说明

完成后停止开发