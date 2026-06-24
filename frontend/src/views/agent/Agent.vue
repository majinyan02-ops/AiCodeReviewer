<template>
  <div class="agent-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-toolbar">
          <div>
            <h2>Agent 智能分析</h2>
            <p>选择项目后自动执行审查、修复、汇总全流程</p>
          </div>
          <el-button type="primary" :disabled="!selectedProjectId || running" :loading="running" @click="startPipeline">
            {{ running ? '分析中...' : '开始分析' }}
          </el-button>
        </div>
      </template>

      <el-form class="filter-bar" :inline="true">
        <el-form-item label="项目">
          <el-select v-model="selectedProjectId" filterable placeholder="请选择项目" style="width: 320px">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-button @click="loadProjects">刷新</el-button>
      </el-form>

      <el-steps :active="activeStep" finish-status="success" class="agent-steps">
        <el-step title="选择项目" />
        <el-step title="ReviewAgent" />
        <el-step title="FixAgent" />
        <el-step title="SummaryAgent" />
        <el-step title="完成" />
      </el-steps>

      <el-alert v-if="pipelineError" :title="pipelineError" type="error" show-icon class="agent-error" />
    </el-card>

    <!-- Review 结果 -->
    <el-card v-if="reviewResult" shadow="never" class="section-card">
      <template #header>
        <div class="section-header">
          <span>ReviewAgent 结果</span>
          <el-tag :type="reviewResult.riskLevel === 'LOW' ? 'success' : reviewResult.riskLevel === 'MEDIUM' ? 'warning' : 'danger'">
            {{ reviewResult.riskLevel }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="总规则数">{{ reviewResult.totalRules }}</el-descriptions-item>
        <el-descriptions-item label="ERROR">{{ reviewResult.errorCount }}</el-descriptions-item>
        <el-descriptions-item label="WARNING">{{ reviewResult.warningCount }}</el-descriptions-item>
        <el-descriptions-item label="INFO">{{ reviewResult.infoCount }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ reviewResult.overallScore }}</el-descriptions-item>
        <el-descriptions-item label="AI 耗时">{{ reviewResult.aiAnalysisDuration }}ms</el-descriptions-item>
        <el-descriptions-item label="生成时间" :span="2">{{ reviewResult.generatedTime }}</el-descriptions-item>
      </el-descriptions>
      <el-card v-if="reviewResult.summary" shadow="never" class="inner-card">
        <template #header>AI 总结</template>
        <p>{{ reviewResult.summary }}</p>
      </el-card>
      <el-table :data="reviewResult.ruleResults" empty-text="暂无规则结果" max-height="400" class="section-card">
        <el-table-column prop="ruleId" label="规则" width="110" />
        <el-table-column prop="ruleName" label="名称" min-width="160" />
        <el-table-column prop="severity" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.severity === 'ERROR' ? 'danger' : row.severity === 'WARNING' ? 'warning' : 'info'" size="small">
              {{ row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="类" min-width="160" show-overflow-tooltip />
        <el-table-column prop="methodName" label="方法" min-width="140" />
        <el-table-column prop="message" label="问题" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- Fix 结果 -->
    <el-card v-if="fixResult" shadow="never" class="section-card">
      <template #header>
        <div class="section-header">
          <span>FixAgent 结果</span>
          <el-space>
            <el-tag type="success">修复 {{ fixResult.fixedIssues }}</el-tag>
            <el-tag type="danger">失败 {{ fixResult.failedIssues }}</el-tag>
          </el-space>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="总问题数">{{ fixResult.totalIssues }}</el-descriptions-item>
        <el-descriptions-item label="已修复">{{ fixResult.fixedIssues }}</el-descriptions-item>
        <el-descriptions-item label="未修复">{{ fixResult.failedIssues }}</el-descriptions-item>
        <el-descriptions-item label="成功率">{{ (fixResult.statistics.successRate * 100).toFixed(0) }}%</el-descriptions-item>
        <el-descriptions-item label="AI 耗时">{{ fixResult.statistics.totalAiDuration }}ms</el-descriptions-item>
        <el-descriptions-item label="生成时间" :span="2">{{ fixResult.generatedTime }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="fixResult.fixItems" empty-text="暂无修复项" max-height="400" class="section-card">
        <el-table-column prop="ruleId" label="规则" width="110" />
        <el-table-column prop="className" label="类" min-width="140" show-overflow-tooltip />
        <el-table-column prop="methodName" label="方法" width="140" />
        <el-table-column prop="severity" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.severity === 'ERROR' ? 'danger' : row.severity === 'WARNING' ? 'warning' : 'info'" size="small">
              {{ row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issue" label="问题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="suggestion" label="修复建议" min-width="200" show-overflow-tooltip />
        <el-table-column label="补丁" width="90">
          <template #default="{ row }">
            <el-tag :type="row.patchGenerated ? 'success' : 'info'" size="small">
              {{ row.patchGenerated ? '已生成' : '未生成' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Summary 结果 -->
    <el-card v-if="summaryResult" shadow="never" class="section-card">
      <template #header>
        <div class="section-header">
          <span>SummaryAgent 结果</span>
          <el-tag :type="statusTagType(summaryResult.healthReport.overallStatus)">
            {{ summaryResult.healthReport.overallStatus }}
          </el-tag>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <div class="health-score-box">
            <div class="health-score-value" :class="scoreClass(summaryResult.healthReport.healthScore)">
              {{ summaryResult.healthReport.healthScore }}
            </div>
            <div class="health-score-label">{{ summaryResult.healthReport.healthLevel }}</div>
          </div>
        </el-col>
        <el-col :span="16">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总问题数">{{ summaryResult.statistics.totalIssues }}</el-descriptions-item>
            <el-descriptions-item label="已修复">{{ summaryResult.statistics.fixedIssues }}</el-descriptions-item>
            <el-descriptions-item label="修复成功率">{{ (summaryResult.statistics.fixSuccessRate * 100).toFixed(0) }}%</el-descriptions-item>
            <el-descriptions-item label="总 AI 耗时">{{ summaryResult.statistics.totalAiDuration }}ms</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>

      <el-card shadow="never" class="inner-card">
        <template #header>项目总结</template>
        <p>{{ summaryResult.healthReport.summary }}</p>
      </el-card>

      <el-row :gutter="20" class="section-card">
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>优势</template>
            <ul v-if="summaryResult.healthReport.strengths.length">
              <li v-for="(item, i) in summaryResult.healthReport.strengths" :key="i">{{ item }}</li>
            </ul>
            <el-empty v-else description="暂无" :image-size="60" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>劣势</template>
            <ul v-if="summaryResult.healthReport.weaknesses.length">
              <li v-for="(item, i) in summaryResult.healthReport.weaknesses" :key="i">{{ item }}</li>
            </ul>
            <el-empty v-else description="暂无" :image-size="60" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>改进建议</template>
            <ul v-if="summaryResult.healthReport.recommendations.length">
              <li v-for="(item, i) in summaryResult.healthReport.recommendations" :key="i">{{ item }}</li>
            </ul>
            <el-empty v-else description="暂无" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="summaryResult.healthReport.topProblems.length" shadow="never" class="inner-card">
        <template #header>Top 问题</template>
        <el-tag v-for="(problem, i) in summaryResult.healthReport.topProblems" :key="i" class="top-problem-tag">
          {{ problem }}
        </el-tag>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getProjectPageApi } from '@/api/project';
import { executeFixAgentApi, executeReviewAgentApi, executeSummaryAgentApi } from '@/api/agent';
import type { FixAgentResult, Project, ReviewAgentResult, SummaryAgentResult } from '@/types/api';

const running = ref(false);
const activeStep = ref(0);
const selectedProjectId = ref<string>('');
const projects = ref<Project[]>([]);
const pipelineError = ref('');

const reviewResult = ref<ReviewAgentResult>();
const fixResult = ref<FixAgentResult>();
const summaryResult = ref<SummaryAgentResult>();

const loadProjects = async () => {
  const result = await getProjectPageApi({ current: 1, size: 100 });
  projects.value = result.records ?? [];
};

const statusTagType = (status: string) => {
  switch (status) {
    case 'HEALTHY': return 'success';
    case 'ATTENTION': return 'warning';
    case 'AT_RISK': return 'danger';
    case 'CRITICAL': return 'danger';
    default: return 'info';
  }
};

const scoreClass = (score: number) => {
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-fair';
  return 'score-poor';
};

const startPipeline = async () => {
  if (!selectedProjectId.value) return;

  running.value = true;
  pipelineError.value = '';
  reviewResult.value = undefined;
  fixResult.value = undefined;
  summaryResult.value = undefined;

  try {
    // Step 1: Review
    activeStep.value = 1;
    const reviewRes = await executeReviewAgentApi(selectedProjectId.value);
    if (!reviewRes.success) {
      throw new Error(reviewRes.message || 'ReviewAgent 执行失败');
    }
    reviewResult.value = reviewRes.payload;

    // Step 2: Fix
    activeStep.value = 2;
    const fixRes = await executeFixAgentApi(selectedProjectId.value);
    if (!fixRes.success) {
      throw new Error(fixRes.message || 'FixAgent 执行失败');
    }
    fixResult.value = fixRes.payload;

    // Step 3: Summary
    activeStep.value = 3;
    const summaryRes = await executeSummaryAgentApi(selectedProjectId.value);
    if (!summaryRes.success) {
      throw new Error(summaryRes.message || 'SummaryAgent 执行失败');
    }
    summaryResult.value = summaryRes.payload;

    activeStep.value = 4;
    ElMessage.success('Agent 分析完成');
  } catch (e: any) {
    pipelineError.value = e.message || '分析过程发生错误';
    ElMessage.error(pipelineError.value);
  } finally {
    running.value = false;
  }
};

onMounted(loadProjects);
</script>

<style scoped>
.agent-steps {
  margin: 20px 0;
}
.agent-error {
  margin-top: 16px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-card {
  margin-top: 16px;
}
.inner-card {
  margin-top: 12px;
}
.health-score-box {
  text-align: center;
  padding: 20px;
}
.health-score-value {
  font-size: 48px;
  font-weight: bold;
}
.health-score-value.score-good { color: #67c23a; }
.health-score-value.score-fair { color: #e6a23c; }
.health-score-value.score-poor { color: #f56c6c; }
.health-score-label {
  font-size: 16px;
  color: #909399;
  margin-top: 8px;
}
.top-problem-tag {
  margin: 4px;
}
ul {
  padding-left: 20px;
  margin: 0;
}
li {
  line-height: 1.8;
}
</style>
