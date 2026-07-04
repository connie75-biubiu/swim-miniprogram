<template>

  <view class="page">

    <scroll-view scroll-y class="scroll" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">

      <view class="section-title">泳姿进步</view>

      <stroke-card-list

        :order="strokeOrder"

        :pace-map="paceMap"

        :change-map="changeMap"

        @update:order="onReorder"

        @tap="goStroke"

      />



      <view class="section-title">训练记录</view>

      <view v-for="w in workouts" :key="w.id" class="card" @tap="goDetail(w.id)">

        <view>

          <text class="card-title">{{ w.date }} · {{ w.stroke }}</text>

          <view class="tags">

            <text class="tag">{{ w.stroke }}</text>

            <text class="tag pool">{{ w.poolType === 1 ? '25米池' : '50米池' }}</text>

            <text class="tag source">训练</text>

          </view>

        </view>

        <view class="right">

          <text class="dist">{{ w.totalDistance }}m</text>

          <text class="pace">{{ formatPace(w.avgPace) }}</text>

        </view>

      </view>

      <view v-if="!workouts.length" class="empty">暂无记录，点右下角添加</view>

      <view v-if="hasMore" class="load-more" @tap="loadMore">加载更多</view>



      <view class="section-title">比赛记录</view>

      <view class="compete-grid">

        <view

          v-for="item in competeSummary"

          :key="item.stroke"

          class="compete-card"

          :class="{ empty: !item.count }"

          @tap="item.count && goCompetition(item.stroke)"

        >

          <text class="cc-icon">{{ STROKE_ICONS[item.stroke] }}</text>

          <text class="cc-name">{{ item.stroke }}</text>

          <text class="cc-count">{{ item.count ? `${item.count} 场比赛` : '暂无比赛' }}</text>

          <text class="cc-best">

            {{ item.bestPace != null ? formatPace(item.bestPace) : '—' }}

            <text class="cc-sub">{{ item.count ? '最佳配速' : '' }}</text>

          </text>

        </view>

      </view>

    </scroll-view>



    <view class="fab" @tap="goRecord">+</view>

  </view>

</template>



<script setup lang="ts">

import { ref } from 'vue'

import { onShow } from '@dcloudio/uni-app'

import StrokeCardList from '@/components/stroke-card-list.vue'

import {

  getWorkouts,

  getCompetitionSummary,

  SOURCE_TRAIN,

  type Workout,

  type CompetitionSummary,

} from '@/api/workout'

import { getStrokeOrder } from '@/api/auth'

import { getStrokeOverview } from '@/api/stats'

import { DEFAULT_STROKE_ORDER, STROKE_ICONS } from '@/utils/constants'

import { formatPace } from '@/utils/pace'



const workouts = ref<Workout[]>([])

const competeSummary = ref<CompetitionSummary[]>([])

const strokeOrder = ref<string[]>([...DEFAULT_STROKE_ORDER])

const paceMap = ref<Record<string, Record<number, number | null>>>({})

const changeMap = ref<Record<string, Record<number, number | null>>>({})

const refreshing = ref(false)

const page = ref(1)

const hasMore = ref(false)



onShow(async () => {

  if (!uni.getStorageSync('token')) {

    uni.reLaunch({ url: '/pages/login/index' })

    return

  }

  page.value = 1

  await load()

})



async function load() {

  try {

    const local = uni.getStorageSync('stroke_order') as string[] | ''

    if (local && local.length) strokeOrder.value = local

    else strokeOrder.value = await getStrokeOrder()

  } catch {

    strokeOrder.value = [...DEFAULT_STROKE_ORDER]

  }

  const res = await getWorkouts(page.value, 20, SOURCE_TRAIN)

  workouts.value = page.value === 1 ? res.records : [...workouts.value, ...res.records]

  hasMore.value = workouts.value.length < res.total

  competeSummary.value = await getCompetitionSummary()

  await loadOverview()

}



async function loadOverview() {

  const overview = await getStrokeOverview(4)

  const pMap: Record<string, Record<number, number | null>> = {}

  const cMap: Record<string, Record<number, number | null>> = {}

  for (const item of overview) {

    if (!pMap[item.stroke]) {

      pMap[item.stroke] = {}

      cMap[item.stroke] = {}

    }

    pMap[item.stroke][item.distance] = item.latestPace

    cMap[item.stroke][item.distance] = item.changeSeconds

  }

  paceMap.value = pMap

  changeMap.value = cMap

}



async function onRefresh() {

  refreshing.value = true

  page.value = 1

  await load()

  refreshing.value = false

}



async function loadMore() {

  page.value++

  await load()

}



function onReorder(order: string[]) {

  strokeOrder.value = order

}



function goRecord() {

  uni.navigateTo({ url: '/pages/record/index' })

}



function goStroke(stroke: string) {

  uni.navigateTo({ url: `/pages/stroke/index?stroke=${encodeURIComponent(stroke)}` })

}



function goDetail(id: number) {

  uni.navigateTo({ url: `/pages/detail/index?id=${id}` })

}



function goCompetition(stroke: string) {

  uni.navigateTo({ url: `/pages/competition/index?stroke=${encodeURIComponent(stroke)}` })

}

</script>



<style scoped>

/* tabBar 页面 page 容器高度已不含 tabBar；用 flex 列布局让 scroll-view 自适应剩余空间，
   避免 100vh 撑满整个视口导致底部被原生 tabBar 遮挡（蝶泳等末尾内容看不到）*/
.page { height: 100%; display: flex; flex-direction: column; }
.scroll { flex: 1; min-height: 0; padding-bottom: 100px; box-sizing: border-box; }

.section-title { font-size: 14px; color: #999; padding: 16px 16px 8px; }

.card { background: #fff; margin: 0 16px 12px; padding: 16px; border-radius: 12px; display: flex; justify-content: space-between; }

.card-title { font-size: 16px; font-weight: 600; display: block; }

.tags { margin-top: 4px; }

.tag { font-size: 11px; background: #E8F6FC; color: #0096D6; padding: 2px 8px; border-radius: 4px; margin-right: 6px; }

.tag.pool { background: #E3F0FF; color: #3B82F6; }

.tag.source { background: #F6FFED; color: #52c41a; }

.right { text-align: right; }

.dist { font-size: 18px; font-weight: 700; color: #0096D6; display: block; }

.pace { font-size: 12px; color: #999; }

.empty { text-align: center; color: #ccc; padding: 40px; }

.load-more { text-align: center; color: #0096D6; padding: 16px; font-size: 14px; }

.compete-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; padding: 0 16px 16px; }

.compete-card { background: #fff; border-radius: 12px; padding: 14px; box-shadow: 0 1px 4px rgba(0,100,150,.06); }

.compete-card.empty { opacity: .55; }

.cc-icon { font-size: 24px; display: block; }

.cc-name { font-size: 14px; font-weight: 600; display: block; margin-top: 4px; }

.cc-count { font-size: 11px; color: #999; display: block; margin-top: 4px; }

.cc-best { font-size: 13px; color: #D48806; font-weight: 600; display: block; margin-top: 6px; }

.cc-sub { font-size: 10px; color: #999; font-weight: 400; display: block; }

.fab { position: fixed; right: 20px; bottom: 80px; width: 52px; height: 52px; background: #0096D6; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; box-shadow: 0 4px 12px rgba(0,150,214,.4); z-index: 10; }

</style>

