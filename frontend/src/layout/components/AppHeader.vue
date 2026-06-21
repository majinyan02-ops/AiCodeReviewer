<template>
  <div class="app-header">
    <div>
      <div class="app-header__title">{{ pageTitle }}</div>
      <div class="app-header__desc">企业级 AI 代码审查管理后台</div>
    </div>

    <div class="app-header__user">
      <el-avatar :size="36">{{ usernameInitial }}</el-avatar>
      <span>{{ username }}</span>
      <el-button type="primary" plain @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const pageTitle = computed(() => String(route.meta.title ?? 'AI Code Reviewer'));
const username = computed(() => userStore.userInfo?.username ?? 'Admin');
const usernameInitial = computed(() => username.value.slice(0, 1).toUpperCase());

const logout = () => {
  userStore.clearSession();
  router.replace('/');
};
</script>