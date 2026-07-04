<template>

  <view class="page">

    <text class="icon">👨‍🏫</text>

    <text class="title">教练端即将开放</text>

    <text class="desc">教练端功能正在开发中</text>

    <button class="btn" @tap="toStudent">切换为学员</button>

    <button class="outline" @tap="reselect">重新选择身份</button>

  </view>

</template>



<script setup lang="ts">

import { selectRole } from '@/api/user'



async function toStudent() {

  await selectRole('student')

  uni.reLaunch({ url: '/pages/index/index' })

}



function reselect() {

  uni.reLaunch({ url: '/pages/role-select/index' })

}

</script>



<style scoped>

.page { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px; }

.icon { font-size: 64px; margin-bottom: 16px; }

.title { font-size: 20px; font-weight: 600; margin-bottom: 8px; }

.desc { font-size: 14px; color: #999; margin-bottom: 32px; }

.btn { width: 100%; background: #0096D6; color: #fff; border-radius: 8px; }

.outline { width: 100%; margin-top: 12px; background: #fff; color: #0096D6; border: 1px solid #0096D6; border-radius: 8px; }

</style>

