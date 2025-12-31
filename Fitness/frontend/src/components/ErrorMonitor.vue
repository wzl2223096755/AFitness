<template>
  <div v-if="showErrorPanel" class="error-monitor-panel">
    <div class="error-monitor-header">
      <h3>错误监控面板</h3>
      <button @click="togglePanel" class="close-btn">×</button>
    </div>
    
    <div class="error-monitor-content">
      <div class="error-stats">
        <div class="stat-item">
          <span class="stat-label">总错误数:</span>
          <span class="stat-value">{{ errorLogs.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日错误:</span>
          <span class="stat-value">{{ todayErrors }}</span>
        </div>
      </div>
      
      <div class="error-controls">
        <button @click="refreshLogs" class="refresh-btn">刷新</button>
        <button @click="clearLogs" class="clear-btn">清空日志</button>
        <button @click="exportLogs" class="export-btn">导出日志</button>
      </div>
      
      <div class="error-list">
        <div 
          v-for="(error, index) in recentErrors" 
          :key="index"
          class="error-item"
          :class="getErrorTypeClass(error.type)"
        >
          <div class="error-header">
            <span class="error-type">{{ error.type }}</span>
            <span class="error-time">{{ formatTime(error.timestamp) }}</span>
          </div>
          <div class="error-message">{{ error.message }}</div>
          <div v-if="error.context" class="error-context">上下文: {{ error.context }}</div>
          <div v-if="error.url" class="error-url">URL: {{ error.url }}</div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 错误通知弹窗 -->
  <div v-if="showErrorNotification" class="error-notification" :class="notificationType">
    <div class="notification-content">
      <span class="notification-icon">{{ getNotificationIcon() }}</span>
      <span class="notification-message">{{ notificationMessage }}</span>
    </div>
    <button @click="dismissNotification" class="notification-close">×</button>
  </div>
  
  <!-- 调试模式切换按钮 -->
  <button 
    v-if="isDevelopment" 
    @click="togglePanel" 
    class="debug-toggle-btn"
    :class="{ active: showErrorPanel }"
  >
    🐛
  </button>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ErrorHandler } from '../utils/errorHandler.js'

// 响应式数据
const showErrorPanel = ref(false)
const errorLogs = ref([])
const showErrorNotification = ref(false)
const notificationMessage = ref('')
const notificationType = ref('error')
const isDevelopment = ref(import.meta.env.DEV)

// 计算属性
const todayErrors = computed(() => {
  const today = new Date().toDateString()
  return errorLogs.value.filter(error => 
    new Date(error.timestamp).toDateString() === today
  ).length
})

const recentErrors = computed(() => {
  return errorLogs.value.slice(-10).reverse() // 显示最近10条错误
})

// 方法
const togglePanel = () => {
  showErrorPanel.value = !showErrorPanel.value
}

const refreshLogs = () => {
  errorLogs.value = ErrorHandler.getErrorLogs()
}

const clearLogs = () => {
  ErrorHandler.clearErrorLogs()
  errorLogs.value = []
  showNotification('错误日志已清空', 'success')
}

const exportLogs = () => {
  const logs = ErrorHandler.getErrorLogs()
  const blob = new Blob([JSON.stringify(logs, null, 2)], { 
    type: 'application/json' 
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `error-logs-${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
  showNotification('错误日志已导出', 'success')
}

const getErrorTypeClass = (type) => {
  const classes = {
    'API_ERROR': 'api-error',
    'NETWORK_ERROR': 'network-error',
    'GENERIC_ERROR': 'generic-error'
  }
  return classes[type] || 'generic-error'
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const showNotification = (message, type = 'error') => {
  notificationMessage.value = message
  notificationType.value = type
  showErrorNotification.value = true
  
  setTimeout(() => {
    showErrorNotification.value = false
  }, 3000)
}

const dismissNotification = () => {
  showErrorNotification.value = false
}

const getNotificationIcon = () => {
  const icons = {
    'error': '❌',
    'success': '✅',
    'warning': '⚠️',
    'info': 'ℹ️'
  }
  return icons[notificationType.value] || icons.error
}

// 监听键盘快捷键
const handleKeyPress = (event) => {
  // Ctrl+Shift+E 切换错误面板
  if (event.ctrlKey && event.shiftKey && event.key === 'E') {
    togglePanel()
  }
}

// 生命周期
onMounted(() => {
  refreshLogs()
  document.addEventListener('keydown', handleKeyPress)
  
  // 监听错误事件
  window.addEventListener('error', (event) => {
    console.error('Global error caught:', event.error)
  })
  
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason)
  })
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyPress)
})
</script>

<style scoped>
.error-monitor-panel {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 500px;
  max-height: 80vh;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  z-index: 10000;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.error-monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.error-monitor-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.error-monitor-content {
  padding: 20px;
  max-height: calc(80vh - 60px);
  overflow-y: auto;
}

.error-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #495057;
}

.error-controls {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.error-controls button {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.refresh-btn {
  background: #007bff;
  color: white;
}

.refresh-btn:hover {
  background: #0056b3;
}

.clear-btn {
  background: #dc3545;
  color: white;
}

.clear-btn:hover {
  background: #c82333;
}

.export-btn {
  background: #28a745;
  color: white;
}

.export-btn:hover {
  background: #218838;
}

.error-list {
  max-height: 400px;
  overflow-y: auto;
}

.error-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  border-left: 4px solid;
  background: #f8f9fa;
}

.error-item.api-error {
  border-left-color: #dc3545;
}

.error-item.network-error {
  border-left-color: #fd7e14;
}

.error-item.generic-error {
  border-left-color: #6c757d;
}

.error-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.error-type {
  font-size: 12px;
  font-weight: 600;
  color: #495057;
  text-transform: uppercase;
}

.error-time {
  font-size: 11px;
  color: #6c757d;
}

.error-message {
  font-size: 14px;
  color: #212529;
  margin-bottom: 4px;
}

.error-context,
.error-url {
  font-size: 12px;
  color: #6c757d;
  word-break: break-all;
}

.error-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10001;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 300px;
  max-width: 500px;
  animation: slideIn 0.3s ease;
}

.error-notification.error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.error-notification.success {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.error-notification.warning {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.notification-close {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 0;
  margin-left: 12px;
}

.debug-toggle-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: #6c757d;
  color: white;
  font-size: 20px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
  z-index: 9999;
}

.debug-toggle-btn:hover,
.debug-toggle-btn.active {
  background: #007bff;
  transform: scale(1.1);
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .error-monitor-panel {
    width: 90vw;
    right: 5vw;
    left: 5vw;
  }
  
  .error-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .error-controls {
    flex-wrap: wrap;
  }
}
</style>