<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-container">
      <div class="error-icon">
        <el-icon :size="60" color="#ef4444"><WarningFilled /></el-icon>
      </div>
      <h2 class="error-title">出错了</h2>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="retry" class="retry-button">
          <el-icon><Refresh /></el-icon>
          重试
        </el-button>
        <el-button @click="goHome" class="home-button">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
      </div>
      <div v-if="showDetails && errorDetails" class="error-details">
        <el-collapse>
          <el-collapse-item title="技术详情" name="1">
            <pre class="error-stack">{{ errorDetails }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
  <slot v-else></slot>
</template>

<script setup>
import { ref, onErrorCaptured, defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled, Refresh, House } from '@element-plus/icons-vue'

const props = defineProps({
  showDetails: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['error', 'retry'])
const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('页面加载失败，请稍后重试')
const errorDetails = ref('')

onErrorCaptured((err, instance, info) => {
  console.error('Error captured by ErrorBoundary:', err)
  hasError.value = true
  errorMessage.value = err.message || '未知错误'
  errorDetails.value = err.stack || ''
  emit('error', { error: err, instance, info })
  return false // 阻止错误继续传播
})

const retry = () => {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
  emit('retry')
  // 强制刷新当前页面
  window.location.reload()
}

const goHome = () => {
  hasError.value = false
  router.push('/dashboard')
}

// 暴露reset方法供父组件调用
const reset = () => {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
}

defineExpose({ reset })
</script>

<style scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  margin: 20px;
}

.error-container {
  text-align: center;
  max-width: 500px;
}

.error-icon {
  margin-bottom: 24px;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.error-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px;
  background: linear-gradient(135deg, #ef4444, #f87171);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.error-message {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 24px;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 24px;
}

.retry-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.home-button {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.home-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.error-details {
  text-align: left;
  margin-top: 20px;
}

.error-stack {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  font-size: 12px;
  color: #64748b;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .error-boundary {
    background: rgba(30, 41, 59, 0.95);
  }
  
  .error-title {
    background: linear-gradient(135deg, #f87171, #ef4444);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .error-message {
    color: #94a3b8;
  }
  
  .error-stack {
    background: #1e293b;
    color: #94a3b8;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .error-boundary {
    padding: 24px 16px;
    margin: 12px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-message {
    font-size: 14px;
  }
  
  .error-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .retry-button,
  .home-button {
    width: 100%;
  }
}
</style>
