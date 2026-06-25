<template>
  <div class="history-detail" v-loading="loading">
    <div class="page-header">
      <el-button link @click="router.back()">← 返回列表</el-button>
      <h2 v-if="detail">{{ detail.projectName || '项目' }} - 分析记录 #{{ detail.id }}</h2>
    </div>

    <template v-if="detail">
      <!-- 概要卡片 -->
      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">总体评分</div>
            <div class="stat-value" :class="scoreClass(detail.overallScore)">
              {{ detail.overallScore ?? '-' }}
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">健康等级</div>
            <el-tag :type="healthTagType(detail.healthLevel)" size="large">
              {{ detail.healthLevel ?? '-' }}
            </el-tag>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">问题总数</div>
            <div class="stat-value">{{ detail.totalIssues }}</div>
            <div class="stat-sub">
              <el-text type="danger">E:{{ detail.errorCount }}</el-text>
              <el-text type="warning"> W:{{ detail.warningCount }}</el-text>
              <el-text type="info"> I:{{ detail.infoCount }}</el-text>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">修复率</div>
            <div class="stat-value">
              {{ detail.fixSuccessRate != null ? (detail.fixSuccessRate * 100).toFixed(0) + '%' : '-' }}
            </div>
            <div class="stat-sub">已修复 {{ detail.fixedIssues }} / {{ detail.totalIssues }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Tab -->
      <el-card shadow="never">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="健康报告" name="health">
            <template v-if="detail.healthReport">
              <div class="health-summary">
                <p>{{ detail.healthReport.summary }}</p>
              </div>
              <el-row :gutter="16">
                <el-col :span="8">
                  <h4>优势</h4>
                  <ul v-if="detail.healthReport.strengths?.length">
                    <li v-for="(s, i) in detail.healthReport.strengths" :key="i">{{ s }}</li>
                  </ul>
                  <el-empty v-else description="暂无" :image-size="60" />
                </el-col>
                <el-col :span="8">
                  <h4>劣势</h4>
                  <ul v-if="detail.healthReport.weaknesses?.length">
                    <li v-for="(w, i) in detail.healthReport.weaknesses" :key="i">{{ w }}</li>
                  </ul>
                  <el-empty v-else description="暂无" :image-size="60" />
                </el-col>
                <el-col :span="8">
                  <h4>改进建议</h4>
                  <ul v-if="detail.healthReport.recommendations?.length">
                    <li v-for="(r, i) in detail.healthReport.recommendations" :key="i">{{ r }}</li>
                  </ul>
                  <el-empty v-else description="暂无" :image-size="60" />
                </el-col>
              </el-row>
              <div v-if="detail.healthReport.topProblems?.length" class="top-problems">
                <h4>Top 问题</h4>
                <el-tag v-for="(p, i) in detail.healthReport.topProblems" :key="i" class="problem-tag" type="danger">
                  {{ p }}
                </el-tag>
              </div>
            </template>
            <el-empty v-else description="暂无健康报告数据" />
          </el-tab-pane>

          <el-tab-pane label="统计详情" name="statistics">
            <template v-if="detail.summaryStatistics">
              <el-descriptions :column="3" border>
                <el-descriptions-item label="总问题数">{{ detail.summaryStatistics.totalIssues }}</el-descriptions-item>
                <el-descriptions-item label="错误数">
                  <el-text type="danger">{{ detail.summaryStatistics.errorCount }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item label="警告数">
                  <el-text type="warning">{{ detail.summaryStatistics.warningCount }}</el-text>
                </el-descriptions-item>
                <el-descriptions-item label="信息数">{{ detail.summaryStatistics.infoCount }}</el-descriptions-item>
                <el-descriptions-item label="已修复">{{ detail.summaryStatistics.fixedIssues }}</el-descriptions-item>
                <el-descriptions-item label="修复成功率">
                  {{ (detail.summaryStatistics.fixSuccessRate * 100).toFixed(1) }}%
                </el-descriptions-item>
                <el-descriptions-item label="Review AI耗时">{{ formatDuration(detail.summaryStatistics.reviewAiDuration) }}</el-descriptions-item>
                <el-descriptions-item label="Fix AI耗时">{{ formatDuration(detail.summaryStatistics.fixAiDuration) }}</el-descriptions-item>
                <el-descriptions-item label="总AI耗时">{{ formatDuration(detail.summaryStatistics.totalAiDuration) }}</el-descriptions-item>
              </el-descriptions>
            </template>
            <el-empty v-else description="暂无统计数据" />
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 元信息 -->
      <el-card shadow="never" class="meta-card">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="记录ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="项目ID">{{ detail.projectId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ detail.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="AI耗时">{{ formatDuration(detail.aiDuration) }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">{{ detail.riskLevel ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="Markdown">{{ detail.markdownPath ?? '无' }}</el-descriptions-item>
          <el-descriptions-item label="PDF">{{ detail.pdfPath ?? '无' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { AnalysisRecordDetail } from '@/types/api';
import { getRecordDetail } from '@/api/analysis-record';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const detail = ref<AnalysisRecordDetail | null>(null);
const activeTab = ref('health');

async function loadDetail() {
  const id = Number(route.params.id);
  if (!id) return;
  loading.value = true;
  try {
    detail.value = await getRecordDetail(id);
  } catch {
    ElMessage.error('加载详情失败');
  } finally {
    loading.value = false;
  }
}

function scoreClass(score?: number) {
  if (score == null) return '';
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-fair';
  return 'score-poor';
}

function healthTagType(level?: string) {
  if (!level) return 'info';
  if (level === 'EXCELLENT' || level === 'GOOD') return 'success';
  if (level === 'FAIR') return 'warning';
  return 'danger';
}

function formatDuration(ms?: number) {
  if (ms == null) return '-';
  if (ms < 1000) return ms + 'ms';
  return (ms / 1000).toFixed(1) + 's';
}

onMounted(loadDetail);
</script>

<style scoped>
.history-detail {
  padding: 20px;
}
.page-header {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
}
.summary-row {
  margin-bottom: 16px;
}
.stat-card {
  text-align: center;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
.stat-value.score-good { color: #67c23a; }
.stat-value.score-fair { color: #e6a23c; }
.stat-value.score-poor { color: #f56c6c; }
.stat-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.health-summary {
  margin-bottom: 16px;
  color: #606266;
}
.top-problems {
  margin-top: 16px;
}
.problem-tag {
  margin: 4px;
}
.meta-card {
  margin-top: 16px;
}
</style>
