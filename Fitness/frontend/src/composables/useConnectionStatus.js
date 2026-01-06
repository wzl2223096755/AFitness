/**
 * 连接状态监控 Composable
 * 定期检查后端连接状态，提供连接状态信息
 */
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

// 连接状态枚举
export const ConnectionStatus = {
  CONNECTED: 'connected',
  DISCONNECTED: 'disconnected',
  CHECKING: 'checking',
  UNKNOWN: 'unknown'
}

// 全局状态（单例模式，避免多个组件重复检查）
const globalStatus = ref(ConnectionStatus.UNKNOWN)
const globalLastChecked = ref(null)
const globalHealthInfo = ref(null)
let checkInterval = null
let subscriberCount = 0

/**
 * 检查后端连接状态
 */
const checkConnection = async () => {
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
 * 启动定期检查
 */
const startChecking = (intervalMs = 30000) => {
  if (checkInterval) {
    return // 已经在检查中
  }
  
  // 立即检查一次
  checkConnection()
  
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
}

/**
 * 连接状态监控 Composable
 * @param {Object} options 配置选项
 * @param {number} options.checkInterval 检查间隔（毫秒），默认 30000
 * @param {boolean} options.autoStart 是否自动开始检查，默认 true
 */
export function useConnectionStatus(options = {}) {
  const {
    checkInterval: interval = 30000,
    autoStart = true
  } = options
  
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
  
  return {
    // 状态
    status: globalStatus,
    lastChecked: globalLastChecked,
    healthInfo: globalHealthInfo,
    
    // 计算属性
    isConnected: () => globalStatus.value === ConnectionStatus.CONNECTED,
    isDisconnected: () => globalStatus.value === ConnectionStatus.DISCONNECTED,
    isChecking: () => globalStatus.value === ConnectionStatus.CHECKING,
    
    // 方法
    checkNow: checkConnection,
    startChecking,
    stopChecking,
    
    // 常量
    ConnectionStatus
  }
}

export default useConnectionStatus
