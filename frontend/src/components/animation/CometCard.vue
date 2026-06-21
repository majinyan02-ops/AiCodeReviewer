<template>
  <div class="comet-card" :style="{ perspective: '1000px' }">
    <div
      ref="cardRef"
      class="comet-card__inner"
      :style="cardStyle"
      @mousemove="handleMouseMove"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
    >
      <slot />
      <div
        class="comet-card__glare"
        :style="{ background: glareBackground, opacity: isHovering ? glareOpacity : 0 }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

const props = withDefaults(defineProps<{
  rotateDepth?: number;
  translateDepth?: number;
  glareOpacity?: number;
  scaleFactor?: number;
}>(), {
  rotateDepth: 12,
  translateDepth: 26,
  glareOpacity: 0.12,
  scaleFactor: 1.02,
});

const cardRef = ref<HTMLElement>();
const xPct = ref(0);
const yPct = ref(0);
const isHovering = ref(false);

const cardStyle = computed(() => ({
  transform: isHovering.value
    ? `rotateX(${-yPct.value * props.rotateDepth}deg) rotateY(${xPct.value * props.rotateDepth}deg) translateX(${-xPct.value * props.translateDepth}px) translateY(${yPct.value * props.translateDepth}px) scale(${props.scaleFactor})`
    : 'rotateX(0deg) rotateY(0deg) scale(1)',
  transition: isHovering.value ? 'transform 0.1s ease-out' : 'transform 0.5s ease-out',
}));

const glareX = computed(() => (xPct.value + 0.5) * 100);
const glareY = computed(() => (yPct.value + 0.5) * 100);

const glareBackground = computed(
  () => `radial-gradient(circle at ${glareX.value}% ${glareY.value}%, rgba(255, 255, 255, 0.9) 10%, rgba(255, 255, 255, 0.75) 20%, rgba(255, 255, 255, 0) 80%)`,
);

const handleMouseMove = (e: MouseEvent) => {
  if (!cardRef.value) return;
  const rect = cardRef.value.getBoundingClientRect();
  const mouseX = e.clientX - rect.left;
  const mouseY = e.clientY - rect.top;
  xPct.value = mouseX / rect.width - 0.5;
  yPct.value = mouseY / rect.height - 0.5;
};

const handleMouseEnter = () => {
  isHovering.value = true;
};

const handleMouseLeave = () => {
  isHovering.value = false;
  xPct.value = 0;
  yPct.value = 0;
};
</script>

<style scoped>
.comet-card {
  transform-style: preserve-3d;
}

.comet-card__inner {
  position: relative;
  border-radius: 12px;
  will-change: transform;
}

.comet-card__glare {
  position: absolute;
  inset: 0;
  z-index: 50;
  pointer-events: none;
  border-radius: 12px;
  mix-blend-mode: overlay;
  will-change: opacity, background;
  transition: opacity 0.2s ease;
}
</style>
