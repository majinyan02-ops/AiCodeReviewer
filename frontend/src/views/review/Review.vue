<template>
  <div class="review-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-toolbar">
          <div>
            <h2>代码审查</h2>
            <p>选择项目后执行源码扫描，并异步生成规则检测与 AI 分析报告</p>
          </div>
          <el-button type="primary" :disabled="!selectedProjectId" :loading="running" @click="startReview">
            {{ running ? '生成中' : '生成报告' }}
          </el-button>
        </div>
      </template>

      <el-form class="filter-bar" :inline="true">
        <el-form-item label="项目">
          <el-select v-model="selectedProjectId" filterable placeholder="请选择项目" style="width: 320px">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-button @click="loadProjects">刷新项目</el-button>
      </el-form>

      <el-steps :active="activeStep" finish-status="success" class="review-steps">
        <el-step title="选择项目" />
        <el-step title="扫描源码" />
        <el-step title="提交报告任务" />
        <el-step title="AI 分析" />
        <el-step title="完成分析" />
      </el-steps>
    </el-card>

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


    <el-card v-if="scanContext" shadow="never" class="section-card">
      <template #header>扫描结果</template>
      <el-progress :percentage="100" status="success" />
      <el-descriptions :column="4" border class="scan-meta">
        <el-descriptions-item label="文件总数">{{ scanContext.totalFiles }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ scanContext.elapsedMs }} ms</el-descriptions-item>
        <el-descriptions-item label="Controller">{{ scanContext.controllers?.length ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="Service">{{ scanContext.services?.length ?? 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getProjectPageApi } from '@/api/project';
import { generateReportAsyncApi, scanProjectApi } from '@/api/review';
import type { Project, ReportTaskProgress, ScanContext } from '@/types/api';

const router = useRouter();
const running = ref(false);
const activeStep = ref(0);
const selectedProjectId = ref<number>();
const projects = ref<Project[]>([]);
const scanContext = ref<ScanContext>();
const taskProgress = ref<ReportTaskProgress>();

const progressStatus = computed(() => {
  if (taskProgress.value?.status === 'SUCCESS') {
    return 'success';
  }
  if (taskProgress.value?.status === 'FAILED' || taskProgress.value?.status === 'NOT_FOUND') {
    return 'exception';
  }
  return undefined;
});

const loadProjects = async () => {
  const result = await getProjectPageApi({ current: 1, size: 100 });
  projects.value = result.records ?? [];
};

const startReview = async () => {
  if (!selectedProjectId.value) {
    return;
  }

  running.value = true;
  activeStep.value = 1;
  scanContext.value = undefined;
  taskProgress.value = undefined;

  try {
    scanContext.value = await scanProjectApi(selectedProjectId.value);
    activeStep.value = 2;
    const taskId = await generateReportAsyncApi(selectedProjectId.value);
    taskProgress.value = {
      taskId,
      status: 'RUNNING',
      stage: '报告任务已提交，正在进入报告中心',
      percent: 0,
      report: null,
    };
    activeStep.value = 3;
    await router.push({ name: 'ReportProject', params: { projectId: selectedProjectId.value } });
  } catch (error) {
    running.value = false;
    throw error;
  }
};

onMounted(loadProjects);
</script>