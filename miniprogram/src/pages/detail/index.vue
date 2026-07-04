<template>
  <view v-if="workout" class="page">
    <view class="card">
      <text class="date">{{ workout.date }}</text>
      <view class="tags">
        <text v-if="workout.sourceType === 2" class="tag compete">比赛</text>
        <text v-else class="tag source">训练</text>
        <text class="tag">{{ workout.stroke }}</text>
        <text class="tag">{{ workout.poolType === 1 ? '25米池' : '50米池' }}</text>
      </view>
      <view v-if="workout.sourceType === 2 && workout.note" class="note">{{ workout.note }}</view>
      <view class="meta">
        <view><text class="v">{{ workout.totalDistance }}m</text><text class="l">总距离</text></view>
        <view><text class="v">{{ formatDuration(workout.totalDuration) }}</text><text class="l">总时长</text></view>
        <view><text class="v">{{ formatPace(workout.avgPace) }}</text><text class="l">平均配速</text></view>
        <view><text class="v">{{ splits.length }}</text><text class="l">分段数</text></view>
      </view>
    </view>
    <view class="section">分段明细</view>
    <view class="card">
      <view v-for="s in splits" :key="s.id" class="row">
        <text>第{{ s.seq }}段 · {{ s.stroke }}</text>
        <text>{{ s.distance }}m · {{ formatPace(s.pace) }}</text>
      </view>
    </view>
    <view class="actions">
      <button class="outline" @tap="edit">编辑</button>
      <button class="outline" @tap="share">分享</button>
      <button class="danger" @tap="remove">删除</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getWorkout, deleteWorkout, type Workout, type Split } from '@/api/workout'
import { formatDuration, formatPace } from '@/utils/pace'

const workout = ref<Workout | null>(null)
const splits = ref<Split[]>([])
let id = 0

onLoad(async (q) => {
  id = Number(q?.id)
  const res = await getWorkout(id)
  workout.value = res.workout
  splits.value = res.splits
})

function edit() {
  uni.navigateTo({ url: `/pages/record/index?id=${id}` })
}

function share() {
  uni.navigateTo({ url: `/pages/share/index?id=${id}` })
}

function remove() {
  uni.showModal({
    title: '确认删除',
    success: async (r) => {
      if (r.confirm) {
        await deleteWorkout(id)
        uni.navigateBack()
      }
    },
  })
}
</script>

<style scoped>
.card { background: #fff; margin: 12px 16px; padding: 16px; border-radius: 12px; }
.date { font-size: 20px; font-weight: 600; }
.tags { margin-top: 8px; }
.tag { font-size: 11px; background: #E8F6FC; color: #0096D6; padding: 2px 8px; border-radius: 4px; margin-right: 6px; }
.tag.compete { background: #FFF7E6; color: #D48806; }
.tag.source { background: #F6FFED; color: #52c41a; }
.note { font-size: 14px; color: #666; margin-top: 8px; }
.meta { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 12px; }
.v { display: block; font-size: 18px; font-weight: 700; text-align: center; }
.l { font-size: 11px; color: #999; text-align: center; display: block; }
.section { font-size: 14px; color: #999; padding: 8px 16px; }
.row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.actions { display: flex; gap: 10px; padding: 16px; }
.outline { flex: 1; background: #fff; color: #0096D6; border: 1px solid #0096D6; font-size: 14px; }
.danger { flex: 1; background: #fff; color: #fa5151; border: 1px solid #fa5151; font-size: 14px; }
</style>
