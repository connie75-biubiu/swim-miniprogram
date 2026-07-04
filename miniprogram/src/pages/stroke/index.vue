<template>
  <view class="page">
    <view v-if="improveText" class="improve">{{ improveText }}</view>
    <view class="dist-tabs">
      <text v-for="d in DISTANCES" :key="d" :class="distTabClass(d)" @tap="setDist(d)">{{ d }}米</text>
    </view>
    <view class="period-tabs">
      <text :class="weekTabClass(4)" @tap="setWeeks(4)">近4周</text>
      <text :class="weekTabClass(8)" @tap="setWeeks(8)">近8周</text>
      <text :class="weekTabClass(12)" @tap="setWeeks(12)">近12周</text>
    </view>
    <text class="hint">↓ 配速越低 = 速度越快，线条下降表示进步</text>
    <view class="card">
      <line-chart
        canvas-id="strokeTrendChart"
        :labels="chartLabels"
        :series="chartSeries"
      />
    </view>
    <view class="section">每周明细</view>
    <view class="card">
      <view v-for="(p, i) in trend" :key="i" class="row">
        <text>第{{ i + 1 }}周 ({{ p.week }})</text>
        <text :class="i === fastestIdx ? 'best' : ''">
          {{ formatPace(p.avgPace) }}/100m
          <text v-if="i === fastestIdx" class="badge">最快</text>
        </text>
      </view>
      <view v-if="!trend.length" class="empty">暂无数据</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LineChart from '@/components/line-chart.vue'
import { getStrokeTrend } from '@/api/stats'
import { DISTANCES } from '@/utils/constants'
import { formatPace } from '@/utils/pace'

const stroke = ref('自由泳')
const dist = ref(100)
const weeks = ref(4)
const trend = ref<{ week: string; avgPace: number }[]>([])

const chartLabels = computed(() => trend.value.map((_, i) => `W${i + 1}`))
const chartSeries = computed(() => [{
  name: stroke.value,
  color: '#0096D6',
  data: trend.value.map((t) => Number(t.avgPace)),
}])

const fastestIdx = computed(() => {
  if (!trend.value.length) return -1
  let idx = 0
  trend.value.forEach((t, i) => {
    if (t.avgPace < trend.value[idx].avgPace) idx = i
  })
  return idx
})

const improveText = computed(() => {
  if (trend.value.length < 2) return ''
  const first = trend.value[0].avgPace
  const last = trend.value[trend.value.length - 1].avgPace
  const diff = first - last
  if (diff > 0) return `📈 近${weeks.value}周配速提升 ${Math.round(diff)}秒`
  if (diff < 0) return `📉 近${weeks.value}周配速退步 ${Math.abs(Math.round(diff))}秒`
  return '📊 配速持平'
})

onLoad((q) => {
  if (q?.stroke) stroke.value = decodeURIComponent(String(q.stroke))
  uni.setNavigationBarTitle({ title: `${stroke.value} · 速度趋势` })
  load()
})

async function load() {
  trend.value = await getStrokeTrend(stroke.value, dist.value, weeks.value)
}

function setDist(d: number) {
  dist.value = d
  load()
}

function setWeeks(w: number) {
  weeks.value = w
  load()
}

function distTabClass(d: number) {
  return dist.value === d ? 'dist-tab active' : 'dist-tab'
}

function weekTabClass(w: number) {
  return weeks.value === w ? 'period-tab active' : 'period-tab'
}
</script>

<style scoped>
.improve { font-size: 12px; color: #52c41a; background: #F6FFED; padding: 6px 12px; border-radius: 12px; margin: 12px 16px 0; display: inline-block; }
.dist-tabs, .period-tabs { display: flex; margin: 12px 16px; background: #E8F0F5; border-radius: 8px; padding: 3px; }
.dist-tab, .period-tab { flex: 1; text-align: center; padding: 7px 4px; font-size: 12px; color: #666; border-radius: 6px; }
.dist-tab.active, .period-tab.active { background: #fff; color: #0096D6; font-weight: 600; }
.hint { font-size: 11px; color: #999; padding: 0 16px 8px; display: block; }
.card { background: #fff; margin: 0 16px 12px; padding: 16px; border-radius: 12px; }
.section { font-size: 14px; color: #999; padding: 8px 16px; }
.row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.best { color: #0096D6; font-weight: 600; }
.badge { font-size: 10px; background: #E6FFFB; color: #13c2c2; padding: 2px 6px; border-radius: 4px; margin-left: 4px; }
.empty { text-align: center; color: #ccc; padding: 24px; }
</style>
