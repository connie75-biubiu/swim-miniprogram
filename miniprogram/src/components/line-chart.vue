<template>
  <view class="chart-wrap">
    <canvas
      :canvas-id="canvasId"
      :id="canvasId"
      class="canvas"
      :style="{ width: width + 'px', height: height + 'px' }"
    />
    <view v-if="legend.length" class="legend">
      <view v-for="item in legend" :key="item.name" class="legend-item">
        <view class="dot" :style="{ background: item.color }" />
        <text>{{ item.name }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { drawLineChart, type ChartSeries } from '@/utils/chart'

const props = defineProps<{
  canvasId: string
  width?: number
  height?: number
  labels: string[]
  series: ChartSeries[]
  legend?: { name: string; color: string }[]
}>()

const width = props.width ?? 320
const height = props.height ?? 180
const legend = props.legend ?? []

function render() {
  drawLineChart(props.canvasId, width, height, props.labels, props.series)
}

onMounted(render)
watch(() => [props.labels, props.series], render, { deep: true })
</script>

<style scoped>
.chart-wrap { padding: 0 4px; }
.canvas { display: block; }
.legend { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 8px; padding: 0 8px; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 11px; color: #666; }
.dot { width: 10px; height: 10px; border-radius: 2px; }
</style>
