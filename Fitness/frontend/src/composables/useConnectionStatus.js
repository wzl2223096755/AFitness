/**
 * 连接状态监控 Composable
 * 定期检查后端连接状态，提供连接状态信息
 * 增强版：支持离线检测和离线提示UI
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
  OFFLINE: 'offline'  // 新增：浏览器离线状态
}

// 全局状态（单例模式，避免多个组件重复检查）
const globalStatus = ref(ConnectionStatus.UNKNOWN)
const globalLastChecked = ref(null)
const globalHealthInfo = ref(null)
const globalIsOnline = ref(navigator.onLine)
const globalOfflineNotificationId = ref(null)
let checkInterval = null
let subscriberCount = 0

/**
 * 检查后端连接状态
 */
const checkConnection = async () => {
  // 如果浏览器离线，直接返回离线状态
  if (!navigator.onLine) {
    globalStatus.value = ConnectionStatus.OFFLINE
    globalHealthInfo.value = null
    globalLastChecked.value = new Date()
    return
  }
  
  globalStatus.value = ConnectionStatus.CHECKING
  
  try {
    const response = await axios.get('/api/v1/health', {
      timeout: 5000 // 5秒超时
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
  // 避免重复显示
  if (globalOfflineNotificationId.value) {
    return
  }
  
  const notification = ElNotification({
    title: '网络连接已断开',
    message: '您当前处于离线状态，部分功能可能无法使用。请检查网络连接。',
    type: 'warning',
    duration: 0, // 不自动关闭
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
  // 立即检查后端连接
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
  if (checkInterval) {
    return // 已经在检查中
  }
  
  // 添加在线/离线事件监听
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  
  // 检查初始状态
  if (!navigator.onLine) {
    handleOffline()
  } else {
    // 立即检查一次
    checkConnection()
  }
  
  // 设置定期检查
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
  
  // 移除事件监听
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
  
  // 关闭离线通知
  closeOfflineNotification()
}

/**
 * 连接状态监控 Composable
 * @param {Object} options 配置选项
 * @param {number} options.checkInterval 检查间隔（毫秒），默认 30000
 * @param {boolean} options.autoStart 是否自动开始检查，默认 true
 * @param {boolean} options.showOfflineUI 是否显示离线提示UI，默认 true
 */
export function useConnectionStatus(options = {}) {
  const {
    checkInterval: interval = 30000,
    autoStart = true,
    showOfflineUI = true
  } = options
  
  // 计算属性
  const isConnected = computed(() => globalStatus.value === ConnectionStatus.CONNECTED)
  const isDisconnected = computed(() => globalStatus.value === ConnectionStatus.DISCONNECTED)
  const isChecking = computed(() => globalStatus.value === ConnectionStatus.CHECKING)
  const isOffline = computed(() => globalStatus.value === ConnectionStatus.OFFLINE || !globalIsOnline.value)
  const isOnline = computed(() => globalIsOnline.value)
  
  // 状态描述文本
  const statusText = computed(() => {
    switch (globalStatus.value) {
      case ConnectionStatus.CONNECTED:
        return '已连接'
      case ConnectionStatus.DISCONNECTED:
        return '连接断开'
      case ConnectionStatus.CHECKING:
        return '检查中...'
      case ConnectionStatus.OFFLINE:
        return '离线'
      default:
        return '未知'
    }
  })
  
  // 状态颜色
  const statusColor = computed(() => {
    switch (globalStatus.value) {
      case ConnectionStatus.CONNECTED:
        return '#67c23a' // 绿色
      case ConnectionStatus.DISCONNECTED:
        return '#f56c6c' // 红色
      case ConnectionStatus.CHECKING:
        return '#e6a23c' // 黄色
      case ConnectionStatus.OFFLINE:
        return '#909399' // 灰色
      default:
        return '#909399'
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
  
  // 监听离线状态变化，显示/隐藏离线UI
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
    // 状态
    status: globalStatus,
    lastChecked: globalLastChecked,
    healthInfo: globalHealthInfo,
    isOnline: globalIsOnline,
    
    // 计算属性
    isConnected,
    isDisconnected,
    isChecking,
    isOffline,
    statusText,
    statusColor,
    
    // 方法
    checkNow: checkConnection,
    startChecking,
    stopChecking,
    showOfflineNotification,
    closeOfflineNotification,
    
    // 常量
    ConnectionStatus
  }
}

export default useConnectionStatus
