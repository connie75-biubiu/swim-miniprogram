<template>

  <view class="page">

    <view class="title">请选择你的身份</view>

    <view class="desc">首次登录需选择身份</view>

    <view class="card" @tap="pick('student')">

      <text class="icon">🏊</text>

      <view class="info">

        <text class="name">我是学员</text>

        <text class="sub">记录训练与比赛，追踪泳姿进步</text>

      </view>

    </view>

    <view class="card" @tap="pick('coach')">

      <text class="icon">👨‍🏫</text>

      <view class="info">

        <text class="name">我是教练</text>

        <text class="sub">查看学员训练数据（二期开发中）</text>

      </view>

    </view>

  </view>

</template>



<script setup lang="ts">

import { selectRole } from '@/api/user'



async function pick(role: 'student' | 'coach') {

  await selectRole(role)

  if (role === 'coach') {

    uni.reLaunch({ url: '/pages/coach-home/index' })

  } else {

    uni.reLaunch({ url: '/pages/index/index' })

  }

}

</script>



<style scoped>

.page { min-height: 100vh; padding: 48px 24px; background: #f5f9fc; }

.title { font-size: 18px; font-weight: 600; text-align: center; margin-bottom: 8px; }

.desc { font-size: 13px; color: #999; text-align: center; margin-bottom: 32px; }

.card { background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }

.icon { font-size: 36px; }

.name { font-size: 16px; font-weight: 600; display: block; }

.sub { font-size: 12px; color: #999; margin-top: 4px; display: block; }

</style>

