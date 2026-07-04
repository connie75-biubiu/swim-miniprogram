<template>

  <view class="page">

    <view class="row">

      <text class="label">教练手机号</text>

      <input v-model="phone" type="number" maxlength="11" placeholder="输入教练注册手机号" class="input" />

    </view>

    <text class="hint">请输入教练在泳记教练端注册的手机号</text>

    <view class="scope">

      <text class="scope-title">授权范围</text>

      <text>· 训练记录（含分段明细）</text>

      <text>· 比赛记录</text>

      <text>· 泳姿进步与速度趋势</text>

      <text>· 统计报表</text>

    </view>

    <button class="save" @tap="submit">确认授权</button>

  </view>

</template>



<script setup lang="ts">

import { ref } from 'vue'

import { addCoachAuth } from '@/api/user'



const phone = ref('')



async function submit() {

  if (!/^1\d{10}$/.test(phone.value)) {

    uni.showToast({ title: '请输入正确手机号', icon: 'none' })

    return

  }

  uni.showModal({

    title: '确认授权',

    content: `确认授权教练（${phone.value.slice(0, 3)}****${phone.value.slice(7)}）查看你的全部训练数据？`,

    success: async (r) => {

      if (r.confirm) {

        await addCoachAuth(phone.value)

        uni.showToast({ title: '授权成功', icon: 'success' })

        setTimeout(() => uni.navigateBack(), 500)

      }

    },

  })

}

</script>



<style scoped>

.row { display: flex; align-items: center; padding: 12px 16px; background: #fff; border-bottom: 1px solid #f0f0f0; }

.label { width: 100px; font-size: 15px; }

.input { flex: 1; text-align: right; font-size: 15px; }

.hint { font-size: 11px; color: #999; padding: 8px 16px; display: block; }

.scope { background: #F5FAFD; margin: 12px 16px; padding: 14px; border-radius: 8px; font-size: 13px; color: #666; line-height: 1.8; display: flex; flex-direction: column; }

.scope-title { font-weight: 600; color: #333; margin-bottom: 4px; }

.save { margin: 16px; background: #0096D6; color: #fff; border-radius: 8px; }

</style>

