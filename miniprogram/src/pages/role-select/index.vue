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

    <view class="card disabled">

      <text class="icon">👨‍🏫</text>

      <view class="info">

        <text class="name">我是教练</text>

        <text class="sub">敬请期待，后续版本支持</text>

      </view>

      <text class="tag">即将上线</text>

    </view>

  </view>

</template>



<script setup lang="ts">

import { selectRole } from '@/api/user'



async function pick(role: 'student' | 'coach') {

  await selectRole(role)

  uni.reLaunch({ url: '/pages/index/index' })

}

</script>



<style scoped>

.page { min-height: 100vh; padding: 48px 24px; background: #f5f9fc; }

.title { font-size: 18px; font-weight: 600; text-align: center; margin-bottom: 8px; }

.desc { font-size: 13px; color: #999; text-align: center; margin-bottom: 32px; }

.card { background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; margin-bottom: 12px; position: relative; }

.card.disabled { opacity: 0.55; }

.icon { font-size: 36px; }

.name { font-size: 16px; font-weight: 600; display: block; }

.sub { font-size: 12px; color: #999; margin-top: 4px; display: block; }

.tag { position: absolute; top: 12px; right: 12px; font-size: 10px; color: #fff; background: #bbb; padding: 2px 8px; border-radius: 10px; }

</style>
