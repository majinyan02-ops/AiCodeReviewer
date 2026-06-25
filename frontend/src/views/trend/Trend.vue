<template>
  <div class="trend-page">
    <div class="page-header">
      <h2>趋势统计</h2>
    </div>

    <!-- 概览统计 -->
    <el-row :gutter="16" class="overview-row" v-if="overview">
      <el-col :span="4">
        <el-card shadow="never" class="mini-stat">
          <div class="mini-label">项目总数</div>
          <div class="mini-value">{{ overview.totalProjects }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="mini-stat">
          <div class="mini-label">分析次数</div>
          <div class="mini-value">{{ overview.totalRecords }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="mini-stat">
          <div class="mini-label">总问题数</div>
          <div class="mini-value">{{ overview.totalIssues }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="mini-stat">
          <div class="mini-label">已修复</div>
          <div class="mini-value">{{ overview.totalFixed }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="mini-stat">
          <div class="mini-label">平均健康分</div>
          <div class="mini-value">{{ overview.avgHealthScore }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="mini-stat">
          <div class="mini-label">平均修复率</div>
          <div class="mini-value">{{ overview.avgFixRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 项目选择 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="项目">
          <el-select v-model="selectedProjectId" placeholder="全部项目" clearable style="width: 200px"
            @change="loadTrend">
            <el-option v-for="p in projects" :key="p.projectId" :label="p.projectName" :value="p.projectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-radio-group v-model="period" @change="loadTrend">
            <el-radio-button :value="7">7天</el-radio-button>
            <el-radio-button :value="30">30天</el-radio-button>
            <el-radio-button :value="90">90天</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>健康评分趋势</template>
          <div ref="healthChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>问题数量趋势</template>
          <div ref="issueChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>修复率趋势</template>
          <div ref="fixRateChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>项目健康对比</template>
          <div ref="comparisonChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';
import type { TrendData, ProjectTrendSummary, StatisticsOverview } from '@/types/api';
import { getProjectTrend, getProjectTrendOverview, getStatisticsOverview } from '@/api/analysis-record';

const selectedProjectId = ref<number | undefined>();
const period = ref(30);
const projects = ref<ProjectTrendSummary[]>([]);
const overview = ref<StatisticsOverview | null>(null);
const trendData = ref<TrendData | null>(null);

const healthChartRef = ref<HTMLElement>();
const issueChartRef = ref<HTMLElement>();
const fixRateChartRef = ref<HTMLElement>();
const comparisonChartRef = ref<HTMLElement>();

let healthChart: echarts.ECharts | null = null;
let issueChart: echarts.ECharts | null = null;
let fixRateChart: echarts.ECharts | null = null;
let comparisonChart: echarts.ECharts | null = null;

async function loadOverview() {
  try {
    overview.value = await getStatisticsOverview();
    projects.value = overview.value.projectHealthItems.map((p) => ({
      projectId: p.projectId,
      projectName: p.projectName,
      latestScore: p.healthScore,
      latestHealthScore: p.healthScore,
      scoreChange: 0,
      totalRecords: p.totalRecords,
    }));
  } catch {
    // ignore
  }
}

async function loadTrend() {
  if (!selectedProjectId.value) {
    trendData.value = null;
    clearCharts();
    return;
  }
  try {
    trendData.value = await getProjectTrend(selectedProjectId.value, period.value);
    await nextTick();
    renderCharts();
  } catch {
    // ignore
  }
}

function clearCharts() {
  healthChart?.clear();
  issueChart?.clear();
  fixRateChart?.clear();
}

function renderCharts() {
  const data = trendData.value;
  if (!data || !data.points.length) return;

  const dates = data.points.map((p) => p.date);

  // 健康评分趋势
  if (healthChartRef.value) {
    if (!healthChart) healthChart = echarts.init(healthChartRef.value);
    healthChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value', min: 0, max: 100 },
      series: [{
        name: '健康评分',
        type: 'line',
        data: data.points.map((p) => p.healthScore),
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#67c23a' },
      }],
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
    });
  }

  // 问题数量趋势
  if (issueChartRef.value) {
    if (!issueChart) issueChart = echarts.init(issueChartRef.value);
    issueChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['错误', '警告', '信息'] },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [
        { name: '错误', type: 'line', stack: 'issues', areaStyle: {}, data: data.points.map((p) => p.errorCount), itemStyle: { color: '#f56c6c' } },
        { name: '警告', type: 'line', stack: 'issues', areaStyle: {}, data: data.points.map((p) => p.warningCount), itemStyle: { color: '#e6a23c' } },
        { name: '信息', type: 'line', stack: 'issues', areaStyle: {}, data: data.points.map((p) => p.infoCount), itemStyle: { color: '#909399' } },
      ],
      grid: { left: 50, right: 20, top: 40, bottom: 30 },
    });
  }

  // 修复率趋势
  if (fixRateChartRef.value) {
    if (!fixRateChart) fixRateChart = echarts.init(fixRateChartRef.value);
    fixRateChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        name: '修复率',
        type: 'line',
        data: data.points.map((p) => p.fixSuccessRate != null ? (p.fixSuccessRate * 100).toFixed(0) : null),
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#409eff' },
      }],
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
    });
  }
}

function renderComparisonChart() {
  if (!comparisonChartRef.value || !overview.value) return;
  if (!comparisonChart) comparisonChart = echarts.init(comparisonChartRef.value);

  const items = overview.value.projectHealthItems;
  comparisonChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['健康评分', '总问题数'] },
    xAxis: { type: 'category', data: items.map((p) => p.projectName), axisLabel: { rotate: 30 } },
    yAxis: [
      { type: 'value', name: '健康分', min: 0, max: 100 },
      { type: 'value', name: '问题数', position: 'right' },
    ],
    series: [
      { name: '健康评分', type: 'bar', data: items.map((p) => p.healthScore), itemStyle: { color: '#67c23a' } },
      { name: '总问题数', type: 'bar', yAxisIndex: 1, data: items.map((p) => p.totalIssues), itemStyle: { color: '#409eff' } },
    ],
    grid: { left: 50, right: 60, top: 40, bottom: 60 },
  });
}

function handleResize() {
  healthChart?.resize();
  issueChart?.resize();
  fixRateChart?.resize();
  comparisonChart?.resize();
}

onMounted(async () => {
  await loadOverview();
  renderComparisonChart();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  healthChart?.dispose();
  issueChart?.dispose();
  fixRateChart?.dispose();
  comparisonChart?.dispose();
});
</script>

<style scoped>
.trend-page {
  padding: 20px;
}
.page-header {
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.overview-row {
  margin-bottom: 16px;
}
.mini-stat {
  text-align: center;
}
.mini-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.mini-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}
.filter-card {
  margin-bottom: 16px;
}
.chart-container {
  width: 100%;
  height: 300px;
}
</style>
