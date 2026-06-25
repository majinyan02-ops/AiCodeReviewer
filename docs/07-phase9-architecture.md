# Phase-9 Enterprise Features Architecture Design

## Overview

Phase-9 adds three enterprise capabilities:
- **Task-36**: Analysis record persistence — survive server restarts
- **Task-37**: History report management — browse, search, view, delete
- **Task-38**: Trend statistics — quality health score, issue count, fix rate over time

## Design Principles

- **PROHIBITED**: Modifying JavaParser, RuleEngine, Agent interface, AgentRegistry, AgentOrchestrator
- **Strategy**: Intercept results AFTER the agent pipeline completes, persist asynchronously
- **Backward compatible**: Redis cache remains primary read path; DB is durable backup
- **Serialization**: Complex nested objects (ReviewAgentResult, FixAgentResult, SummaryAgentResult) stored as JSON in `TEXT`/`LONGTEXT` columns
- **Single record per run**: One `analysis_record` row = one complete agent pipeline execution

---

## 1. Database Design

### Table: `analysis_record`

One row per complete agent pipeline run. Stores all three agent results as JSON.

```sql
CREATE TABLE IF NOT EXISTS `analysis_record` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `project_id`          BIGINT       NOT NULL COMMENT '项目ID',
    `project_name`        VARCHAR(100) DEFAULT NULL COMMENT '项目名称(冗余,便于查询)',
    `task_id`             BIGINT       DEFAULT NULL COMMENT '关联review_task ID',
    `overall_score`       INT          DEFAULT NULL COMMENT '总体评分(0-100)',
    `risk_level`          VARCHAR(20)  DEFAULT NULL COMMENT '风险等级: HIGH/MEDIUM/LOW',
    `health_level`        VARCHAR(20)  DEFAULT NULL COMMENT '健康等级: EXCELLENT/GOOD/FAIR/POOR/CRITICAL',
    `health_score`        INT          DEFAULT NULL COMMENT '健康评分(0-100)',
    `total_issues`        INT          DEFAULT 0 COMMENT '总问题数',
    `error_count`         INT          DEFAULT 0 COMMENT '错误数',
    `warning_count`       INT          DEFAULT 0 COMMENT '警告数',
    `info_count`          INT          DEFAULT 0 COMMENT '信息数',
    `fixed_issues`        INT          DEFAULT 0 COMMENT '已修复数',
    `fix_success_rate`    DOUBLE       DEFAULT 0 COMMENT '修复成功率(%)',
    `review_result_json`  LONGTEXT     DEFAULT NULL COMMENT 'ReviewAgentResult JSON',
    `fix_result_json`     LONGTEXT     DEFAULT NULL COMMENT 'FixAgentResult JSON',
    `summary_result_json` LONGTEXT     DEFAULT NULL COMMENT 'SummaryAgentResult JSON',
    `ai_duration`         BIGINT       DEFAULT 0 COMMENT 'AI总耗时(ms)',
    `status`              VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS/FAILED/PARTIAL',
    `error_message`       TEXT         DEFAULT NULL COMMENT '失败原因',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_overall_score` (`overall_score`),
    KEY `idx_health_score` (`health_score`),
    KEY `idx_project_time` (`project_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析记录表';
```

**Design rationale**:
- Denormalized stats columns (`overall_score`, `health_score`, `total_issues`, etc.) enable fast trend queries WITHOUT parsing JSON
- JSON columns store the full nested objects for detail views
- `project_id + create_time` composite index supports efficient trend queries

### ER Relationship (updated)

```
user → project → review_task → review_result
                               → review_report
                               → analysis_record (NEW)
rule_config → review_result
```

---

## 2. Entity Classes

### 2.1 `AnalysisRecord` (new)

**Package**: `com.aicode.entity`

```java
package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("analysis_record")
public class AnalysisRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String projectName;

    private Long taskId;

    private Integer overallScore;

    private String riskLevel;

    private String healthLevel;

    private Integer healthScore;

    private Integer totalIssues;

    private Integer errorCount;

    private Integer warningCount;

    private Integer infoCount;

    private Integer fixedIssues;

    private Double fixSuccessRate;

    private String reviewResultJson;

    private String fixResultJson;

    private String summaryResultJson;

    private Long aiDuration;

    private String status;

    private String errorMessage;

    private LocalDateTime createTime;
}
```

---

## 3. Mapper Interfaces

### 3.1 `AnalysisRecordMapper` (new)

**Package**: `com.aicode.mapper`

```java
package com.aicode.mapper;

import com.aicode.entity.AnalysisRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnalysisRecordMapper extends BaseMapper<AnalysisRecord> {

    /**
     * 查询项目最近N条记录的趋势数据(仅取统计列,不加载JSON)
     * 用于趋势图表渲染
     */
    @Select("""
        SELECT id, project_id, overall_score, health_score,
               total_issues, error_count, warning_count, info_count,
               fixed_issues, fix_success_rate, create_time
        FROM analysis_record
        WHERE project_id = #{projectId} AND status = 'SUCCESS'
        ORDER BY create_time DESC
        LIMIT #{limit}
    """)
    List<AnalysisRecord> selectTrendData(@Param("projectId") Long projectId,
                                         @Param("limit") int limit);
}
```

---

## 4. Service Layer

### 4.1 `AnalysisRecordService` (interface, new)

**Package**: `com.aicode.service`

```java
package com.aicode.service;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.common.PageResult;
import com.aicode.dto.AnalysisRecordDTO;
import com.aicode.dto.AnalysisRecordQueryRequest;
import com.aicode.dto.TrendDataVO;

import java.util.List;

public interface AnalysisRecordService {

    /**
     * Task-36: 保存一次完整的Agent管线执行结果
     */
    void saveRecord(Long projectId, String projectName, Long taskId,
                    ReviewAgentResult reviewResult,
                    FixAgentResult fixResult,
                    SummaryAgentResult summaryResult,
                    Long aiDuration,
                    String status, String errorMessage);

    /**
     * Task-37: 分页查询历史记录
     */
    PageResult<AnalysisRecordDTO> queryRecords(AnalysisRecordQueryRequest request);

    /**
     * Task-37: 查看单条记录详情(含完整JSON)
     */
    AnalysisRecordDTO getRecordDetail(Long recordId);

    /**
     * Task-37: 删除单条记录
     */
    void deleteRecord(Long recordId);

    /**
     * Task-37: 批量删除记录
     */
    void batchDeleteRecords(List<Long> recordIds);

    /**
     * Task-38: 获取项目趋势数据
     */
    TrendDataVO getProjectTrend(Long projectId, int limit);

    /**
     * Task-38: 获取所有项目的概览趋势(对比用)
     */
    List<TrendDataVO.ProjectTrendSummary> getProjectTrendSummaries();
}
```

### 4.2 `AnalysisRecordServiceImpl` (impl, new)

**Package**: `com.aicode.service.impl`

**Key implementation notes**:
1. **`saveRecord`**: Uses Jackson ObjectMapper to serialize the three result objects to JSON, then inserts via MyBatis Plus. Stats columns are extracted from SummaryAgentResult for fast queries.
2. **`queryRecords`**: Builds MyBatis Plus `LambdaQueryWrapper` dynamically from the query request. Returns `PageResult` (wrapping MyBatis Plus `IPage`).
3. **`getRecordDetail`**: Loads full record, deserializes JSON columns back to typed objects via `AnalysisRecordDetailDTO`.
4. **`getProjectTrend`**: Calls mapper's `selectTrendData`, reverses to chronological order, wraps in `TrendDataVO`.
5. Uses `@Transactional` for batch delete.

---

## 5. Controller Layer

### 5.1 `AnalysisRecordController` (new)

**Package**: `com.aicode.controller`

**Base path**: `/api/analysis-record`

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| `GET` | `/page` | 分页查询历史记录 | Query params: `projectId, keyword, riskLevel, startDate, endDate, page, size` | `Result<PageResult<AnalysisRecordDTO>>` |
| `GET` | `/{id}` | 查看记录详情 | Path param: `id` | `Result<AnalysisRecordDetailDTO>` |
| `DELETE` | `/{id}` | 删除记录 | Path param: `id` | `Result<Void>` |
| `DELETE` | `/batch` | 批量删除 | Request body: `List<Long> ids` | `Result<Void>` |
| `GET` | `/trend/{projectId}` | 项目趋势数据 | Path: `projectId`, Query: `limit` (default 30) | `Result<TrendDataVO>` |
| `GET` | `/trend/overview` | 所有项目趋势概览 | None | `Result<List<ProjectTrendSummary>>` |

---

## 6. DTO / VO Design

### 6.1 DTOs (new)

**Package**: `com.aicode.dto`

#### `AnalysisRecordDTO` — list view item (no JSON fields)

```java
package com.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecordDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long taskId;
    private Integer overallScore;
    private String riskLevel;
    private String healthLevel;
    private Integer healthScore;
    private Integer totalIssues;
    private Integer errorCount;
    private Integer warningCount;
    private Integer infoCount;
    private Integer fixedIssues;
    private Double fixSuccessRate;
    private Long aiDuration;
    private String status;
    private LocalDateTime createTime;
}
```

#### `AnalysisRecordDetailDTO` — detail view (with deserialized JSON)

```java
package com.aicode.dto;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.ProjectHealthReport;
import com.aicode.agent.summary.model.SummaryStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecordDetailDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long taskId;
    private Integer overallScore;
    private String riskLevel;
    private String healthLevel;
    private Integer healthScore;
    private Integer totalIssues;
    private Integer errorCount;
    private Integer warningCount;
    private Integer infoCount;
    private Integer fixedIssues;
    private Double fixSuccessRate;
    private Long aiDuration;
    private String status;
    private LocalDateTime createTime;

    // Deserialized agent results
    private ReviewAgentResult reviewResult;
    private FixAgentResult fixResult;
    private SummaryStatistics summaryStatistics;
    private ProjectHealthReport healthReport;
}
```

#### `AnalysisRecordQueryRequest` — query params

```java
package com.aicode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecordQueryRequest {
    private Long projectId;
    private String keyword;
    private String riskLevel;
    private String healthLevel;
    private String startDate;
    private String endDate;
    private Integer page = 1;
    private Integer size = 20;
}
```

### 6.2 VO (new)

#### `TrendDataVO` — for trend charts

```java
package com.aicode.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataVO {
    private Long projectId;
    private String projectName;
    private List<TrendPoint> points;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private Integer overallScore;
        private Integer healthScore;
        private Integer totalIssues;
        private Integer errorCount;
        private Integer warningCount;
        private Integer infoCount;
        private Integer fixedIssues;
        private Double fixSuccessRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectTrendSummary {
        private Long projectId;
        private String projectName;
        private Integer latestScore;
        private Integer latestHealthScore;
        private Double scoreChange;
        private Integer totalRecords;
    }
}
```

#### `PageResult<T>` (new, generic)

```java
package com.aicode.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int size;
    private int current;
    private int pages;
}
```

---

## 7. Integration Point: Where to Persist

### Strategy: Post-pipeline persistence in `AgentController` / `ReviewAgentController`

**The agent pipeline flow currently**:
1. `ReviewAgentController.review()` → calls `agentOrchestrator.executeAgent(REVIEW, context)` → returns `AgentResult`
2. Agent results go to Redis cache via `AnalysisCacheService`
3. No DB persistence

**New flow** (non-invasive):
Create a new service `AgentPipelinePersistenceService` that the existing controllers call AFTER the pipeline completes. This does NOT modify AgentOrchestrator.

### 7.1 `AgentPipelinePersistenceService` (new)

**Package**: `com.aicode.agent`

```java
package com.aicode.agent;

import com.aicode.agent.fix.model.FixAgentResult;
import com.aicode.agent.review.model.ReviewAgentResult;
import com.aicode.agent.summary.model.SummaryAgentResult;
import com.aicode.service.AnalysisRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentPipelinePersistenceService {

    private final AnalysisRecordService analysisRecordService;
    private final ObjectMapper objectMapper;

    /**
     * Pipeline完成后异步持久化到DB
     * 在ReviewAgentController / AgentController的批量执行后调用
     */
    @Async
    public void persistAfterPipeline(String projectId,
                                      String projectName,
                                      Long taskId,
                                      ReviewAgentResult reviewResult,
                                      FixAgentResult fixResult,
                                      SummaryAgentResult summaryResult,
                                      Long aiDuration) {
        try {
            analysisRecordService.saveRecord(
                Long.parseLong(projectId), projectName, taskId,
                reviewResult, fixResult, summaryResult,
                aiDuration, "SUCCESS", null
            );
            log.info("分析记录已持久化: projectId={}", projectId);
        } catch (Exception e) {
            log.error("持久化分析记录失败: projectId={}", projectId, e);
        }
    }

    @Async
    public void persistFailure(String projectId, String projectName,
                                Long taskId, Long aiDuration, String errorMessage) {
        try {
            analysisRecordService.saveRecord(
                Long.parseLong(projectId), projectName, taskId,
                null, null, null,
                aiDuration, "FAILED", errorMessage
            );
        } catch (Exception e) {
            log.error("持久化失败记录出错: projectId={}", projectId, e);
        }
    }
}
```

### 7.2 Modification Point: `ReviewAgentController`

The only existing file that needs modification (add 2 lines):

In `ReviewAgentController.review()`, after the agent pipeline returns results, add:

```java
// After existing logic...
agentPipelinePersistenceService.persistAfterPipeline(
    projectId, null, null,
    reviewResult, null, null,
    result.getDuration()
);
```

Similarly, a new `AgentPipelineController` can orchestrate the full pipeline (Review → Fix → Summary) with persistence at the end.

### 7.3 New: `AgentPipelineController`

**Package**: `com.aicode.agent.controller`

**Base path**: `/api/agent/pipeline`

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| `POST` | `/execute/{projectId}` | Run full pipeline + persist | Path: `projectId`, Query: `projectName` (optional) | `Result<AgentResult>` |

This controller runs the full Review → Fix → Summary pipeline via `AgentOrchestrator.executeAgents()`, then persists all results.

---

## 8. Frontend Design

### 8.1 New Pages

#### 8.1.1 History Page (`/app/history`)

**Route**: `src/views/history/History.vue`

**Layout**:
```
┌─────────────────────────────────────────────────┐
│ Filters Bar                                       │
│ [Project ▾] [Risk Level ▾] [Health ▾] [Date Range] [Search] [Reset] │
├─────────────────────────────────────────────────┤
│ Table                                             │
│ ID | Project | Score | Health | Issues | Fix Rate │ Status | Time | Actions │
├─────────────────────────────────────────────────┤
│ Pagination: < 1 2 3 ... 10 >                     │
└─────────────────────────────────────────────────┘
```

**Features**:
- Search by project name, risk level, health level, date range
- Click row → navigate to detail page
- Delete / batch delete with confirmation dialog
- Score column: color-coded (green=good, yellow=fair, red=poor)

#### 8.1.2 History Detail Page (`/app/history/:id`)

**Route**: `src/views/history/HistoryDetail.vue`

**Layout**:
```
┌─────────────────────────────────────────────────┐
│ Header: Back button | Project: xxx | Date: xxx   │
├──────────────┬──────────────────────────────────┤
│ Summary Card │ ECharts: Health Score Gauge       │
│ Score: 85    │                                    │
│ Health: Good │                                    │
├──────────────┴──────────────────────────────────┤
│ Tab: [Review] [Fix] [Summary]                    │
├─────────────────────────────────────────────────┤
│ Review Tab:                                      │
│ - Rule results table (from ReviewAgentResult)    │
│ - AI analysis summary                            │
├─────────────────────────────────────────────────┤
│ Fix Tab:                                         │
│ - Fix items table (from FixAgentResult)          │
│ - Fix statistics                                 │
├─────────────────────────────────────────────────┤
│ Summary Tab:                                     │
│ - Health report (strengths, weaknesses, etc.)    │
│ - Statistics overview                            │
└─────────────────────────────────────────────────┘
```

#### 8.1.3 Trend Statistics Page (`/app/trend`)

**Route**: `src/views/trend/Trend.vue`

**Layout**:
```
┌─────────────────────────────────────────────────┐
│ Project Selector: [All Projects ▾] | Period: [7D] [30D] [90D] │
├─────────────────────────────────────────────────┤
│ Chart 1: Health Score Trend (Line Chart)         │
│ 📈                                               │
├─────────────────────────────────────────────────┤
│ Chart 2: Issue Count Trend (Stacked Area Chart)  │
│ [Error] [Warning] [Info]                         │
├─────────────────────────────────────────────────┤
│ Chart 3: Fix Rate Trend (Line Chart)             │
│ 📊                                               │
├─────────────────────────────────────────────────┤
│ Chart 4: Score Comparison (Bar Chart)            │
│ Compare multiple projects                        │
└─────────────────────────────────────────────────┘
```

**Chart details**:
- **Health Score Trend**: Line chart with gradient fill, Y-axis 0-100, green/yellow/red zones
- **Issue Count Trend**: Stacked area chart, 3 series (error/warning/info)
- **Fix Rate Trend**: Line chart with percentage Y-axis (0-100%)
- **Score Comparison**: Grouped bar chart comparing projects

### 8.2 New TypeScript Types

**File**: `src/types/api.ts` (add to existing)

```typescript
export type AnalysisRecord = {
  id: number;
  projectId: number;
  projectName?: string;
  taskId?: number;
  overallScore?: number;
  riskLevel?: string;
  healthLevel?: string;
  healthScore?: number;
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  fixedIssues: number;
  fixSuccessRate: number;
  aiDuration: number;
  status: string;
  createTime: string;
};

export type AnalysisRecordDetail = AnalysisRecord & {
  reviewResult?: ReviewAgentResult;
  fixResult?: FixAgentResult;
  summaryStatistics?: SummaryStatistics;
  healthReport?: ProjectHealthReport;
};

export type ReviewAgentResult = {
  totalRules: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  overallScore: number;
  riskLevel: string;
  summary: string;
  ruleResults: RuleResult[];
  aiAnalysisDuration: number;
  generatedTime: string;
};

export type FixAgentResult = {
  totalIssues: number;
  fixedIssues: number;
  failedIssues: number;
  fixItems: FixItem[];
  statistics: FixStatistics;
  generatedTime: string;
};

export type FixItem = {
  ruleId: string;
  className: string;
  methodName: string;
  severity: string;
  issue: string;
  suggestion: string;
  patchContent: string;
  patchGenerated: boolean;
  generateDuration: number;
};

export type FixStatistics = {
  totalIssues: number;
  fixedIssues: number;
  failedIssues: number;
  successRate: number;
  totalDuration: number;
  totalAiDuration: number;
};

export type SummaryStatistics = {
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  fixedIssues: number;
  failedIssues: number;
  fixSuccessRate: number;
  reviewAiDuration: number;
  fixAiDuration: number;
  totalAiDuration: number;
};

export type ProjectHealthReport = {
  healthLevel: string;
  healthScore: number;
  overallStatus: string;
  summary: string;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  topProblems: string[];
};

export type TrendData = {
  projectId: number;
  projectName: string;
  points: TrendPoint[];
};

export type TrendPoint = {
  date: string;
  overallScore?: number;
  healthScore?: number;
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
  fixedIssues: number;
  fixSuccessRate: number;
};

export type AnalysisRecordQuery = {
  projectId?: number;
  keyword?: string;
  riskLevel?: string;
  healthLevel?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
};

export type ProjectTrendSummary = {
  projectId: number;
  projectName: string;
  latestScore?: number;
  latestHealthScore?: number;
  scoreChange?: number;
  totalRecords: number;
};
```

### 8.3 New API Files

**File**: `src/api/analysis-record.ts`

```typescript
import request from '@/utils/request';
import type {
  AnalysisRecord,
  AnalysisRecordDetail,
  AnalysisRecordQuery,
  TrendData,
  ProjectTrendSummary,
} from '@/types/api';

export function getRecordPage(params: AnalysisRecordQuery) {
  return request.get('/api/analysis-record/page', { params });
}

export function getRecordDetail(id: number) {
  return request.get(`/api/analysis-record/${id}`);
}

export function deleteRecord(id: number) {
  return request.delete(`/api/analysis-record/${id}`);
}

export function batchDeleteRecords(ids: number[]) {
  return request.delete('/api/analysis-record/batch', { data: ids });
}

export function getProjectTrend(projectId: number, limit = 30) {
  return request.get(`/api/analysis-record/trend/${projectId}`, { params: { limit } });
}

export function getProjectTrendOverview() {
  return request.get('/api/analysis-record/trend/overview');
}
```

### 8.4 Router Updates

**File**: `src/router/index.ts` — add to `children` of `/app`:

```typescript
{
  path: 'history',
  name: 'History',
  component: () => import('@/views/history/History.vue'),
  meta: { title: '历史记录' },
},
{
  path: 'history/:id',
  name: 'HistoryDetail',
  component: () => import('@/views/history/HistoryDetail.vue'),
  meta: { title: '记录详情' },
},
{
  path: 'trend',
  name: 'Trend',
  component: () => import('@/views/trend/Trend.vue'),
  meta: { title: '趋势统计' },
},
```

---

## 9. Risk Analysis

### High Risk

| Risk | Mitigation |
|------|-----------|
| JSON column size overflow (very large projects with 1000+ issues) | Limit `ruleResults` to top 200 items in JSON. Use pagination for full list in detail view. |
| Performance degradation from JSON deserialization on detail view | Cache deserialized objects in Redis with 1h TTL. Only deserialize on cache miss. |

### Medium Risk

| Risk | Mitigation |
|------|-----------|
| Database size growth from storing full JSON per run | Add scheduled cleanup: retain last N records per project (configurable, default 100). Use `@Scheduled` in `AnalysisRecordCleanupJob`. |
| Race condition between pipeline completion and async persistence | Use `@Async` with a dedicated thread pool. If persistence fails, log error but don't fail the pipeline. Pipeline is still primary in Redis. |
| Breaking existing controller behavior | Persistence is fire-and-forget (async). Existing Redis cache read path unchanged. Controllers add one extra line at the end. |

### Low Risk

| Risk | Mitigation |
|------|-----------|
| Schema migration for existing data | SQL migration script adds `analysis_record` table. No existing tables modified. Zero-downtime migration. |
| Frontend bundle size increase from ECharts trend charts | ECharts already used in the project. Import only required chart types via tree-shaking. |

---

## 10. Implementation Order

### Phase 9A: Task-36 (Analysis Record Persistence)

**Order**:
1. SQL migration: Create `analysis_record` table
2. Entity: `AnalysisRecord.java`
3. Mapper: `AnalysisRecordMapper.java`
4. Service interface: `AnalysisRecordService.java`
5. Service impl: `AnalysisRecordServiceImpl.java`
6. DTO: `AnalysisRecordDTO`, `AnalysisRecordDetailDTO`, `AnalysisRecordQueryRequest`
7. VO: `PageResult` (generic wrapper)
8. `AgentPipelinePersistenceService.java`
9. Modify `ReviewAgentController` to add persistence call (2 lines)
10. New `AgentPipelineController` for full pipeline with persistence
11. Compile + unit test

### Phase 9B: Task-37 (History Report Management)

**Order**:
1. Controller: `AnalysisRecordController.java`
2. VO: `AnalysisRecordDTO` (if not done in 9A)
3. Frontend: TypeScript types in `api.ts`
4. Frontend: API file `analysis-record.ts`
5. Frontend: `History.vue` page
6. Frontend: `HistoryDetail.vue` page
7. Router update
8. Test CRUD operations

### Phase 9C: Task-38 (Trend Statistics)

**Order**:
1. Mapper: Add `selectTrendData` custom query (if not done in 9A)
2. Service: Add `getProjectTrend`, `getProjectTrendSummaries` methods
3. VO: `TrendDataVO`
4. Controller: Add `/trend` and `/trend/overview` endpoints
5. Frontend: `Trend.vue` page with ECharts
6. Frontend: API endpoints for trend data
7. Router update
8. Test chart rendering

---

## 11. File Summary (New Files)

### Backend (new files)

| # | File | Package | Purpose |
|---|------|---------|---------|
| 1 | `AnalysisRecord.java` | `entity` | DB entity |
| 2 | `AnalysisRecordMapper.java` | `mapper` | Mapper with trend query |
| 3 | `AnalysisRecordService.java` | `service` | Service interface |
| 4 | `AnalysisRecordServiceImpl.java` | `service.impl` | Service implementation |
| 5 | `AnalysisRecordController.java` | `controller` | REST API |
| 6 | `AnalysisRecordDTO.java` | `dto` | List view DTO |
| 7 | `AnalysisRecordDetailDTO.java` | `dto` | Detail view DTO |
| 8 | `AnalysisRecordQueryRequest.java` | `dto` | Query params |
| 9 | `TrendDataVO.java` | `vo` | Trend chart data |
| 10 | `PageResult.java` | `common` | Generic page wrapper |
| 11 | `AgentPipelinePersistenceService.java` | `agent` | Async persistence |
| 12 | `AgentPipelineController.java` | `agent.controller` | Full pipeline endpoint |
| 13 | `V9__add_analysis_record.sql` | `sql` | Migration script |

### Backend (modified files)

| # | File | Change |
|---|------|--------|
| 1 | `ReviewAgentController.java` | Add 2 lines for persistence |

### Frontend (new files)

| # | File | Purpose |
|---|------|---------|
| 1 | `src/views/history/History.vue` | History list page |
| 2 | `src/views/history/HistoryDetail.vue` | History detail page |
| 3 | `src/views/trend/Trend.vue` | Trend statistics page |
| 4 | `src/api/analysis-record.ts` | API client |

### Frontend (modified files)

| # | File | Change |
|---|------|--------|
| 1 | `src/types/api.ts` | Add Phase-9 types |
| 2 | `src/router/index.ts` | Add 3 new routes |
