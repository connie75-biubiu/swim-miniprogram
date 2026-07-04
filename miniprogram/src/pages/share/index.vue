<template>
  <view class="page" v-if="workout">
    <canvas canvas-id="shareCanvas" class="hidden-canvas" />
    <view class="preview">
      <text class="brand">🏊 泳记</text>
      <text class="big">{{ workout.totalDistance }}m</text>
      <text class="info">{{ workout.date }} · {{ workout.stroke }} · {{ poolLabel }}</text>
      <text class="sub">配速 {{ formatPace(workout.avgPace) }} · 用时 {{ formatDuration(workout.totalDuration) }}</text>
    </view>
    <image v-if="imgPath" class="img" :src="imgPath" mode="widthFix" />
    <button class="btn" open-type="share">发送给好友</button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { getWorkout, type Workout } from '@/api/workout'
import { drawShareCard } from '@/utils/share-card'
import { formatDuration, formatPace } from '@/utils/pace'

const workout = ref<Workout | null>(null)
const imgPath = ref('')
const poolLabel = ref('')

onLoad(async (q) => {
  const id = Number(q?.id)
  const res = await getWorkout(id)
  workout.value = res.workout
  poolLabel.value = res.workout.poolType === 1 ? '25米池' : '50米池'
  imgPath.value = await drawShareCard('shareCanvas', res.workout)
})

onShareAppMessage(() => ({
  title: `${workout.value?.date} 游泳 ${workout.value?.totalDistance}m`,
  path: `/pages/login/index`,
  imageUrl: imgPath.value,
}))
</script>

<style scoped>
.page { padding: 24px 16px; display: flex; flex-direction: column; align-items: center; }
.hidden-canvas { position: fixed; left: -9999px; width: 300px; height: 400px; }
.preview { background: #fff; border: 2px dashed #0096D6; border-radius: 12px; padding: 24px; text-align: center; width: 100%; box-sizing: border-box; margin-bottom: 16px; }
.brand { font-size: 14px; color: #999; display: block; }
.big { font-size: 28px; font-weight: 700; color: #0096D6; display: block; margin: 8px 0; }
.info { font-size: 16px; color: #333; display: block; }
.sub { font-size: 14px; color: #0096D6; display: block; margin-top: 8px; }
.img { width: 100%; margin-bottom: 16px; border-radius: 8px; }
.btn { width: 100%; background: #0096D6; color: #fff; border-radius: 8px; }
</style>
