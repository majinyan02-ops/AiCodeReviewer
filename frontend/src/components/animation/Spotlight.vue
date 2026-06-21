<template>
  <div class="spotlight" aria-hidden="true">
    <div ref="leftRef" class="spotlight__beam spotlight__beam--left">
      <div
        class="spotlight__glow"
        :style="{
          width: `${width}px`,
          height: `${height}px`,
          background: gradientFirst,
          transform: `translateY(${translateY}px) rotate(-45deg)`,
        }"
      />
      <div
        class="spotlight__glow spotlight__glow--sub"
        :style="{
          height: `${height}px`,
          width: `${smallWidth}px`,
          background: gradientSecond,
          transform: 'rotate(-45deg) translate(5%, -50%)',
        }"
      />
      <div
        class="spotlight__glow spotlight__glow--sub"
        :style="{
          height: `${height}px`,
          width: `${smallWidth}px`,
          background: gradientThird,
          transform: 'rotate(-45deg) translate(-180%, -70%)',
        }"
      />
    </div>
    <div ref="rightRef" class="spotlight__beam spotlight__beam--right">
      <div
        class="spotlight__glow"
        :style="{
          width: `${width}px`,
          height: `${height}px`,
          background: gradientFirst,
          transform: `translateY(${translateY}px) rotate(45deg)`,
        }"
      />
      <div
        class="spotlight__glow spotlight__glow--sub"
        :style="{
          height: `${height}px`,
          width: `${smallWidth}px`,
          background: gradientSecond,
          transform: 'rotate(45deg) translate(-5%, -50%)',
        }"
      />
      <div
        class="spotlight__glow spotlight__glow--sub"
        :style="{
          height: `${height}px`,
          width: `${smallWidth}px`,
          background: gradientThird,
          transform: 'rotate(45deg) translate(180%, -70%)',
        }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

const props = withDefaults(defineProps<{
  duration?: number;
  width?: number;
  height?: number;
  smallWidth?: number;
  translateY?: number;
  xOffset?: number;
}>(), {
  duration: 7,
  width: 560,
  height: 1380,
  smallWidth: 240,
  translateY: -350,
  xOffset: 100,
});

const gradientFirst = 'radial-gradient(68.54% 68.72% at 55.02% 31.46%, hsla(0, 0%, 100%, .06) 0, hsla(0, 0%, 100%, .02) 50%, hsla(0, 0%, 100%, 0) 80%)';
const gradientSecond = 'radial-gradient(50% 50% at 50% 50%, hsla(0, 0%, 100%, .04) 0, hsla(0, 0%, 100%, .02) 80%, transparent 100%)';
const gradientThird = 'radial-gradient(50% 50% at 50% 50%, hsla(0, 0%, 100%, .03) 0, hsla(0, 0%, 100%, .01) 80%, transparent 100%)';

const leftRef = ref<HTMLElement>();
const rightRef = ref<HTMLElement>();
let animationFrame = 0;
let startTime = 0;

const animate = (timestamp: number) => {
  if (!startTime) startTime = timestamp;
  const elapsed = (timestamp - startTime) / 1000;
  const { duration, xOffset } = props;

  const progress = (elapsed % (duration * 2)) / duration;
  const eased = progress <= 1
    ? 0.5 - 0.5 * Math.cos(Math.PI * progress)
    : 0.5 - 0.5 * Math.cos(Math.PI * (progress - 1));

  const leftX = eased * xOffset;
  const rightX = -eased * xOffset;

  if (leftRef.value) leftRef.value.style.transform = `translateX(${leftX}px)`;
  if (rightRef.value) rightRef.value.style.transform = `translateX(${rightX}px)`;

  animationFrame = requestAnimationFrame(animate);
};

onMounted(() => {
  animationFrame = requestAnimationFrame(animate);
});

onBeforeUnmount(() => {
  if (animationFrame) cancelAnimationFrame(animationFrame);
});
</script>

<style scoped>
.spotlight {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.spotlight__beam {
  position: absolute;
  top: 0;
  width: 100vw;
  height: 100vh;
  will-change: transform;
}

.spotlight__beam--left {
  left: 0;
}

.spotlight__beam--right {
  right: 0;
}

.spotlight__glow {
  position: absolute;
  top: 0;
  will-change: transform;
}

.spotlight__glow--sub {
  transform-origin: top left;
}

.spotlight__beam--right .spotlight__glow--sub {
  transform-origin: top right;
}
</style>
