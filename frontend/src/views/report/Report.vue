<template>
  <div class="report-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-toolbar">
          <div>
            <h2>报告中心</h2>
            <p>{{ projectReportMode ? '按项目查看报告生成进度与结果' : '查看本机已生成报告，支持 Markdown / PDF 下载' }}</p>
          </div>
          <el-space>
            <el-button v-if="projectReportMode" @click="backToList">返回列表</el-button>
            <el-button @click="refreshPage">刷新</el-button>
          </el-space>
        </div>
      </template>

      <template v-if="projectReportMode">
        <el-card v-if="taskProgress" shadow="never" class="section-card">
          <template #header>报告生成进度</template>
          <el-progress :percentage="taskProgress.percent" :status="progressStatus" />
          <div class="progress-stage">{{ taskProgress.stage }} ({{ taskProgress.percent }}%)</div>
          <el-descriptions :column="3" border class="scan-meta">
            <el-descriptions-item label="任务 ID">{{ taskProgress.taskId }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ taskProgress.status }}</el-descriptions-item>
            <el-descriptions-item label="阶段">{{ taskProgress.stage }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-empty v-else description="暂无报告任务进度" />

        <template v-if="currentReport">
          <el-descriptions :column="3" border class="section-card">
            <el-descriptions-item label="项目名称">{{ currentReport.projectName }}</el-descriptions-item>
            <el-descriptions-item label="扫描时间">{{ currentReport.report.scanTime }}</el-descriptions-item>
            <el-descriptions-item label="总规则数">{{ currentReport.report.totalRules }}</el-descriptions-item>
            <el-descriptions-item label="通过规则">{{ currentReport.report.passedRules }}</el-descriptions-item>
            <el-descriptions-item label="失败规则">{{ currentReport.report.failedRules }}</el-descriptions-item>
            <el-descriptions-item label="生成时间">{{ currentReport.createdAt }}</el-descriptions-item>
          </el-descriptions>

          <el-card class="section-card" shadow="never">
            <template #header>Markdown 预览</template>
            <pre class="markdown-preview">{{ markdownPreview }}</pre>
          </el-card>

          <el-card class="section-card" shadow="never">
            <template #header>问题列表</template>
            <el-table :data="currentReport.report.issues" empty-text="暂无问题">
              <el-table-column prop="ruleId" label="规则" width="110" />
              <el-table-column prop="ruleName" label="名称" min-width="160" />
              <el-table-column prop="severity" label="等级" width="110" />
              <el-table-column prop="className" label="类" min-width="160" show-overflow-tooltip />
              <el-table-column prop="suggestion" label="建议" min-width="220" show-overflow-tooltip />
            </el-table>
          </el-card>
        </template>
      </template>

      <template v-else>
        <el-form class="filter-bar" :inline="true">
          <el-form-item label="搜索">
            <el-input v-model.trim="keyword" placeholder="按项目名称搜索" clearable />
          </el-form-item>
        </el-form>

        <el-table :data="pagedReports" empty-text="暂无报告，请先在代码审查页面生成报告">
          <el-table-column prop="projectName" label="项目名称" min-width="160" />
          <el-table-column prop="createdAt" label="生成时间" min-width="180" />
          <el-table-column label="问题统计" min-width="180">
            <template #default="{ row }">
              <el-space>
                <el-tag type="danger">E {{ row.report.errorCount }}</el-tag>
                <el-tag type="warning">W {{ row.report.warningCount }}</el-tag>
                <el-tag type="info">I {{ row.report.infoCount }}</el-tag>
              </el-space>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="downloadMarkdown(row)">Markdown</el-button>
              <el-button link type="primary" @click="downloadPdf(row)">PDF</el-button>
              <el-button link type="danger" @click="removeReport(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page.current"
            v-model:page-size="page.size"
            :total="filteredReports.length"
            layout="total, sizes, prev, pager, next"
          />
        </div>
      </template>
    </el-card>

    <el-drawer v-model="drawerVisible" size="62%" title="报告详情">
      <template v-if="currentReport">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="项目名称">{{ currentReport.projectName }}</el-descriptions-item>
          <el-descriptions-item label="扫描时间">{{ currentReport.report.scanTime }}</el-descriptions-item>
          <el-descriptions-item label="总规则数">{{ currentReport.report.totalRules }}</el-descriptions-item>
          <el-descriptions-item label="通过规则">{{ currentReport.report.passedRules }}</el-descriptions-item>
          <el-descriptions-item label="失败规则">{{ currentReport.report.failedRules }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ currentReport.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <el-card class="section-card" shadow="never">
          <template #header>Markdown 预览</template>
          <pre class="markdown-preview">{{ markdownPreview }}</pre>
        </el-card>

        <el-card class="section-card" shadow="never">
          <template #header>问题列表</template>
          <el-table :data="currentReport.report.issues" empty-text="暂无问题">
            <el-table-column prop="ruleId" label="规则" width="110" />
            <el-table-column prop="ruleName" label="名称" min-width="160" />
            <el-table-column prop="severity" label="等级" width="110" />
            <el-table-column prop="className" label="类" min-width="160" show-overflow-tooltip />
            <el-table-column prop="suggestion" label="建议" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-card>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { downloadPdfApi } from '@/api/report';
import { getReportProgressByProjectApi } from '@/api/review';
import { getRecordPage, deleteRecord } from '@/api/analysis-record';
import { downloadBlob } from '@/utils/download';
import type { AnalysisRecord, CachedReport, ReportTaskProgress, ReviewReport } from '@/types/api';

const POLL_INTERVAL = 500;

const route = useRoute();
const router = useRouter();
const keyword = ref('');
const reports = ref<CachedReport[]>([]);
const drawerVisible = ref(false);
const currentReport = ref<CachedReport>();
const taskProgress = ref<ReportTaskProgress>();
const page = reactive({ current: 1, size: 10 });
let pollTimer: number | undefined;

const routeProjectId = computed(() => {
  const rawProjectId = route.params.projectId;
  const projectId = Number(Array.isArray(rawProjectId) ? rawProjectId[0] : rawProjectId);
  return Number.isInteger(projectId) && projectId > 0 ? projectId : undefined;
});

const projectReportMode = computed(() => typeof routeProjectId.value === 'number');

const progressStatus = computed(() => {
  if (taskProgress.value?.status === 'SUCCESS') {
    return 'success';
  }
  if (taskProgress.value?.status === 'FAILED' || taskProgress.value?.status === 'NOT_FOUND') {
    return 'exception';
  }
  return undefined;
});

const filteredReports = computed(() => {
  if (!keyword.value) {
    return reports.value;
  }

  return reports.value.filter((item) => item.projectName.toLowerCase().includes(keyword.value.toLowerCase()));
});

const pagedReports = computed(() => {
  const start = (page.current - 1) * page.size;
  return filteredReports.value.slice(start, start + page.size);
});

const markdownPreview = computed(() => {
  if (!currentReport.value) return '';
  return buildMarkdownContent(currentReport.value.report);
});

const buildMarkdownContent = (report: ReviewReport) => {
  const issues = report.issues
    .map((issue) => `- [${issue.severity}] ${issue.ruleId} ${issue.ruleName}：${issue.suggestion}`)
    .join('\n');

  return `# ${report.projectName} 代码审查报告\n\n## 总览\n\n- 扫描时间：${report.scanTime}\n- 总规则数：${report.totalRules}\n- 通过规则：${report.passedRules}\n- 失败规则：${report.failedRules}\n- ERROR：${report.errorCount}\n- WARNING：${report.warningCount}\n- INFO：${report.infoCount}\n\n## 总体评价\n\n${report.overallSummary || '暂无总体评价'}\n\n## 问题列表\n\n${issues || '暂无问题'}`;
};

const clearPollTimer = () => {
  if (pollTimer) {
    window.clearTimeout(pollTimer);
    pollTimer = undefined;
  }
};

const loadReports = async () => {
  try {
    const res = await getRecordPage({ page: 1, size: 100 });
    reports.value = (res.records ?? []).map((r: any) => ({
      id: `agent-${r.id}`,
      projectId: r.projectId,
      taskId: String(r.taskId ?? ''),
      projectName: r.projectName || `项目${r.projectId}`,
      createdAt: r.createTime,
      report: {
        projectName: r.projectName || `项目${r.projectId}`,
        scanTime: r.createTime,
        totalRules: r.totalIssues,
        passedRules: r.totalIssues - r.errorCount - r.warningCount - r.infoCount,
        failedRules: r.errorCount + r.warningCount + r.infoCount,
        errorCount: r.errorCount,
        warningCount: r.warningCount,
        infoCount: r.infoCount,
        issues: r.issues ?? [],
        overallSummary: r.healthLevel ? `健康等级: ${r.healthLevel}, 评分: ${r.healthScore}` : '',
      },
      aiAnalyses: [],
    }));
  } catch {
    reports.value = [];
  }
};

const applyReportProgress = async (progress: ReportTaskProgress) => {
  taskProgress.value = progress;

  if (progress.status === 'SUCCESS') {
    clearPollTimer();
    loadReports();
    ElMessage.success('报告生成完成');
    return;
  }

  if (progress.status === 'FAILED' || progress.status === 'NOT_FOUND') {
    clearPollTimer();
    currentReport.value = undefined;
    ElMessage.error(progress.stage || (progress.status === 'NOT_FOUND' ? '报告任务不存在或已过期' : '报告生成失败'));
    return;
  }

  pollTimer = window.setTimeout(loadProjectReportProgress, POLL_INTERVAL);
};

const loadProjectReportProgress = async () => {
  if (!routeProjectId.value) {
    return;
  }

  const progress = await getReportProgressByProjectApi(routeProjectId.value);
  await applyReportProgress(progress);
};

const refreshPage = async () => {
  if (projectReportMode.value) {
    clearPollTimer();
    await loadProjectReportProgress();
    return;
  }

  loadReports();
};

const backToList = () => router.push({ name: 'Report' });

const openDetail = (report: CachedReport) => {
  currentReport.value = report;
  drawerVisible.value = true;
};

const downloadMarkdown = async (report: CachedReport) => {
  const content = buildMarkdownContent(report.report);
  const blob = new Blob([content], { type: 'text/markdown; charset=UTF-8' });
  downloadBlob(blob, `${report.projectName}-review-report.md`);
};

const downloadPdf = async (report: CachedReport) => {
  const blob = await downloadPdfApi(report.taskId);
  downloadBlob(blob, `${report.projectName}-review-report.pdf`);
};

const removeReport = async (report: CachedReport) => {
  await ElMessageBox.confirm(`确认删除报告「${report.projectName}」？`, '删除确认', { type: 'warning' });
  // 从 ID 中提取数据库记录 ID
  const recordId = Number(report.id.replace('agent-', ''));
  if (recordId) {
    await deleteRecord(recordId);
  }
  loadReports();
  ElMessage.success('报告已删除');
};

watch(
  () => routeProjectId.value,
  async () => {
    clearPollTimer();
    currentReport.value = undefined;
    taskProgress.value = undefined;
    await refreshPage();
  },
);

onMounted(refreshPage);
onBeforeUnmount(clearPollTimer);
</script>