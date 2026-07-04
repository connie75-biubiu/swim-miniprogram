<template>

  <view class="page">

    <view class="header">

      <image class="avatar" src="/static/logo.png" mode="aspectFill" />

      <text class="name">{{ profile.nickname || '游泳学员' }}</text>

      <text class="phone">{{ profile.phone || '' }}</text>

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

      <view class="item" @tap="goCoach">

        <text>👨‍🏫 授权教练</text>

        <text class="count">{{ coachCount }} 位</text>

        <text class="arrow">›</text>

      </view>

    </view>

    <button class="logout" @tap="logout">退出登录</button>

  </view>

</template>



<script setup lang="ts">

import { reactive, ref } from 'vue'

import { onShow } from '@dcloudio/uni-app'

import { getProfile } from '@/api/user'

import { getCoachAuthList } from '@/api/user'



const profile = reactive({

  nickname: '',

  phone: '',

  gender: null as number | null,

  birthMonth: '',

})

const coachCount = ref(0)



onShow(async () => {

  if (!uni.getStorageSync('token')) {

    uni.reLaunch({ url: '/pages/login/index' })

    return

  }

  const p = await getProfile()

  profile.nickname = p.nickname || ''

  profile.phone = p.phone || ''

  profile.gender = p.gender

  profile.birthMonth = p.birthMonth || ''

  const list = await getCoachAuthList()

  coachCount.value = list.length

})



function formatBirth(m: string) {

  const [y, mo] = m.split('-')

  return `${y}年${Number(mo)}月`

}



function goEdit() {

  uni.navigateTo({ url: '/pages/profile-edit/index' })

}



function goCoach() {

  uni.navigateTo({ url: '/pages/coach-auth/index' })

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

.phone { font-size: 13px; color: #999; margin-top: 4px; display: block; }

.meta { display: flex; justify-content: center; gap: 16px; margin-top: 8px; font-size: 13px; color: #666; }

.badge { display: inline-block; margin-top: 8px; font-size: 11px; background: #E8F6FC; color: #0096D6; padding: 3px 10px; border-radius: 12px; }

.menu { background: #fff; margin: 12px 16px; border-radius: 12px; overflow: hidden; }

.item { display: flex; align-items: center; padding: 14px 16px; border-bottom: 1px solid #f5f5f5; font-size: 15px; }

.item:last-child { border: none; }

.count { margin-left: auto; font-size: 11px; color: #D48806; background: #FFF7E6; padding: 2px 8px; border-radius: 10px; margin-right: 8px; }

.arrow { color: #ccc; }

.logout { margin: 24px 16px; background: #fff; color: #0096D6; border: 1px solid #0096D6; border-radius: 8px; }

</style>

