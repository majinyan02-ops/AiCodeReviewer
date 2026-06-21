<template>
  <el-container class="app-layout">
    <el-aside width="232px" class="app-layout__aside">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header class="app-layout__header">
        <AppHeader />
      </el-header>
      <el-main class="app-layout__main">
        <RouterView />
      </el-main>
      <el-footer class="app-layout__footer">AI Code Reviewer © 2026</el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import AppHeader from './components/AppHeader.vue';
import AppSidebar from './components/AppSidebar.vue';

onMounted(() => {
  document.documentElement.classList.remove('dark');
});
</script>