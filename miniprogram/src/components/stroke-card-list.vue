<template>
  <view class="list">
    <view
      v-for="(stroke, idx) in order"
      :key="stroke"
      class="card"
      :class="{ dragging: dragIdx === idx }"
      :style="dragStyle(idx)"
      @longpress="onLongPress(idx)"
      @touchmove.stop.prevent="onTouchMove"
      @touchend="onTouchEnd"
      @tap="emit('tap', stroke)"
    >
      <view class="card-head">
        <text class="icon">{{ STROKE_ICONS[stroke] }}</text>
        <text class="name">{{ stroke }}</text>
        <text v-if="dragIdx === idx" class="hint">拖动排序</text>
      </view>
      <view class="dist-grid">
        <view v-for="d in DISTANCES" :key="d" class="dist-item">
          <text class="dist-label">{{ d }}米</text>
          <text class="dist-pace">{{ paceText(stroke, d) }}</text>
          <text :class="changeClass(stroke, d)">{{ changeText(stroke, d) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DISTANCES, STROKE_ICONS } from '@/utils/constants'
import { formatPace } from '@/utils/pace'
import { updateStrokeOrder } from '@/api/auth'

const props = defineProps<{
  order: string[]
  paceMap: Record<string, Record<number, number | null>>
  changeMap: Record<string, Record<number, number | null>>
}>()
const emit = defineEmits<{ 'update:order': [string[]]; tap: [string] }>()

const dragIdx = ref(-1)
const offsetY = ref(0)
const startY = ref(0)
const cardH = 160

function dragStyle(idx: number) {
  if (dragIdx.value !== idx) return {}
  return { transform: `translateY(${offsetY.value}px)`, opacity: 0.85 }
}

function paceText(stroke: string, d: number) {
  return formatPace(props.paceMap[stroke]?.[d] ?? null)
}

function changeText(stroke: string, d: number) {
  const c = props.changeMap[stroke]?.[d]
  if (c == null) return ''
  const sec = Math.round(Math.abs(c))
  if (c < 0) return `↑ 快${sec}秒`
  if (c > 0) return `↓ 慢${sec}秒`
  return '持平'
}

function changeClass(stroke: string, d: number) {
  const c = props.changeMap[stroke]?.[d]
  if (c == null) return 'change'
  return c <= 0 ? 'change up' : 'change down'
}

function onLongPress(idx: number) {
  dragIdx.value = idx
  uni.vibrateShort({ type: 'light' })
}

function onTouchMove(e: TouchEvent) {
  if (dragIdx.value < 0) return
  const y = e.touches[0].clientY
  if (!startY.value) startY.value = y
  offsetY.value = y - startY.value
  const shift = Math.round(offsetY.value / cardH)
  const target = dragIdx.value + shift
  if (target >= 0 && target < props.order.length && target !== dragIdx.value) {
    const list = [...props.order]
    const [item] = list.splice(dragIdx.value, 1)
    list.splice(target, 0, item)
    dragIdx.value = target
    startY.value = y
    offsetY.value = 0
    emit('update:order', list)
  }
}

async function onTouchEnd() {
  if (dragIdx.value >= 0) {
    uni.setStorageSync('stroke_order', props.order)
    try {
      await updateStrokeOrder(props.order)
    } catch { /* 离线时本地已存 */ }
  }
  dragIdx.value = -1
  offsetY.value = 0
  startY.value = 0
}
</script>

<style scoped>
.card { background: #fff; border-radius: 12px; margin: 0 16px 10px; padding: 14px; box-shadow: 0 1px 4px rgba(0,100,150,.06); }
.card.dragging { box-shadow: 0 8px 20px rgba(0,150,214,.2); }
.card-head { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.icon { font-size: 24px; }
.name { font-size: 16px; font-weight: 600; flex: 1; }
.hint { font-size: 11px; color: #0096D6; }
.dist-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.dist-item { background: #F5FAFD; border-radius: 6px; padding: 6px 8px; }
.dist-label { font-size: 10px; color: #999; display: block; }
.dist-pace { font-size: 12px; color: #0096D6; font-weight: 600; display: block; }
.change { font-size: 10px; display: block; margin-top: 2px; }
.change.up { color: #52c41a; }
.change.down { color: #fa8c16; }
</style>
