# Phase-9 开发日志

## Task-36/37: 分析记录持久化 + 历史报告管理

### 新增文件

| 文件 | 说明 |
|------|------|
| `sql/V9__add_analysis_record.sql` | 分析记录表DDL |
| `src/main/java/com/aicode/entity/AnalysisRecord.java` | 分析记录实体 |
| `src/main/java/com/aicode/mapper/AnalysisRecordMapper.java` | Mapper（含趋势查询SQL） |
| `src/main/java/com/aicode/service/AnalysisRecordService.java` | Service接口 |
| `src/main/java/com/aicode/service/impl/AnalysisRecordServiceImpl.java` | Service实现 |
| `src/main/java/com/aicode/controller/AnalysisRecordController.java` | REST API |
| `src/main/java/com/aicode/dto/AnalysisRecordDTO.java` | 列表DTO |
| `src/main/java/com/aicode/dto/AnalysisRecordDetailDTO.java` | 详情DTO |
| `src/main/java/com/aicode/dto/AnalysisRecordQueryRequest.java` | 查询请求DTO |
| `src/main/java/com/aicode/common/PageResult.java` | 通用分页结果 |
| `src/main/java/com/aicode/vo/TrendDataVO.java` | 趋势数据VO |
| `src/main/java/com/aicode/vo/StatisticsOverviewVO.java` | 统计概览VO |
| `frontend/src/api/analysis-record.ts` | 前端API模块 |
| `frontend/src/views/history/History.vue` | 历史列表页 |
| `frontend/src/views/history/HistoryDetail.vue` | 历史详情页 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `src/main/java/com/aicode/AiCodeReviewerApplication.java` | 添加@EnableAsync |
| `src/main/java/com/aicode/agent/controller/AgentController.java` | executeBatch后异步持久化 |
| `frontend/src/types/api.ts` | 新增Phase-9类型定义 |
| `frontend/src/router/index.ts` | 新增history/history/:id/trend路由 |
| `frontend/src/layout/components/AppSidebar.vue` | 新增历史记录/趋势统计导航 |

### 数据库变更

新增表 `analysis_record`：
- 项目ID、项目名、总体评分、健康评分
- 问题分布统计列（error/warning/info count）
- 修复统计（fixedIssues/fixSuccessRate）
- SummaryAgentResult JSON列
- 报告路径（markdown_path/pdf_path）
- 索引：project_id, create_time, health_score, project_time复合

### API变更

| Method | Path | 说明 |
|--------|------|------|
| GET | /api/analysis-record/page | 分页查询 |
| GET | /api/analysis-record/{id} | 详情 |
| DELETE | /api/analysis-record/{id} | 删除 |
| DELETE | /api/analysis-record/batch | 批量删除 |
| GET | /api/analysis-record/trend/{projectId} | 趋势数据 |
| GET | /api/analysis-record/trend/overview | 全项目对比 |
| GET | /api/analysis-record/statistics/overview | 统计概览 |

---

## Task-38: 趋势统计分析

### 新增文件

| 文件 | 说明 |
|------|------|
| `frontend/src/views/trend/Trend.vue` | 趋势统计页（ECharts） |

### 功能

- 健康评分趋势折线图
- 问题数量堆叠面积图（Error/Warning/Info）
- 修复率趋势折线图
- 项目健康对比柱状图
- 支持项目选择和时间范围（7天/30天/90天）
