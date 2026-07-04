<template>

  <view class="page">

    <text class="hint">授权后，教练可查看你的训练、比赛和统计数据</text>

    <view class="section">已授权教练</view>

    <view v-for="c in list" :key="c.id" class="card">

      <view class="left">

        <text class="name">{{ c.coachName }}</text>

        <text class="phone">{{ c.coachPhone }}</text>

        <text class="date">授权于 {{ formatDate(c.createdAt) }}</text>

      </view>

      <text class="revoke" @tap="revoke(c.id)">取消授权</text>

    </view>

    <view v-if="!list.length" class="empty">暂无授权教练</view>

    <button class="add" @tap="goAdd">+ 添加教练授权</button>

  </view>

</template>



<script setup lang="ts">

import { ref } from 'vue'

import { onShow } from '@dcloudio/uni-app'

import { getCoachAuthList, revokeCoachAuth, type CoachAuthItem } from '@/api/user'



const list = ref<CoachAuthItem[]>([])



onShow(load)



async function load() {

  list.value = await getCoachAuthList()

}



function formatDate(s: string) {

  return s.slice(0, 10)

}



function goAdd() {

  uni.navigateTo({ url: '/pages/coach-add/index' })

}



function revoke(id: number) {

  uni.showModal({

    title: '取消授权',

    content: '确认取消该教练的数据查看权限？',

    success: async (r) => {

      if (r.confirm) {

        await revokeCoachAuth(id)

        await load()

      }

    },

  })

}

</script>



<style scoped>

.hint { font-size: 12px; color: #999; padding: 12px 16px; display: block; }

.section { font-size: 14px; color: #999; padding: 8px 16px; }

.card { background: #fff; margin: 0 16px 10px; padding: 14px 16px; border-radius: 12px; display: flex; align-items: center; }

.left { flex: 1; }

.name { font-size: 15px; font-weight: 600; display: block; }

.phone { font-size: 12px; color: #999; display: block; margin-top: 2px; }

.date { font-size: 11px; color: #bbb; display: block; margin-top: 4px; }

.revoke { color: #fa5151; font-size: 13px; border: 1px solid #fa5151; padding: 6px 12px; border-radius: 16px; }

.empty { text-align: center; color: #ccc; padding: 32px; }

.add { margin: 16px; background: #0096D6; color: #fff; border-radius: 8px; }

</style>

