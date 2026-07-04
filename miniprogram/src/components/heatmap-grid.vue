<template>
  <view class="heatmap">
    <view v-for="(cell, i) in cells" :key="i" class="cell" :style="{ background: cell.color }" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { HEAT_COLORS, heatLevel } from '@/utils/chart'

const props = defineProps<{ data: { date: string; distance: number }[]; year: number }>()

const cells = computed(() => {
  const map = new Map(props.data.map((d) => [d.date, d.distance]))
  const max = Math.max(...props.data.map((d) => d.distance), 1)
  const start = new Date(props.year, 0, 1)
  const dow = start.getDay()
  const pad = dow === 0 ? 6 : dow - 1
  const result: { color: string }[] = []
  for (let i = 0; i < pad; i++) result.push({ color: HEAT_COLORS[0] })
  const end = new Date(props.year, 11, 31)
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const key = d.toISOString().slice(0, 10)
    const dist = map.get(key) ?? 0
    result.push({ color: HEAT_COLORS[heatLevel(dist, max)] })
  }
  return result
})
</script>

<style scoped>
.heatmap { display: grid; grid-template-columns: repeat(7, 1fr); gap: 3px; padding: 0 16px 16px; }
.cell { aspect-ratio: 1; border-radius: 3px; }
</style>
