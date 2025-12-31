/**
 * 离线同步组合式函数
 * 提供响应式的同步状态和操作方法
 * Requirements: 4.3, 4.4, 4.5
 */

import { ref, onMounted, onUnmounted, computed } from 'vue'
import {
  initSyncManager,
  destroySyncManager,
  addSyncListener,
  getSyncState,
  isOnline,
  triggerSync,
  queueTrainingSync,
  queueNutritionSync,
  queueRecoverySync,
  SyncStatus
} from '@/utils/syncManager'
import { getSyncQueueStats } from '@/utils/offlineStorage'

/**
 * 离线同步组合式函数
 * @returns {Object}
 */
export function useOfflineSync() {
  // 响应式状态
  const online = ref(navigator.onLine)
  const syncStatus = ref(SyncStatus.IDLE)
  const syncInProgress = ref(false)
  const lastSyncTime = ref(null)
  const pendingCount = ref(0)
  const syncMessage = ref('')
  const syncProgress = ref({ current: 0, total: 0 })
  
  // 计算属性
  const isOffline = computed(() => !online.value)
  const hasPendingSync = computed(() => pendingCount.value > 0)
  const statusText = computed(() => {
    if (!online.value) return '离线'
    if (syncInProgress.value) return '同步中...'
    if (pendingCount.value > 0) return `${pendingCount.value} 条待同步`
    return '已同步'
  })
  
  // 监听器清理函数
  let removeListener = null
  
  /**
   * 处理同步事件
   */
  function handleSyncEvent(event) {
    const { type, state, message, current, total, count } = event
    
    // 更新状态
    if (state) {
      online.value = state.isOnline
      syncStatus.value = state.status
      syncInProgress.value = state.syncInProgress
      lastSyncTime.value = state.lastSyncTime
    }
    
    // 处理不同事件类型
    switch (type) {
      case 'online':
        syncMessage.value = '网络已恢复'
        break
      case 'offline':
        syncMessage.value = message || '网络已断开'
        break
      case 'sync_available':
        pendingCount.value = count || 0
        syncMessage.value = message
        break
      case 'sync_start':
        syncMessage.value = '开始同步...'
        break
      case 'sync_progress':
        syncProgress.value = { current, total }
        syncMessage.value = `同步中 ${current}/${total}`
        break
      case 'sync_complete':
        syncMessage.value = message
        updatePendingCount()
        break
      case 'sync_error':
        syncMessage.value = `同步错误: ${event.error}`
        break
    }
  }
  
  /**
   * 更新待同步数量
   */
  async function updatePendingCount() {
    try {
      const stats = await getSyncQueueStats()
      pendingCount.value = stats.pending
    } catch (error) {
      console.error('获取同步队列统计失败:', error)
    }
  }
  
  /**
   * 手动触发同步
   */
  async function sync() {
    if (!online.value) {
      syncMessage.value = '网络离线，无法同步'
      return { success: false, reason: 'offline' }
    }
    
    return await triggerSync()
  }
  
  /**
   * 添加训练数据到同步队列
   */
  async function addTrainingToSync(action, data) {
    const id = await queueTrainingSync(action, data)
    await updatePendingCount()
    return id
  }
  
  /**
   * 添加营养数据到同步队列
   */
  async function addNutritionToSync(action, data) {
    const id = await queueNutritionSync(action, data)
    await updatePendingCount()
    return id
  }
  
  /**
   * 添加恢复数据到同步队列
   */
  async function addRecoveryToSync(action, data) {
    const id = await queueRecoverySync(action, data)
    await updatePendingCount()
    return id
  }
  
  // 生命周期
  onMounted(() => {
    // 初始化同步管理器
    initSyncManager()
    
    // 添加监听器
    removeListener = addSyncListener(handleSyncEvent)
    
    // 初始化状态
    const state = getSyncState()
    online.value = state.isOnline
    syncStatus.value = state.status
    lastSyncTime.value = state.lastSyncTime
    
    // 获取待同步数量
    updatePendingCount()
  })
  
  onUnmounted(() => {
    if (removeListener) {
      removeListener()
    }
  })
  
  return {
    // 状态
    online,
    isOffline,
    syncStatus,
    syncInProgress,
    lastSyncTime,
    pendingCount,
    hasPendingSync,
    syncMessage,
    syncProgress,
    statusText,
    
    // 方法
    sync,
    addTrainingToSync,
    addNutritionToSync,
    addRecoveryToSync,
    updatePendingCount
  }
}
