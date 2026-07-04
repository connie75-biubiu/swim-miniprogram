<template>

  <view class="page">

    <view v-if="summary" class="banner">🏆 共 {{ summary.totalCount }} 场 · 最佳 {{ formatPace(summary.bestPace) }}</view>

    <view class="section">全部比赛</view>

    <view v-for="w in records" :key="w.id" class="card" @tap="goDetail(w.id)">

      <view>

        <text class="title">{{ w.date }} · {{ w.note || '比赛' }}</text>

        <view class="tags">

          <text class="tag compete">比赛</text>

          <text class="tag pool">{{ w.poolType === 1 ? '25米池' : '50米池' }}</text>

          <text class="tag">{{ w.totalDistance }}m</text>

        </view>

      </view>

      <view class="right">

        <text class="pace">{{ formatPace(w.avgPace) }}</text>

        <text class="sub">/100m</text>

      </view>

    </view>

    <view v-if="!records.length" class="empty">暂无比赛记录</view>

  </view>

</template>



<script setup lang="ts">

import { ref } from 'vue'

import { onLoad } from '@dcloudio/uni-app'

import { getCompetitions, type Workout } from '@/api/workout'

import { formatPace } from '@/utils/pace'



const stroke = ref('自由泳')

const records = ref<Workout[]>([])

const summary = ref<{ totalCount: number; bestPace: number | null } | null>(null)



onLoad(async (q) => {

  if (q?.stroke) stroke.value = decodeURIComponent(String(q.stroke))

  uni.setNavigationBarTitle({ title: `${stroke.value} · 比赛记录` })

  const res = await getCompetitions(stroke.value)

  records.value = res.records

  summary.value = { totalCount: res.totalCount, bestPace: res.bestPace }

})



function goDetail(id: number) {

  uni.navigateTo({ url: `/pages/detail/index?id=${id}` })

}

</script>



<style scoped>

.banner { font-size: 12px; color: #D48806; background: #FFF7E6; padding: 8px 16px; margin: 12px 16px 0; border-radius: 12px; }

.section { font-size: 14px; color: #999; padding: 12px 16px 8px; }

.card { background: #fff; margin: 0 16px 10px; padding: 16px; border-radius: 12px; display: flex; justify-content: space-between; }

.title { font-size: 16px; font-weight: 600; display: block; }

.tags { margin-top: 4px; }

.tag { font-size: 11px; background: #E8F6FC; color: #0096D6; padding: 2px 8px; border-radius: 4px; margin-right: 6px; }

.tag.pool { background: #E3F0FF; color: #3B82F6; }

.tag.compete { background: #FFF7E6; color: #D48806; }

.right { text-align: right; }

.pace { font-size: 18px; font-weight: 700; color: #D48806; display: block; }

.sub { font-size: 12px; color: #999; }

.empty { text-align: center; color: #ccc; padding: 40px; }

</style>

