<template>
  <view v-if="visible" class="mask" @tap.stop>
    <view class="box">
      <view class="title">用户协议与隐私政策</view>
      <view class="text">欢迎使用泳记。我们将收集微信账号信息和手机号用于训练记录服务。您可主动授权教练查看训练数据，授权关系可随时取消。</view>
      <view class="btns">
        <button class="cancel" @tap="emit('decline')">不同意</button>
        <button class="ok" @tap="emit('accept')">同意并继续</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>()
const emit = defineEmits<{ accept: []; decline: [] }>()
</script>

<style scoped>
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.box { background: #fff; border-radius: 12px; padding: 24px; width: 280px; }
.title { font-size: 17px; font-weight: 600; text-align: center; margin-bottom: 12px; }
.text { font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 20px; }
.btns { display: flex; gap: 12px; }
.cancel, .ok { flex: 1; font-size: 15px; border-radius: 8px; }
.cancel { background: #f5f5f5; color: #666; }
.ok { background: #0096D6; color: #fff; }
</style>
