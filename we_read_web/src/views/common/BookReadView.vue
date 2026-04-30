<template>
  <div class="read-wrap">
    <div class="read-header">
      <div class="title">{{ bookTitle }}</div>
      <div class="time">累计阅读：{{ displayTime(totalSeconds + sessionSeconds) }}</div>
    </div>
    <div class="read-body">
      <div class="panel">
        <div class="big-time">{{ displayTime(sessionSeconds) }}</div>
        <div class="actions">
          <el-button type="primary" @click="toggleTimer">{{ running ? '暂停' : '开始' }}</el-button>
          <el-button @click="back">返回</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { bookApi } from '@/api/book'
import { isSuccessResponse } from '@/utils/api-helper'

const route = useRoute()
const router = useRouter()

const bookId = Number(route.params.id)
const bookTitle = ref('')
const totalSeconds = ref(0)
const sessionSeconds = ref(0)
const running = ref(true)
let timer = null

const displayTime = (sec) => {
  const s = Math.floor(sec)
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const r = s % 60
  const pad = (n) => (n < 10 ? '0' + n : '' + n)
  return `${pad(h)}:${pad(m)}:${pad(r)}`
}

const startTimer = () => {
  if (timer) return
  running.value = true
  timer = setInterval(() => {
    sessionSeconds.value += 1
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  running.value = false
}

const toggleTimer = () => {
  if (running.value) {
    stopTimer()
  } else {
    startTimer()
  }
}

const persistAndBack = async () => {
  try {
    if (sessionSeconds.value > 0) {
      await userApi.addReadingTime({
        bookId,
        deltaSeconds: sessionSeconds.value
      })
      ElMessage.success('阅读时长已保存')
    }
  } catch (e) {}
  router.push(`/book/${bookId}`)
}

const back = () => {
  stopTimer()
  persistAndBack()
}

onMounted(async () => {
  try {
    const info = await bookApi.getBookById(bookId)
    if (isSuccessResponse(info)) {
      bookTitle.value = info.data?.title || ''
    }
  } catch (e) {}
  try {
    const res = await userApi.getReadingTime(bookId)
    if (res?.meta?.code === 200) {
      totalSeconds.value = Number(res.data?.seconds || 0)
    }
  } catch (e) {}
  startTimer()
})

onBeforeUnmount(() => {
  stopTimer()
})
</script>

<style scoped>
.read-wrap{max-width:900px;margin:0 auto;padding:24px}
.read-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}
.title{font-size:18px;font-weight:600}
.time{color:#606266}
.read-body{display:flex;justify-content:center;align-items:center;min-height:360px;background:var(--el-bg-color);border-radius:8px}
.panel{text-align:center}
.big-time{font-size:48px;font-weight:700;margin-bottom:16px}
.actions{display:flex;gap:12px;justify-content:center}
</style>
