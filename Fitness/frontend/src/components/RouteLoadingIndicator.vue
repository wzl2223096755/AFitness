<template>
  <Transition name="fade">
    <div v-if="isLoading" class="route-loading-indicator">
      <div class="loading-bar">
        <div class="loading-progress" :style="{ width: progress + '%' }"></div>
      </div>
      <div class="loading-content" v-if="showSpinner">
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-ring"></div>
          <div class="spinner-ring"></div>
        </div>
        <p class="loading-text">{{ loadingText }}</p>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  showSpinner: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '页面加载中...'
  }
})

const router = useRouter()
const isLoading = ref(false)
const progress = ref(0)
let progressInterval = null
let loadingTimeout = null

const startLoading = () => {
  isLoading.value = true
  progress.value = 0
  
  // 模拟进度条
  progressInterval = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.random() * 15
    }
  }, 100)
  
  // 超时保护：最多显示10秒
  loadingTimeout = setTimeout(() => {
    finishLoading()
  }, 10000)
}

const finishLoading = () => {
  if (progressInterval) {
    clearInterval(progressInterval)
    progressInterval = null
  }
  if (loadingTimeout) {
    clearTimeout(loadingTimeout)
    loadingTimeout = null
  }
  
  progress.value = 100
  
  setTimeout(() => {
    isLoading.value = false
    progress.value = 0
  }, 300)
}

onMounted(() => {
  router.beforeEach((to, from, next) => {
    if (to.path !== from.path) {
      startLoading()
    }
    next()
  })
  
  router.afterEach(() => {
    finishLoading()
  })
  
  router.onError(() => {
    finishLoading()
  })
})

onUnmounted(() => {
  if (progressInterval) {
    clearInterval(progressInterval)
  }
  if (loadingTimeout) {
    clearTimeout(loadingTimeout)
  }
})
</script>

<style scoped>
.route-loading-indicator {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
}

.loading-bar {
  height: 3px;
  background: rgba(99, 102, 241, 0.2);
  overflow: hidden;
}

.loading-progress {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #06b6d4);
  transition: width 0.2s ease;
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.5);
}

.loading-content {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: rgba(15, 15, 35, 0.95);
  backdrop-filter: blur(20px);
  padding: 32px 48px;
  border-radius: 16px;
  border: 1px solid rgba(99, 102, 241, 0.3);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-radius: 50%;
  animation: spin 1.5s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.spinner-ring:nth-child(1) {
  border-top-color: #6366f1;
  animation-delay: -0.45s;
}

.spinner-ring:nth-child(2) {
  border-top-color: #8b5cf6;
  animation-delay: -0.3s;
  width: 70%;
  height: 70%;
  top: 15%;
  left: 15%;
}

.spinner-ring:nth-child(3) {
  border-top-color: #06b6d4;
  animation-delay: -0.15s;
  width: 40%;
  height: 40%;
  top: 30%;
  left: 30%;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
  font-weight: 500;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
