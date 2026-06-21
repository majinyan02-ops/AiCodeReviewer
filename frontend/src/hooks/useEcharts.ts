import * as echarts from 'echarts';
import { onBeforeUnmount, onMounted, ref, type Ref } from 'vue';

export const useEcharts = (containerRef: Ref<HTMLElement | undefined>) => {
  const chart = ref<echarts.ECharts>();

  const resize = () => chart.value?.resize();

  onMounted(() => {
    if (containerRef.value) {
      chart.value = echarts.init(containerRef.value);
      window.addEventListener('resize', resize);
    }
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize);
    chart.value?.dispose();
  });

  return chart;
};