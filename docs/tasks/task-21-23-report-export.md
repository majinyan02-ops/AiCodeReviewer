# Task21~23 Report Export System

## 背景

当前已完成：

Task01~Task20

系统已具备：

Git仓库扫描

JavaParser解析

CallGraph分析

RuleEngine检测

SpringAI分析

ReviewReport生成

当前流程：

ReviewReport

↓

需要支持导出

---

## Task21

Markdown报告导出

### 新增

MarkdownReportGenerator

接口：

String generate(ReviewReport report)

输出：

Markdown文本

包含：

# 项目概览

# 问题统计

# 问题详情

# AI分析

# 总体建议

---

## Task22

PDF报告导出

### 新增

PdfReportGenerator

使用：

OpenPDF

或者

iTextPDF

输入：

ReviewReport

输出：

review-report.pdf

要求：

标题

统计信息

问题详情

AI建议

分页支持

---

## Task23

下载接口

### 新增

ReportController

接口：

GET /api/report/{id}/markdown

GET /api/report/{id}/pdf

返回：

application/octet-stream

支持浏览器下载

文件名：

review-report.md

review-report.pdf

---

## 架构要求

ReviewReport

↓

MarkdownReportGenerator

↓

PdfReportGenerator

↓

ReportController

禁止：

不要重新调用AI

不要重新扫描源码

不要修改RuleEngine

不要修改CallGraph

---

完成后输出：

1. 新增文件列表
2. 导出示例
3. PDF示例
4. 接口测试结果