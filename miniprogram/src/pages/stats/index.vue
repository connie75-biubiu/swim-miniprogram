<template>
  <view class="page">
    <view class="tabs">
      <text :class="tabClass('week')" @tap="setPeriod('week')">本周</text>
      <text :class="tabClass('month')" @tap="setPeriod('month')">本月</text>
    </view>
    <view class="card summary">
      <view><text class="n">{{ summary.totalDistance }}m</text><text class="l">总距离</text></view>
      <view><text class="n">{{ formatDuration(summary.totalDuration) }}</text><text class="l">总时长</text></view>
      <view><text class="n">{{ summary.count }}</text><text class="l">训练次数</text></view>
    </view>

    <view class="dist-tabs">
      <text v-for="d in DISTANCES" :key="d" :class="distTabClass(d)" @tap="setDist(d)">{{ d }}米</text>
    </view>

    <view class="section">各泳姿速度趋势（近4周）</view>
    <text class="hint">线条下降 = 速度提升</text>
    <view class="card">
      <line-chart
        canvas-id="statsStrokeChart"
        :labels="chartLabels"
        :series="chartSeries"
        :legend="chartLegend"
      />
    </view>

    <view class="section">速度提升排名</view>
    <view class="card">
      <view v-for="(item, idx) in improveList" :key="item.stroke" class="row">
        <text>{{ rankIcon(idx) }} {{ item.stroke }}</text>
        <text :class="improveClass(item.improveSeconds)">{{ improveText(item.improveSeconds) }}</text>
      </view>
      <view v-if="!improveList.length" class="empty">暂无数据</view>
    </view>

    <view class="section">个人最佳 (PR)</view>
    <view class="card">
      <view v-for="p in prList" :key="p.stroke" class="row">
        <text>{{ p.stroke }}</text>
        <text>最快 {{ formatPace(p.bestPace) }}</text>
      </view>
    </view>

    <view class="section">训练热力图</view>
    <heatmap-grid :year="year" :data="heatmapData" />
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import LineChart from '@/components/line-chart.vue'
import HeatmapGrid from '@/components/heatmap-grid.vue'
import { getSummary, getPr, getHeatmap, getStrokeOverview, getStrokeImprove } from '@/api/stats'
import { DISTANCES, STROKES } from '@/utils/constants'
import { STROKE_CHART_COLORS } from '@/utils/chart'
import { formatDuration, formatPace } from '@/utils/pace'

const period = ref<'week' | 'month'>('week')
const chartDist = ref(100)
const year = new Date().getFullYear()
const summary = reactive({ totalDistance: 0, totalDuration: 0, count: 0 })
const prList = ref<{ stroke: string; bestPace: number; maxDistance: number }[]>([])
const heatmapData = ref<{ date: string; distance: number }[]>([])
const overview = ref<Awaited<ReturnType<typeof getStrokeOverview>>>([])
const improveList = ref<{ stroke: string; improveSeconds: number }[]>([])

const chartLabels = computed(() => {
  const sample = overview.value.find((o) => o.stroke === '自由泳' && o.distance === chartDist.value)
  return (sample?.trend ?? []).map((t, i) => `W${i + 1}`)
})

const chartSeries = computed(() =>
  STROKES.map((stroke) => {
    const item = overview.value.find((o) => o.stroke === stroke && o.distance === chartDist.value)
    return {
      name: stroke,
      color: STROKE_CHART_COLORS[stroke] ?? '#0096D6',
      data: (item?.trend ?? []).map((t) => Number(t.avgPace)),
      dashed: stroke === '蝶泳',
    }
  }).filter((s) => s.data.length > 0),
)

const chartLegend = computed(() =>
  chartSeries.value.map((s) => ({ name: s.name, color: s.color })),
)

onShow(async () => {
  if (!uni.getStorageSync('token')) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  await loadSummary()
  prList.value = await getPr()
  heatmapData.value = await getHeatmap(year)
  await loadCharts()
})

async function loadSummary() {
  const s = await getSummary(period.value)
  summary.totalDistance = s.totalDistance
  summary.totalDuration = s.totalDuration
  summary.count = s.count
}

async function loadCharts() {
  overview.value = await getStrokeOverview(4)
  improveList.value = await getStrokeImprove(chartDist.value, 4)
}

function setPeriod(p: 'week' | 'month') {
  period.value = p
  loadSummary()
}

function setDist(d: number) {
  chartDist.value = d
  loadCharts()
}

function tabClass(p: string) {
  return period.value === p ? 'tab active' : 'tab'
}

function distTabClass(d: number) {
  return chartDist.value === d ? 'dist-tab active' : 'dist-tab'
}

function rankIcon(idx: number) {
  return ['🥇', '🥈', '🥉', ''][idx] ?? ''
}

function improveText(sec: number) {
  if (sec > 0) return `↑ 提升 ${Math.round(sec)}秒/100m`
  if (sec < 0) return `↓ 退步 ${Math.abs(Math.round(sec))}秒/100m`
  return '持平'
}

function improveClass(sec: number) {
  return sec >= 0 ? 'up' : 'down'
}
</script>

<style scoped>
.tabs, .dist-tabs { display: flex; margin: 12px 16px; background: #E8F0F5; border-radius: 8px; padding: 3px; }
.tab, .dist-tab { flex: 1; text-align: center; padding: 8px; font-size: 13px; color: #666; border-radius: 6px; }
.tab.active, .dist-tab.active { background: #fff; color: #0096D6; font-weight: 600; }
.card { background: #fff; margin: 0 16px 12px; padding: 16px; border-radius: 12px; }
.summary { display: flex; justify-content: space-around; text-align: center; }
.n { display: block; font-size: 20px; font-weight: 700; color: #0096D6; }
.l { font-size: 12px; color: #999; }
.section { font-size: 14px; color: #999; padding: 8px 16px 4px; }
.hint { font-size: 11px; color: #999; padding: 0 16px 8px; display: block; }
.row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.up { color: #52c41a; }
.down { color: #fa8c16; }
.empty { text-align: center; color: #ccc; padding: 16px; }
</style>
