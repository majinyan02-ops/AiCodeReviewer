<template>
  <div class="app-sidebar">
    <div class="app-sidebar__brand">
      <div class="app-sidebar__logo">
        <img src="/logo.svg?v=custom-logo" alt="AI Code Reviewer logo" />
      </div>
      <div>
        <div class="app-sidebar__title">Code Reviewer</div>
        <div class="app-sidebar__subtitle">Management Console</div>
      </div>
    </div>

    <el-menu router :default-active="route.path" class="app-sidebar__menu">
      <el-menu-item index="/app/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>Dashboard</span>
      </el-menu-item>
      <el-menu-item index="/app/project">
        <el-icon><Folder /></el-icon>
        <span>项目管理</span>
      </el-menu-item>
      <el-menu-item index="/app/agent">
        <el-icon><Monitor /></el-icon>
        <span>Agent 分析</span>
      </el-menu-item>
      <el-menu-item index="/app/report">
        <el-icon><Document /></el-icon>
        <span>报告中心</span>
      </el-menu-item>
      <el-menu-item index="/app/history">
        <el-icon><Clock /></el-icon>
        <span>历史记录</span>
      </el-menu-item>
      <el-menu-item index="/app/trend">
        <el-icon><TrendCharts /></el-icon>
        <span>趋势统计</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { DataBoard, Document, Folder, Monitor, Clock, TrendCharts } from '@element-plus/icons-vue';
import { useRoute } from 'vue-router';

const route = useRoute();
</script>