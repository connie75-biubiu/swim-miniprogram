<template>

  <view class="page">

    <view class="header">

      <image class="avatar" src="/static/logo.png" mode="aspectFill" />

      <text class="name">{{ profile.nickname || '游泳学员' }}</text>

      <view class="meta">

        <text v-if="profile.gender">{{ profile.gender === 1 ? '♂ 男' : '♀ 女' }}</text>

        <text v-if="profile.birthMonth">🎂 {{ formatBirth(profile.birthMonth) }}</text>

      </view>

      <text class="badge">学员</text>

    </view>

    <view class="menu">

      <view class="item" @tap="goEdit">

        <text>✏️ 编辑资料</text><text class="arrow">›</text>

      </view>

    </view>

    <button class="logout" @tap="logout">退出登录</button>

  </view>

</template>



<script setup lang="ts">

import { reactive } from 'vue'

import { onShow } from '@dcloudio/uni-app'

import { getProfile } from '@/api/user'



const profile = reactive({

  nickname: '',

  gender: null as number | null,

  birthMonth: '',

})



onShow(async () => {

  if (!uni.getStorageSync('token')) {

    uni.reLaunch({ url: '/pages/login/index' })

    return

  }

  const p = await getProfile()

  profile.nickname = p.nickname || ''

  profile.gender = p.gender

  profile.birthMonth = p.birthMonth || ''

})



function formatBirth(m: string) {

  const [y, mo] = m.split('-')

  return `${y}年${Number(mo)}月`

}



function goEdit() {

  uni.navigateTo({ url: '/pages/profile-edit/index' })

}



function logout() {

  uni.removeStorageSync('token')

  uni.reLaunch({ url: '/pages/login/index' })

}

</script>



<style scoped>

.header { background: linear-gradient(180deg, #E8F6FC, #f5f9fc); padding: 32px 16px 24px; text-align: center; }

.avatar { width: 72px; height: 72px; border-radius: 50%; margin-bottom: 12px; }

.name { font-size: 18px; font-weight: 600; display: block; }

.meta { display: flex; justify-content: center; gap: 16px; margin-top: 8px; font-size: 13px; color: #666; }

.badge { display: inline-block; margin-top: 8px; font-size: 11px; background: #E8F6FC; color: #0096D6; padding: 3px 10px; border-radius: 12px; }

.menu { background: #fff; margin: 12px 16px; border-radius: 12px; overflow: hidden; }

.item { display: flex; align-items: center; padding: 14px 16px; border-bottom: 1px solid #f5f5f5; font-size: 15px; }

.item:last-child { border: none; }

.arrow { color: #ccc; }

.logout { margin: 24px 16px; background: #fff; color: #0096D6; border: 1px solid #0096D6; border-radius: 8px; }

</style>
