<template>
  <div class="dashboard-page">
    <div class="stat-grid">
      <el-card v-for="item in stats" :key="item.label" class="stat-card" shadow="never">
        <el-statistic :title="item.label" :value="item.value" />
        <div class="stat-card__hint">{{ item.hint }}</div>
      </el-card>
    </div>

    <el-row :gutter="20" class="dashboard-page__charts">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>问题等级分布</template>
          <div ref="severityChartRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>报告通过率</template>
          <div ref="passChartRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>最近项目</template>
          <el-table :data="recentProjects" v-loading="loading" empty-text="暂无项目">
            <el-table-column prop="name" label="项目名称" min-width="140" />
            <el-table-column prop="branchName" label="分支" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '未启用' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>最近报告</template>
          <el-table :data="recentReports" empty-text="暂无报告">
            <el-table-column prop="projectName" label="项目名称" min-width="140" />
            <el-table-column prop="createdAt" label="生成时间" min-width="160" />
            <el-table-column label="问题数" width="90">
              <template #default="{ row }">{{ row.report.failedRules }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { getProjectPageApi } from '@/api/project';
import { getCachedReports } from '@/utils/reportCache';
import { useEcharts } from '@/hooks/useEcharts';
import type { CachedReport, Project } from '@/types/api';

const loading = ref(false);
const projects = ref<Project[]>([]);
const reports = ref<CachedReport[]>([]);
const severityChartRef = ref<HTMLElement>();
const passChartRef = ref<HTMLElement>();
const severityChart = useEcharts(severityChartRef);
const passChart = useEcharts(passChartRef);

const recentProjects = computed(() => projects.value.slice(0, 5));
const recentReports = computed(() => reports.value.slice(0, 5));

const totals = computed(() => {
  const initial = { error: 0, warning: 0, info: 0, failed: 0, passed: 0 };
  return reports.value.reduce((acc, item) => {
    acc.error += item.report.errorCount;
    acc.warning += item.report.warningCount;
    acc.info += item.report.infoCount;
    acc.failed += item.report.failedRules;
    acc.passed += item.report.passedRules;
    return acc;
  }, initial);
});

const stats = computed(() => [
  { label: '项目数量', value: projects.value.length, hint: '来自项目管理接口' },
  { label: '扫描数量', value: reports.value.length, hint: '来自本地报告记录' },
  { label: '问题数量', value: totals.value.failed, hint: '未通过规则总数' },
  { label: 'ERROR 数量', value: totals.value.error, hint: '高风险问题' },
  { label: 'WARNING 数量', value: totals.value.warning, hint: '中风险问题' },
  { label: 'INFO 数量', value: totals.value.info, hint: '提示类问题' },
]);

const renderCharts = async () => {
  await nextTick();
  severityChart.value?.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        data: [
          { name: 'ERROR', value: totals.value.error },
          { name: 'WARNING', value: totals.value.warning },
          { name: 'INFO', value: totals.value.info },
        ],
      },
    ],
  });

  passChart.value?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['通过', '未通过'] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: [totals.value.passed, totals.value.failed], itemStyle: { color: '#2563eb' } }],
  });
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await getProjectPageApi({ current: 1, size: 10 });
    projects.value = page.records ?? [];
    reports.value = getCachedReports();
    await renderCharts();
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);
</script>