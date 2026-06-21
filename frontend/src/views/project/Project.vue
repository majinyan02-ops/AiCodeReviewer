<template>
  <div class="project-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-toolbar">
          <div>
            <h2>项目管理</h2>
            <p>维护待审查的 Git 项目与分支信息，支持仓库同步和状态查看</p>
          </div>
          <el-button type="primary" @click="openCreate">新增项目</el-button>
        </div>
      </template>

      <el-form class="filter-bar" :inline="true">
        <el-form-item label="搜索">
          <el-input v-model.trim="keyword" placeholder="按项目名称 / Git 地址搜索" clearable @clear="loadProjects" />
        </el-form-item>
        <el-button type="primary" @click="loadProjects">查询</el-button>
      </el-form>

      <el-table :data="filteredProjects" v-loading="loading" empty-text="暂无项目">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" min-width="160" />
        <el-table-column prop="gitUrl" label="Git 仓库地址" min-width="260" show-overflow-tooltip />
        <el-table-column prop="branchName" label="分支" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '未启用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" :loading="isSyncing(row.id)" @click="syncRepository(row)">同步仓库</el-button>
            <el-button link type="primary" @click="showRepositoryStatus(row)">仓库状态</el-button>
            <el-button link type="danger" @click="removeProject(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, sizes, prev, pager, next"
          @current-change="loadProjects"
          @size-change="loadProjects"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑项目' : '新增项目'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="Git 地址" prop="gitUrl">
          <el-input v-model.trim="form.gitUrl" placeholder="https://github.com/user/repo.git" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model.trim="form.description" type="textarea" :rows="4" placeholder="请输入项目描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="statusVisible" title="Git 仓库状态" size="520px">
      <template v-if="gitStatus">
        <el-alert
          :title="gitStatus.exists ? '本地仓库已存在，可以进入代码审查扫描' : '本地仓库不存在，请先同步仓库'"
          :type="gitStatus.exists ? 'success' : 'warning'"
          show-icon
          :closable="false"
          class="status-alert"
        />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="项目 ID">{{ gitStatus.projectId }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ gitStatus.projectName }}</el-descriptions-item>
          <el-descriptions-item label="Git 地址">{{ gitStatus.gitUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分支">{{ gitStatus.branch || '-' }}</el-descriptions-item>
          <el-descriptions-item label="本地路径">{{ gitStatus.localPath || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最新提交">{{ gitStatus.latestCommit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交信息">{{ gitStatus.commitMessage || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else description="暂无仓库状态" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { getRepositoryStatusApi, syncRepositoryApi } from '@/api/git';
import { createProjectApi, deleteProjectApi, getProjectPageApi, updateProjectApi } from '@/api/project';
import { connectGitSync, type GitSyncMessage } from '@/utils/websocket';
import type { GitStatus, Project, ProjectForm } from '@/types/api';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const statusVisible = ref(false);
const editingId = ref<number>();
const keyword = ref('');
const formRef = ref<FormInstance>();
const projects = ref<Project[]>([]);
const gitStatus = ref<GitStatus>();
const syncingIds = ref<Set<number>>(new Set());
const page = reactive({ current: 1, size: 10, total: 0 });
const form = reactive<ProjectForm>({ name: '', gitUrl: '', branchName: '', description: '' });

const rules: FormRules<ProjectForm> = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  gitUrl: [{ required: true, message: '请输入 Git 仓库地址', trigger: 'blur' }],
};

const filteredProjects = computed(() => {
  if (!keyword.value) {
    return projects.value;
  }

  return projects.value.filter((item) =>
    [item.name, item.gitUrl, item.branchName].some((value) => value?.toLowerCase().includes(keyword.value.toLowerCase())),
  );
});

const isSyncing = (projectId: number) => syncingIds.value.has(projectId);

const setSyncing = (projectId: number, value: boolean) => {
  const next = new Set(syncingIds.value);

  if (value) {
    next.add(projectId);
  } else {
    next.delete(projectId);
  }

  syncingIds.value = next;
};

const resetForm = () => {
  editingId.value = undefined;
  Object.assign(form, { name: '', gitUrl: '', branchName: '', description: '' });
  formRef.value?.clearValidate();
};

const loadProjects = async () => {
  loading.value = true;
  try {
    const result = await getProjectPageApi({ current: page.current, size: page.size });
    projects.value = result.records ?? [];
    page.total = result.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (project: Project) => {
  editingId.value = project.id;
  Object.assign(form, {
    name: project.name,
    gitUrl: project.gitUrl ?? '',
    branchName: project.branchName ?? '',
    description: project.description ?? '',
  });
  dialogVisible.value = true;
};

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitting.value = true;
  try {
    if (editingId.value) {
      await updateProjectApi(editingId.value, form);
      ElMessage.success('项目已更新');
    } else {
      await createProjectApi(form);
      ElMessage.success('项目已创建');
    }
    dialogVisible.value = false;
    await loadProjects();
  } finally {
    submitting.value = false;
  }
};

const syncRepository = async (project: Project) => {
  if (!project.gitUrl) {
    ElMessage.warning('请先配置 Git 仓库地址');
    return;
  }

  setSyncing(project.id, true);
  let disconnect: (() => void) | null = null;

  try {
    disconnect = connectGitSync(project.id, async (msg: GitSyncMessage) => {
      if (msg.status === 'syncing') {
        ElMessage.info('仓库同步中，请稍候...');
      } else if (msg.status === 'success') {
        ElMessage.success(msg.message || '仓库同步成功');
        gitStatus.value = await getRepositoryStatusApi(project.id);
        statusVisible.value = true;
        setSyncing(project.id, false);
        disconnect?.();
      } else if (msg.status === 'error') {
        ElMessage.error(msg.message || '仓库同步失败');
        setSyncing(project.id, false);
        disconnect?.();
      }
    });

    await syncRepositoryApi(project.id);
  } catch (e) {
    setSyncing(project.id, false);
    disconnect?.();
  }
};

const showRepositoryStatus = async (project: Project) => {
  gitStatus.value = await getRepositoryStatusApi(project.id);
  statusVisible.value = true;
};

const removeProject = async (project: Project) => {
  await ElMessageBox.confirm(`确认删除项目「${project.name}」？`, '删除确认', { type: 'warning' });
  await deleteProjectApi(project.id);
  ElMessage.success('项目已删除');
  await loadProjects();
};

onMounted(loadProjects);
</script>