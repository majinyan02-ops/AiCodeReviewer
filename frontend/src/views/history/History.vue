<template>
  <div class="history-page">
    <div class="page-header">
      <h2>历史记录</h2>
    </div>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="handleSearch">
        <el-form-item label="项目">
          <el-select v-model="queryForm.projectId" placeholder="全部项目" clearable style="width: 180px">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="queryForm.riskLevel" placeholder="全部" clearable style="width: 130px">
            <el-option label="HIGH" value="HIGH" />
            <el-option label="MEDIUM" value="MEDIUM" />
            <el-option label="LOW" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="健康等级">
          <el-select v-model="queryForm.healthLevel" placeholder="全部" clearable style="width: 130px">
            <el-option label="EXCELLENT" value="EXCELLENT" />
            <el-option label="GOOD" value="GOOD" />
            <el-option label="FAIR" value="FAIR" />
            <el-option label="POOR" value="POOR" />
            <el-option label="CRITICAL" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
            style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange"
        stripe style="width: 100%">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="projectName" label="项目" min-width="140" />
        <el-table-column prop="overallScore" label="评分" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="scoreTagType(row.overallScore)" size="small">
              {{ row.overallScore ?? '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="healthLevel" label="健康等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="healthTagType(row.healthLevel)" size="small">
              {{ row.healthLevel ?? '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="问题分布" width="140" align="center">
          <template #default="{ row }">
            <span class="issue-count">
              <el-text type="danger" size="small">{{ row.errorCount }}</el-text> /
              <el-text type="warning" size="small">{{ row.warningCount }}</el-text> /
              <el-text type="info" size="small">{{ row.infoCount }}</el-text>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="fixedIssues" label="已修复" width="80" align="center" />
        <el-table-column prop="fixSuccessRate" label="修复率" width="90" align="center">
          <template #default="{ row }">
            {{ row.fixSuccessRate != null ? (row.fixSuccessRate * 100).toFixed(0) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryForm.page" v-model:page-size="queryForm.size"
          :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          @current-change="loadData" @size-change="handleSearch" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { AnalysisRecord, AnalysisRecordQuery } from '@/types/api';
import { getRecordPage, deleteRecord, batchDeleteRecords } from '@/api/analysis-record';
import { getProjectPageApi } from '@/api/project';

const router = useRouter();
const loading = ref(false);
const tableData = ref<AnalysisRecord[]>([]);
const total = ref(0);
const selectedIds = ref<number[]>([]);
const dateRange = ref<[string, string] | null>(null);
const projects = ref<{ id: number; name: string }[]>([]);

const queryForm = ref<AnalysisRecordQuery>({
  page: 1,
  size: 20,
});

async function loadData() {
  loading.value = true;
  try {
    if (dateRange.value) {
      queryForm.value.startDate = dateRange.value[0];
      queryForm.value.endDate = dateRange.value[1];
    } else {
      queryForm.value.startDate = undefined;
      queryForm.value.endDate = undefined;
    }
    const res = await getRecordPage(queryForm.value);
    tableData.value = res.records;
    total.value = res.total;
  } catch {
    ElMessage.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  queryForm.value.page = 1;
  loadData();
}

function handleReset() {
  queryForm.value = { page: 1, size: 20 };
  dateRange.value = null;
  loadData();
}

function handleSelectionChange(rows: AnalysisRecord[]) {
  selectedIds.value = rows.map((r) => r.id);
}

async function handleDelete(id: number) {
  await deleteRecord(id);
  ElMessage.success('已删除');
  loadData();
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除 ${selectedIds.value.length} 条记录？`, '批量删除', { type: 'warning' });
  await batchDeleteRecords(selectedIds.value);
  ElMessage.success('已删除');
  loadData();
}

function goDetail(id: number) {
  router.push(`/app/history/${id}`);
}

function scoreTagType(score?: number) {
  if (score == null) return 'info';
  if (score >= 80) return 'success';
  if (score >= 60) return 'warning';
  return 'danger';
}

function healthTagType(level?: string) {
  if (!level) return 'info';
  if (level === 'EXCELLENT' || level === 'GOOD') return 'success';
  if (level === 'FAIR') return 'warning';
  return 'danger';
}

async function loadProjects() {
  try {
    const res = await getProjectPageApi({ current: 1, size: 100 });
    projects.value = res.records.map((p: any) => ({ id: p.id, name: p.name }));
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadData();
  loadProjects();
});
</script>

<style scoped>
.history-page {
  padding: 20px;
}
.page-header {
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.filter-card {
  margin-bottom: 16px;
}
.table-toolbar {
  margin-bottom: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.issue-count {
  font-size: 13px;
}
</style>
