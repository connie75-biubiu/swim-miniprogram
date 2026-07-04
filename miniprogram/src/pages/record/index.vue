<template>
  <view class="page">
    <view class="form-row">
      <text class="label">训练日期</text>
      <picker mode="date" :value="form.date" @change="onDateChange">
        <text>{{ form.date }}</text>
      </picker>
    </view>

    <view class="form-row">
      <text class="label">主泳姿</text>
      <picker :range="strokes" :value="strokeIdx" @change="onStroke">
        <text>{{ form.stroke }}</text>
      </picker>
    </view>

    <view class="form-row">
      <text class="label">泳池</text>
      <view class="radio">
        <text :class="poolClass(1)" @tap="setPool(1)">25米</text>
        <text :class="poolClass(2)" @tap="setPool(2)">50米</text>
      </view>
    </view>

    <view class="form-row">
      <text class="label">数据来源</text>
      <view class="radio">
        <text :class="sourceClass(SOURCE_TRAIN)" @tap="setSource(SOURCE_TRAIN)">训练</text>
        <text :class="sourceClass(SOURCE_COMPETE)" @tap="setSource(SOURCE_COMPETE)">比赛</text>
      </view>
    </view>

    <split-form v-model="form.splits" />

    <view class="summary">
      <view><text class="n">{{ totalDist }}m</text><text class="l">总距离</text></view>
      <view><text class="n">{{ formatDuration(totalDur) }}</text><text class="l">总时长</text></view>
      <view><text class="n">{{ formatPace(avgPace) }}</text><text class="l">平均配速</text></view>
    </view>

    <view class="form-row">
      <text class="label">{{ noteLabel }}</text>
      <input v-model="form.note" :placeholder="notePlaceholder" />
    </view>

    <button class="save" @tap="save">保存记录</button>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import SplitForm from '@/components/split-form.vue'
import {
  createWorkout,
  updateWorkout,
  getWorkout,
  SOURCE_TRAIN,
  SOURCE_COMPETE,
  type WorkoutForm,
} from '@/api/workout'
import { STROKES } from '@/utils/constants'
import { calcPace, formatDuration, formatPace } from '@/utils/pace'

const strokes = [...STROKES, '混合']
const editId = ref<number | null>(null)
const form = reactive<WorkoutForm>({
  date: new Date().toISOString().slice(0, 10),
  stroke: '自由泳',
  poolType: 1,
  sourceType: SOURCE_TRAIN,
  note: '',
  splits: [{ seq: 1, stroke: '自由泳', distance: 200, duration: 240 }],
})

const strokeIdx = computed(() => strokes.indexOf(form.stroke))
const totalDist = computed(() => form.splits.reduce((s, x) => s + x.distance, 0))
const totalDur = computed(() => form.splits.reduce((s, x) => s + x.duration, 0))
const avgPace = computed(() => calcPace(totalDist.value, totalDur.value))
const isCompete = computed(() => form.sourceType === SOURCE_COMPETE)
const noteLabel = computed(() => (isCompete.value ? '比赛名称' : '备注'))
const notePlaceholder = computed(() => (isCompete.value ? '如：市运会、春季杯' : '选填'))

onLoad(async (q) => {
  if (q?.id) {
    editId.value = Number(q.id)
    const res = await getWorkout(editId.value)
    form.date = res.workout.date
    form.stroke = res.workout.stroke
    form.poolType = res.workout.poolType
    form.sourceType = res.workout.sourceType || SOURCE_TRAIN
    form.note = res.workout.note || ''
    form.splits = res.splits.map((s) => ({
      seq: s.seq,
      stroke: s.stroke,
      distance: s.distance,
      duration: s.duration,
    }))
    uni.setNavigationBarTitle({
      title: form.sourceType === SOURCE_COMPETE ? '编辑比赛' : '编辑训练',
    })
  }
})

function onDateChange(e: { detail: { value: string } }) {
  form.date = e.detail.value
}

function setPool(n: number) {
  form.poolType = n
}

function setSource(n: number) {
  form.sourceType = n
}

function poolClass(n: number) {
  return form.poolType === n ? 'r on' : 'r'
}

function sourceClass(n: number) {
  return form.sourceType === n ? 'r on' : 'r'
}

function onStroke(e: { detail: { value: string } }) {
  const s = strokes[Number(e.detail.value)]
  form.stroke = s
  // 单泳姿训练为主：主泳姿改变时，把所有分段的泳姿同步过来，
  // 避免主泳姿与分段泳姿脱节（混合泳时可在分段里单独再调）
  form.splits = form.splits.map((x) => ({ ...x, stroke: s }))
}

async function save() {
  if (isCompete.value && !form.note?.trim()) {
    uni.showToast({ title: '请填写比赛名称', icon: 'none' })
    return
  }
  if (editId.value) await updateWorkout(editId.value, form)
  else await createWorkout(form)
  uni.showToast({ title: '已保存', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 500)
}
</script>

<style scoped>
.form-row { display: flex; align-items: center; padding: 12px 16px; background: #fff; border-bottom: 1px solid #f0f0f0; }
.label { width: 90px; font-size: 15px; }
.radio { flex: 1; display: flex; gap: 12px; justify-content: flex-end; }
.r { padding: 6px 16px; border: 1px solid #ddd; border-radius: 20px; font-size: 13px; }
.r.on { background: #0096D6; color: #fff; border-color: #0096D6; }
.summary { display: flex; justify-content: space-around; background: #E8F6FC; margin: 12px 16px; padding: 12px; border-radius: 8px; text-align: center; }
.n { display: block; font-size: 16px; font-weight: 700; color: #0096D6; }
.l { font-size: 12px; color: #999; }
.save { margin: 16px; background: #0096D6; color: #fff; border-radius: 8px; }
</style>
