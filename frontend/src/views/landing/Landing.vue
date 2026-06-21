<template>
  <div class="landing" @pointermove="handlePointerMove" @pointerleave="handlePointerLeave">
    <Spotlight />
    <div class="landing__grid" aria-hidden="true"></div>
    <div class="landing__glow" :style="glowStyle" aria-hidden="true"></div>

    <nav class="landing-nav">
      <div class="landing-nav__brand">
        <div class="landing-nav__logo">
          <img src="/logo.svg?v=custom-logo" alt="Logo" />
        </div>
        <span class="landing-nav__title">AI Code Reviewer</span>
      </div>
      <div class="landing-nav__actions">
        <el-button round class="landing-nav__btn--ghost" @click="goLogin">登录</el-button>
        <el-button round class="landing-nav__btn--cta" @click="goLogin">开始使用 →</el-button>
      </div>
    </nav>

    <section class="landing-hero">
      <CometCard class="landing-hero__video-card">
        <video
          loop
          muted
          autoplay
          playsinline
          src="/login-bg.mp4"
          class="landing-hero__video"
          aria-label="AI Code Reviewer 产品演示"
        />
        <div class="landing-hero__video-fade" aria-hidden="true"></div>
      </CometCard>

      <div class="landing-hero__content">
        <a
          class="landing-hero__badge"
          href="https://github.com/majinyan02-ops/AiCodeReviewer"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span class="landing-hero__badge-dot"></span>
          开源免费 · 企业级 AI 代码审查平台
        </a>

        <p class="landing-hero__eyebrow">让 AI 成为你的</p>
        <h1 class="landing-hero__title">代码审查专家</h1>

        <p class="landing-hero__desc">
          基于 AI 驱动的智能代码审查工具，自动扫描源码、检测问题、生成审查报告。<br />
          告别繁琐的人工审查，让每一行代码都经得起考验。
        </p>

        <div class="landing-hero__cta">
          <el-button round size="large" class="landing-hero__btn--primary" @click="goLogin">
            立即开始
            <el-icon class="landing-hero__btn-icon"><ArrowRight /></el-icon>
          </el-button>
          <el-button round size="large" class="landing-hero__btn--outline" @click="scrollToFeatures">
            了解更多
            <el-icon class="landing-hero__btn-icon"><Reading /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="landing-hero__scroll-indicator" aria-hidden="true">
        <div class="landing-hero__scroll-mouse">
          <div class="landing-hero__scroll-dot"></div>
        </div>
      </div>
    </section>

    <div class="landing-content">
      <section id="features" class="landing-features">
        <div class="landing-features__header">
          <h2 class="landing-features__title">核心功能</h2>
          <p class="landing-features__desc">全方位覆盖代码审查流程，从扫描到报告一站式完成</p>
        </div>
        <div class="landing-features__grid">
          <div class="feature-card" v-for="feature in features" :key="feature.title">
            <div class="feature-card__icon">{{ feature.icon }}</div>
            <h3 class="feature-card__title">{{ feature.title }}</h3>
            <p class="feature-card__desc">{{ feature.desc }}</p>
          </div>
        </div>
      </section>

      <footer class="landing-footer">
        <p>AI Code Reviewer © 2026 · 开源免费，基于 Vue 3 + Spring AI 构建</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowRight, Reading } from '@element-plus/icons-vue';
import Spotlight from '@/components/animation/Spotlight.vue';
import CometCard from '@/components/animation/CometCard.vue';

const router = useRouter();

onMounted(() => {
  document.documentElement.classList.add('dark');
});

const mouseX = ref(0);
const mouseY = ref(0);
const glowVisible = ref(false);

const glowStyle = computed(() => ({
  '--glow-x': `${mouseX.value}px`,
  '--glow-y': `${mouseY.value}px`,
  opacity: glowVisible.value ? 1 : 0,
}));

const handlePointerMove = (e: PointerEvent) => {
  mouseX.value = e.clientX;
  mouseY.value = e.clientY;
  glowVisible.value = true;
};

const handlePointerLeave = () => {
  glowVisible.value = false;
};

const goLogin = () => {
  router.push('/login');
};

const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' });
};

const features = [
  {
    icon: '🔍',
    title: '智能源码扫描',
    desc: '自动解析 Java 项目结构，识别 Controller、Service 等核心组件，快速定位潜在问题。',
  },
  {
    icon: '🤖',
    title: 'AI 深度分析',
    desc: '基于 Spring AI 驱动，对代码问题进行智能分析，提供修复建议和最佳实践推荐。',
  },
  {
    icon: '📊',
    title: '可视化仪表盘',
    desc: '问题等级分布、审查通过率等关键指标一目了然，数据驱动决策。',
  },
  {
    icon: '📄',
    title: '多格式报告',
    desc: '支持 Markdown 和 PDF 双格式报告导出，便于团队分享和存档。',
  },
  {
    icon: '🔗',
    title: 'Git 仓库集成',
    desc: '一键同步 Git 仓库，自动拉取最新代码，无缝衔接开发工作流。',
  },
  {
    icon: '⚡',
    title: '异步任务引擎',
    desc: '报告生成采用异步任务机制，实时查看进度，无需等待。',
  },
];
</script>
