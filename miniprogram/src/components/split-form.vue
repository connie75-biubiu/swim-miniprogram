<template>
  <view class="split-list">
    <view v-for="(item, idx) in modelValue" :key="idx" class="split-item">
      <view class="split-head">
        <text>第 {{ idx + 1 }} 段</text>
        <text class="del" @tap="remove(idx)">删除</text>
      </view>
      <view class="fields">
        <picker :range="strokes" :value="strokeIndex(item.stroke)" @change="onStroke($event, idx)">
          <view class="field"><text class="label">泳姿</text><text>{{ item.stroke }}</text></view>
        </picker>
        <view class="field">
          <text class="label">距离(m)</text>
          <input type="number" :value="String(item.distance)" @input="onNum($event, idx, 'distance')" />
        </view>
        <view class="field">
          <text class="label">时长(秒)</text>
          <input type="number" :value="String(item.duration)" @input="onNum($event, idx, 'duration')" />
        </view>
      </view>
    </view>
    <view class="add" @tap="add">+ 添加分段</view>
  </view>
</template>

<script setup lang="ts">
import type { SplitForm } from '@/api/workout'
import { STROKES } from '@/utils/constants'

const props = defineProps<{ modelValue: SplitForm[] }>()
const emit = defineEmits<{ 'update:modelValue': [SplitForm[]] }>()
const strokes = [...STROKES, '混合']

function strokeIndex(s: string) {
  return Math.max(0, strokes.indexOf(s))
}

function update(list: SplitForm[]) {
  emit('update:modelValue', list.map((s, i) => ({ ...s, seq: i + 1 })))
}

function onStroke(e: { detail: { value: string } }, idx: number) {
  const list = [...props.modelValue]
  list[idx].stroke = strokes[Number(e.detail.value)]
  update(list)
}

function onNum(e: { detail: { value: string } }, idx: number, key: 'distance' | 'duration') {
  const list = [...props.modelValue]
  list[idx][key] = Number(e.detail.value) || 0
  update(list)
}

function add() {
  update([...props.modelValue, { seq: props.modelValue.length + 1, stroke: '自由泳', distance: 100, duration: 120 }])
}

function remove(idx: number) {
  if (props.modelValue.length <= 1) {
    uni.showToast({ title: '至少保留1段', icon: 'none' })
    return
  }
  const list = [...props.modelValue]
  list.splice(idx, 1)
  update(list)
}
</script>

<style scoped>
.split-item { background: #f9f9f9; border-radius: 8px; margin: 8px 16px; padding: 12px; }
.split-head { display: flex; justify-content: space-between; font-size: 13px; color: #999; margin-bottom: 8px; }
.del { color: #fa5151; }
.fields { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 8px; }
.field { font-size: 13px; }
.label { display: block; font-size: 11px; color: #999; margin-bottom: 4px; }
.add { text-align: center; color: #0096D6; padding: 12px; font-size: 14px; }
</style>
