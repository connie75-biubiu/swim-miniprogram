<template>

  <view class="page">

    <view class="row">

      <text class="label">昵称</text>

      <input v-model="form.nickname" class="input" />

    </view>

    <view class="row">

      <text class="label">出生年月</text>

      <picker mode="date" fields="month" :value="form.birthMonth + '-01'" @change="onBirth">

        <text>{{ form.birthMonth || '请选择' }}</text>

      </picker>

    </view>

    <view class="row">

      <text class="label">性别</text>

      <view class="radio">

        <text :class="gClass(1)" @tap="form.gender = 1">男</text>

        <text :class="gClass(2)" @tap="form.gender = 2">女</text>

      </view>

    </view>

    <button class="save" @tap="save">保存</button>

  </view>

</template>



<script setup lang="ts">

import { reactive } from 'vue'

import { onLoad } from '@dcloudio/uni-app'

import { getProfile, updateProfile } from '@/api/user'



const form = reactive({ nickname: '', birthMonth: '', gender: 1 })



onLoad(async () => {

  const p = await getProfile()

  form.nickname = p.nickname || ''

  form.birthMonth = p.birthMonth || ''

  form.gender = p.gender || 1

})



function onBirth(e: { detail: { value: string } }) {

  form.birthMonth = e.detail.value.slice(0, 7)

}



function gClass(n: number) {

  return form.gender === n ? 'r on' : 'r'

}



async function save() {

  await updateProfile({

    nickname: form.nickname,

    gender: form.gender,

    birthMonth: form.birthMonth,

  })

  uni.showToast({ title: '已保存', icon: 'success' })

  setTimeout(() => uni.navigateBack(), 500)

}

</script>



<style scoped>

.row { display: flex; align-items: center; padding: 12px 16px; background: #fff; border-bottom: 1px solid #f0f0f0; }

.label { width: 90px; font-size: 15px; }

.input { flex: 1; text-align: right; font-size: 15px; }

.radio { flex: 1; display: flex; gap: 12px; justify-content: flex-end; }

.r { padding: 6px 16px; border: 1px solid #ddd; border-radius: 20px; font-size: 13px; }

.r.on { background: #0096D6; color: #fff; border-color: #0096D6; }

.save { margin: 16px; background: #0096D6; color: #fff; border-radius: 8px; }

</style>
