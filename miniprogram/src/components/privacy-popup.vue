<template>
  <view v-if="visible" class="mask" @tap.stop>
    <view class="box">
      <view class="title">用户协议与隐私政策</view>
      <view class="text">欢迎使用泳记。我们将使用你的微信账号信息进行登录身份识别，并将登录状态缓存在本地设备上，用于提供游泳训练记录服务。</view>
      <view class="links">
        <text class="link" @tap="openPrivacyContract">查看完整《用户隐私保护指引》</text>
      </view>
      <view class="btns">
        <button class="cancel" @tap="emit('decline')">不同意</button>
        <!-- open-type=agreePrivacyAuthorization 是微信官方隐私授权按钮，
             用户点击后由微信系统记录授权，普通 @tap 无法完成官方授权 -->
        <button class="ok" open-type="agreePrivacyAuthorization" @agreeprivacyauthorization="emit('accept')">同意并继续</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>()
const emit = defineEmits<{ accept: []; decline: [] }>()

// 打开微信平台配置的隐私协议页（wx.openPrivacyContract 是官方提供的 API）
function openPrivacyContract() {
  if (uni.openPrivacyContract) {
    uni.openPrivacyContract({
      fail: () => uni.showToast({ title: '协议页打开失败', icon: 'none' })
    })
  }
}
</script>

<style scoped>
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.box { background: #fff; border-radius: 12px; padding: 24px; width: 280px; }
.title { font-size: 17px; font-weight: 600; text-align: center; margin-bottom: 12px; }
.text { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 12px; }
.links { margin-bottom: 20px; }
.link { font-size: 13px; color: #0096D6; text-decoration: underline; }
.btns { display: flex; gap: 12px; }
.cancel, .ok { flex: 1; font-size: 15px; border-radius: 8px; }
.cancel { background: #f5f5f5; color: #666; }
.ok { background: #0096D6; color: #fff; }
</style>
