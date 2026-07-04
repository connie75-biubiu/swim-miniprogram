<template>
  <view class="login">
    <privacy-popup :visible="showPrivacy" @accept="onAccept" @decline="onDecline" />
    <image class="logo" src="/static/logo.png" mode="aspectFill" />
    <text class="title">泳记</text>
    <text class="desc">记录每一次畅游</text>
    <text class="desc sub">追踪速度进步，突破自我</text>
    <button class="btn" @tap="doLogin">微信快捷登录</button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PrivacyPopup from '@/components/privacy-popup.vue'
import { wxLogin } from '@/api/auth'
import { afterLogin } from '@/utils/nav'

const showPrivacy = ref(!uni.getStorageSync('privacyAgreed'))

function onAccept() {
  uni.setStorageSync('privacyAgreed', true)
  showPrivacy.value = false
}

function onDecline() {
  uni.showToast({ title: '需同意协议才能使用', icon: 'none' })
}

async function doLogin() {
  if (!uni.getStorageSync('privacyAgreed')) {
    showPrivacy.value = true
    return
  }
  const { code } = await uni.login({ provider: 'weixin' })
  const res = await wxLogin(code)
  uni.setStorageSync('token', res.token)
  afterLogin(res)
}
</script>

<style scoped>
.login { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; padding: 40px; background: linear-gradient(180deg, #E8F6FC, #f5f9fc); }
.logo { width: 120px; height: 120px; border-radius: 50%; margin-bottom: 16px; }
.title { font-size: 24px; font-weight: 700; color: #0077B6; margin-bottom: 8px; }
.desc { font-size: 14px; color: #999; text-align: center; line-height: 1.6; }
.desc.sub { margin-bottom: 48px; }
.btn { width: 100%; background: #0096D6; color: #fff; border-radius: 8px; }
</style>
