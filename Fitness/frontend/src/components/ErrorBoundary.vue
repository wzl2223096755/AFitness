<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-container">
      <div class="error-icon">
        <el-icon :size="60" color="#ef4444"><WarningFilled /></el-icon>
      </div>
      <h2 class="error-title">出错了</h2>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="retry" class="retry-button" :loading="isRetrying">
          <el-icon v-if="!isRetrying"><Refresh /></el-icon>
          {{ isRetrying ? '重试中...' : '重试' }}
        </el-button>
        <el-button @click="goHome" class="home-button">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button v-if="!errorReported" @click="reportError" class="report-button" type="warning">
          <el-icon><Warning /></el-icon>
          报告问题
        </el-button>
        <span v-else class="reported-text">
          <el-icon><CircleCheck /></el-icon>
          已报告
        </span>
      </div>
      <div v-if="showDetails && errorDetails" class="error-details">
        <el-collapse>
          <el-collapse-item title="技术详情" name="1">
            <pre class="error-stack">{{ errorDetails }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
      <div v-if="retryCount > 0" class="retry-info">
        <span>已重试 {{ retryCount }} 次</span>
      </div>
    </div>
  </div>
  <slot v-else></slot>
</template>

<script setup>
import { ref, onErrorCaptured, defineProps, defineEmits, computed } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled, Refresh, House, Warning, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { captureError } from '@/utils/errorMonitoring.js'

const props = defineProps({
  showDetails: {
    type: Boolean,
    default: false
  },
  maxRetries: {
    type: Number,
    default: 3
  },
  // 自定义错误消息
  customErrorMessage: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['error', 'retry', 'report'])
const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('页面加载失败，请稍后重试')
const errorDetails = ref('')
const errorReported = ref(false)
const isRetrying = ref(false)
const retryCount = ref(0)
const lastError = ref(null)
const errorInfo = ref(null)

onErrorCaptured((err, instance, info) => {
  console.error('Error captured by ErrorBoundary:', err)
  hasError.value = true
  errorMessage.value = props.customErrorMessage || err.message || '未知错误'
  errorDetails.value = err.stack || ''
  lastError.value = err
  errorInfo.value = {
    componentName: instance?.$options?.name || 'Unknown',
    info,
    timestamp: new Date().toISOString(),
    url: window.location.href,
    userAgent: navigator.userAgent
  }
  emit('error', { error: err, instance, info })
  return false // 阻止错误继续传播
})

const retry = async () => {
  if (retryCount.value >= props.maxRetries) {
    ElMessage.warning(`已达到最大重试次数 (${props.maxRetries} 次)，请稍后再试或联系管理员`)
    return
  }
  
  isRetrying.value = true
  retryCount.value++
  
  try {
    // 等待一小段时间再重试
    await new Promise(resolve => setTimeout(resolve, 500))
    
    hasError.value = false
    errorMessage.value = ''
    errorDetails.value = ''
    errorReported.value = false
    lastError.value = null
    errorInfo.value = null
    
    emit('retry', { retryCount: retryCount.value })
    
    // 强制刷新当前页面
    window.location.reload()
  } finally {
    isRetrying.value = false
  }
}

const goHome = () => {
  hasError.value = false
  retryCount.value = 0
  router.push('/dashboard')
}

/**
 * 报告错误到后端或错误监控服务
 */
const reportError = async () => {
  if (errorReported.value) {
    return
  }
  
  try {
    // 构建错误报告数据
    const errorReport = {
      message: errorMessage.value,
      stack: errorDetails.value,
      componentName: errorInfo.value?.componentName,
      info: errorInfo.value?.info,
      timestamp: errorInfo.value?.timestamp || new Date().toISOString(),
      url: errorInfo.value?.url || window.location.href,
      userAgent: errorInfo.value?.userAgent || navigator.userAgent,
      retryCount: retryCount.value
    }
    
    // 使用错误监控服务上报
    if (lastError.value) {
      captureError(lastError.value, {
        ...errorReport,
        reported: true
      })
    }
    
    // 存储到本地错误日志
    storeErrorReport(errorReport)
    
    errorReported.value = true
    emit('report', errorReport)
    
    ElMessage.success('问题已报告，感谢您的反馈！')
  } catch (e) {
    console.error('Failed to report error:', e)
    ElMessage.error('报告失败，请稍后重试')
  }
}

/**
 * 存储错误报告到本地
 */
const storeErrorReport = (report) => {
  try {
    const reports = JSON.parse(localStorage.getItem('errorReports') || '[]')
    reports.push(report)
    
    // 限制存储数量
    if (reports.length > 20) {
      reports.splice(0, reports.length - 20)
    }
    
    localStorage.setItem('errorReports', JSON.stringify(reports))
  } catch (e) {
    console.warn('Failed to store error report:', e)
  }
}

// 暴露reset方法供父组件调用
const reset = () => {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
  errorReported.value = false
  isRetrying.value = false
  retryCount.value = 0
  lastError.value = null
  errorInfo.value = null
}

defineExpose({ reset, hasError, retryCount })
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
  gap: 12px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
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

.retry-button:hover:not(:disabled) {
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

.report-button {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.report-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(230, 162, 60, 0.4);
}

.reported-text {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 14px;
  font-weight: 500;
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

.retry-info {
  margin-top: 16px;
  font-size: 12px;
  color: #94a3b8;
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
  .home-button,
  .report-button {
    width: 100%;
  }
}
</style>
