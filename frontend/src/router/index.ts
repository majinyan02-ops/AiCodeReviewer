import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store/user';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/landing/Landing.vue'),
    meta: { public: true, title: 'AI Code Reviewer' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/app',
    component: () => import('@/layout/AppLayout.vue'),
    redirect: '/app/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: 'Dashboard' },
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('@/views/project/Project.vue'),
        meta: { title: '项目管理' },
      },
      {
        path: 'agent',
        name: 'Agent',
        component: () => import('@/views/agent/Agent.vue'),
        meta: { title: 'Agent 智能分析' },
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/Report.vue'),
        meta: { title: '报告中心' },
      },
      {
        path: 'report/:projectId',
        name: 'ReportProject',
        component: () => import('@/views/report/Report.vue'),
        meta: { title: '报告中心' },
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/history/History.vue'),
        meta: { title: '历史记录' },
      },
      {
        path: 'history/:id',
        name: 'HistoryDetail',
        component: () => import('@/views/history/HistoryDetail.vue'),
        meta: { title: '记录详情' },
      },
      {
        path: 'trend',
        name: 'Trend',
        component: () => import('@/views/trend/Trend.vue'),
        meta: { title: '趋势统计' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const userStore = useUserStore();

  if (to.meta.public) {
    if (to.path === '/login' && userStore.isLogin) {
      try {
        await userStore.loadUserInfo();
        return '/app/dashboard';
      } catch {
        userStore.clearSession();
        return true;
      }
    }

    return true;
  }

  if (!userStore.isLogin) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    };
  }

  try {
    await userStore.loadUserInfo();
    return true;
  } catch {
    userStore.clearSession();
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    };
  }
});