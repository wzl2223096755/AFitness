/**
 * 连接状态监控 Composable
 * Connection status monitoring composable for shared use
 */
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import axios from 'axios'
import { ElNotification, ElMessage } from 'element-plus'

// 连接状态枚举
export const ConnectionStatus = {
  CONNECTED: 'connected',
  DISCONNECTED: 'disconnected',
  CHECKING: 'checking',
  UNKNOWN: 'unknown',
  OFFLINE: 'offline'
}

// 全局状态（单例模式）
const globalStatus = ref(ConnectionStatus.UNKNOWN)
const globalLastChecked = ref(null)
const globalHealthInfo = ref(null)
const globalIsOnline = ref(typeof navigator !== 'undefined' ? navigator.onLine : true)
const globalOfflineNotificationId = ref(null)
let checkInterval = null
let subscriberCount = 0

/**
 * 检查后端连接状态
 */
const checkConnection = async () => {
  if (typeof navigator !== 'undefined' && !navigator.onLine) {
    globalStatus.value = ConnectionStatus.OFFLINE
    globalHealthInfo.value = null
    globalLastChecked.value = new Date()
    return
  }
  
  globalStatus.value = ConnectionStatus.CHECKING
  
  try {
    const response = await axios.get('/api/v1/health', {
      timeout: 5000
    })
    
    if (response.data && response.data.code === 200) {
      globalStatus.value = ConnectionStatus.CONNECTED
      globalHealthInfo.value = response.data.data
    } else {
      globalStatus.value = ConnectionStatus.DISCONNECTED
      globalHealthInfo.value = null
    }
  } catch (error) {
    console.warn('后端连接检查失败:', error.message)
    globalStatus.value = ConnectionStatus.DISCONNECTED
    globalHealthInfo.value = null
  }
  
  globalLastChecked.value = new Date()
}

/**
 * 显示离线通知
 */
const showOfflineNotification = () => {
  if (globalOfflineNotificationId.value) return
  
  const notification = ElNotification({
    title: '网络连接已断开',
    message: '您当前处于离线状态，部分功能可能无法使用。请检查网络连接。',
    type: 'warning',
    duration: 0,
    position: 'top-right',
    showClose: true,
    customClass: 'offline-notification',
    onClose: () => {
      globalOfflineNotificationId.value = null
    }
  })
  
  globalOfflineNotificationId.value = notification
}

/**
 * 关闭离线通知
 */
const closeOfflineNotification = () => {
  if (globalOfflineNotificationId.value) {
    globalOfflineNotificationId.value.close()
    globalOfflineNotificationId.value = null
  }
}

/**
 * 显示重新连接通知
 */
const showReconnectedNotification = () => {
  ElMessage.success({
    message: '网络连接已恢复',
    duration: 3000,
    showClose: true
  })
}

/**
 * 处理在线状态变化
 */
const handleOnline = () => {
  globalIsOnline.value = true
  closeOfflineNotification()
  showReconnectedNotification()
  checkConnection()
}

/**
 * 处理离线状态变化
 */
const handleOffline = () => {
  globalIsOnline.value = false
  globalStatus.value = ConnectionStatus.OFFLINE
  showOfflineNotification()
}

/**
 * 启动定期检查
 */
const startChecking = (intervalMs = 30000) => {
  if (checkInterval) return
  
  if (typeof window !== 'undefined') {
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)
    
    if (!navigator.onLine) {
      handleOffline()
    } else {
      checkConnection()
    }
  }
  
  checkInterval = setInterval(checkConnection, intervalMs)
}

/**
 * 停止定期检查
 */
const stopChecking = () => {
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
  
  if (typeof window !== 'undefined') {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)
  }
  
  closeOfflineNotification()
}

/**
 * 连接状态监控 Composable
 * @param {Object} options 配置选项
 */
export function useConnectionStatus(options = {}) {
  const {
    checkInterval: interval = 30000,
    autoStart = true,
    showOfflineUI = true
  } = options
  
  const isConnected = computed(() => globalStatus.value === ConnectionStatus.CONNECTED)
  const isDisconnected = computed(() => globalStatus.value === ConnectionStatus.DISCONNECTED)
  const isChecking = computed(() => globalStatus.value === ConnectionStatus.CHECKING)
  const isOffline = computed(() => globalStatus.value === ConnectionStatus.OFFLINE || !globalIsOnline.value)
  const isOnline = computed(() => globalIsOnline.value)
  
  const statusText = computed(() => {
    switch (globalStatus.value) {
      case ConnectionStatus.CONNECTED: return '已连接'
      case ConnectionStatus.DISCONNECTED: return '连接断开'
      case ConnectionStatus.CHECKING: return '检查中...'
      case ConnectionStatus.OFFLINE: return '离线'
      default: return '未知'
    }
  })
  
  const statusColor = computed(() => {
    switch (globalStatus.value) {
      case ConnectionStatus.CONNECTED: return '#67c23a'
      case ConnectionStatus.DISCONNECTED: return '#f56c6c'
      case ConnectionStatus.CHECKING: return '#e6a23c'
      case ConnectionStatus.OFFLINE: return '#909399'
      default: return '#909399'
    }
  })
  
  onMounted(() => {
    subscriberCount++
    if (autoStart && subscriberCount === 1) {
      startChecking(interval)
    }
  })
  
  onUnmounted(() => {
    subscriberCount--
    if (subscriberCount === 0) {
      stopChecking()
    }
  })
  
  if (showOfflineUI) {
    watch(isOffline, (offline) => {
      if (offline) {
        showOfflineNotification()
      } else {
        closeOfflineNotification()
      }
    }, { immediate: true })
  }
  
  return {
    status: globalStatus,
    lastChecked: globalLastChecked,
    healthInfo: globalHealthInfo,
    isOnline: globalIsOnline,
    isConnected,
    isDisconnected,
    isChecking,
    isOffline,
    statusText,
    statusColor,
    checkNow: checkConnection,
    startChecking,
    stopChecking,
    showOfflineNotification,
    closeOfflineNotification,
    ConnectionStatus
  }
}

export default useConnectionStatus
