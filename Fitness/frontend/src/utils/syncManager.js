/**
 * 离线数据同步管理器
 * 实现同步队列管理、网络状态监听和自动同步逻辑
 * Requirements: 4.3, 4.4, 4.5
 */

import {
  getPendingSyncData,
  markAsSynced,
  updateSyncItem,
  clearSyncedData,
  getSyncQueueStats,
  addToSyncQueue
} from './offlineStorage'

// 同步状态
const SyncStatus = {
  IDLE: 'idle',
  SYNCING: 'syncing',
  ERROR: 'error',
  OFFLINE: 'offline'
}

// 同步管理器状态
let syncState = {
  status: SyncStatus.IDLE,
  isOnline: navigator.onLine,
  lastSyncTime: null,
  syncInProgress: false,
  listeners: new Set()
}

// 同步配置
const SYNC_CONFIG = {
  maxRetries: 3,
  retryDelay: 5000, // 5秒
  autoSyncInterval: 30000, // 30秒自动同步检查
  batchSize: 10 // 每批同步数量
}

// API端点映射
const API_ENDPOINTS = {
  training: {
    create: '/api/v1/training/records',
    update: (id) => `/api/v1/training/records/${id}`,
    delete: (id) => `/api/v1/training/records/${id}`
  },
  nutrition: {
    create: '/api/v1/nutrition/records',
    update: (id) => `/api/v1/nutrition/records/${id}`,
    delete: (id) => `/api/v1/nutrition/records/${id}`
  },
  recovery: {
    create: '/api/v1/recovery/records',
    update: (id) => `/api/v1/recovery/records/${id}`,
    delete: (id) => `/api/v1/recovery/records/${id}`
  }
}

/**
 * 初始化同步管理器
 */
export function initSyncManager() {
  // 监听网络状态变化
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  
  // 初始化网络状态
  syncState.isOnline = navigator.onLine
  if (!syncState.isOnline) {
    syncState.status = SyncStatus.OFFLINE
  }
  
  // 启动自动同步检查
  startAutoSync()
  
  console.log('同步管理器已初始化')
}

/**
 * 销毁同步管理器
 */
export function destroySyncManager() {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
  stopAutoSync()
  syncState.listeners.clear()
  console.log('同步管理器已销毁')
}


// 自动同步定时器
let autoSyncTimer = null

/**
 * 启动自动同步
 */
function startAutoSync() {
  if (autoSyncTimer) return
  
  autoSyncTimer = setInterval(async () => {
    if (syncState.isOnline && !syncState.syncInProgress) {
      const stats = await getSyncQueueStats()
      if (stats.pending > 0) {
        console.log(`发现 ${stats.pending} 条待同步数据，开始自动同步`)
        await syncPendingData()
      }
    }
  }, SYNC_CONFIG.autoSyncInterval)
  
  console.log('自动同步已启动')
}

/**
 * 停止自动同步
 */
function stopAutoSync() {
  if (autoSyncTimer) {
    clearInterval(autoSyncTimer)
    autoSyncTimer = null
    console.log('自动同步已停止')
  }
}

/**
 * 处理网络恢复
 */
async function handleOnline() {
  console.log('网络已恢复')
  syncState.isOnline = true
  syncState.status = SyncStatus.IDLE
  notifyListeners({ type: 'online' })
  
  // 网络恢复后自动同步
  const stats = await getSyncQueueStats()
  if (stats.pending > 0) {
    notifyListeners({ 
      type: 'sync_available', 
      message: `有 ${stats.pending} 条数据等待同步`,
      count: stats.pending
    })
    await syncPendingData()
  }
}

/**
 * 处理网络断开
 */
function handleOffline() {
  console.log('网络已断开')
  syncState.isOnline = false
  syncState.status = SyncStatus.OFFLINE
  notifyListeners({ type: 'offline', message: '网络已断开，数据将在恢复后同步' })
}

/**
 * 添加状态变化监听器
 * @param {Function} listener - 监听函数
 * @returns {Function} 取消监听的函数
 */
export function addSyncListener(listener) {
  syncState.listeners.add(listener)
  return () => syncState.listeners.delete(listener)
}

/**
 * 通知所有监听器
 * @param {Object} event - 事件对象
 */
function notifyListeners(event) {
  syncState.listeners.forEach(listener => {
    try {
      listener({ ...event, state: getSyncState() })
    } catch (error) {
      console.error('同步监听器执行错误:', error)
    }
  })
}

/**
 * 获取当前同步状态
 * @returns {Object}
 */
export function getSyncState() {
  return {
    status: syncState.status,
    isOnline: syncState.isOnline,
    lastSyncTime: syncState.lastSyncTime,
    syncInProgress: syncState.syncInProgress
  }
}

/**
 * 检查是否在线
 * @returns {boolean}
 */
export function isOnline() {
  return syncState.isOnline
}


/**
 * 同步待处理的数据
 * @returns {Promise<Object>} 同步结果
 */
export async function syncPendingData() {
  if (syncState.syncInProgress) {
    console.log('同步正在进行中，跳过')
    return { success: false, reason: 'sync_in_progress' }
  }
  
  if (!syncState.isOnline) {
    console.log('网络离线，无法同步')
    return { success: false, reason: 'offline' }
  }
  
  syncState.syncInProgress = true
  syncState.status = SyncStatus.SYNCING
  notifyListeners({ type: 'sync_start' })
  
  const results = {
    total: 0,
    success: 0,
    failed: 0,
    errors: []
  }
  
  try {
    const pendingItems = await getPendingSyncData()
    results.total = pendingItems.length
    
    if (pendingItems.length === 0) {
      console.log('没有待同步的数据')
      return { success: true, results }
    }
    
    console.log(`开始同步 ${pendingItems.length} 条数据`)
    
    // 分批处理
    for (let i = 0; i < pendingItems.length; i += SYNC_CONFIG.batchSize) {
      const batch = pendingItems.slice(i, i + SYNC_CONFIG.batchSize)
      
      for (const item of batch) {
        try {
          await syncSingleItem(item)
          await markAsSynced(item.id)
          results.success++
          
          notifyListeners({ 
            type: 'sync_progress', 
            current: results.success + results.failed,
            total: results.total
          })
        } catch (error) {
          results.failed++
          results.errors.push({ id: item.id, error: error.message })
          
          // 更新重试计数
          await handleSyncError(item, error)
        }
      }
    }
    
    // 清理已同步的数据
    await clearSyncedData()
    
    syncState.lastSyncTime = Date.now()
    syncState.status = results.failed > 0 ? SyncStatus.ERROR : SyncStatus.IDLE
    
    notifyListeners({ 
      type: 'sync_complete', 
      results,
      message: `同步完成: ${results.success}/${results.total} 成功`
    })
    
    return { success: true, results }
  } catch (error) {
    console.error('同步过程出错:', error)
    syncState.status = SyncStatus.ERROR
    notifyListeners({ type: 'sync_error', error: error.message })
    return { success: false, error: error.message }
  } finally {
    syncState.syncInProgress = false
  }
}

/**
 * 同步单个数据项
 * @param {Object} item - 同步项
 * @returns {Promise<void>}
 */
async function syncSingleItem(item) {
  const { type, action, data } = item
  const endpoints = API_ENDPOINTS[type]
  
  if (!endpoints) {
    throw new Error(`未知的数据类型: ${type}`)
  }
  
  let url, method, body
  
  switch (action) {
    case 'create':
      url = endpoints.create
      method = 'POST'
      body = JSON.stringify(data)
      break
    case 'update':
      url = endpoints.update(data.id)
      method = 'PUT'
      body = JSON.stringify(data)
      break
    case 'delete':
      url = endpoints.delete(data.id)
      method = 'DELETE'
      body = null
      break
    default:
      throw new Error(`未知的操作类型: ${action}`)
  }
  
  // 获取认证token
  const token = localStorage.getItem('token')
  
  const response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    },
    body
  })
  
  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(`API请求失败: ${response.status} - ${errorText}`)
  }
  
  console.log(`同步成功: ${type}/${action}`)
}

/**
 * 处理同步错误
 * @param {Object} item - 同步项
 * @param {Error} error - 错误对象
 */
async function handleSyncError(item, error) {
  const newRetryCount = (item.retryCount || 0) + 1
  
  if (newRetryCount >= SYNC_CONFIG.maxRetries) {
    // 达到最大重试次数，标记为失败
    await updateSyncItem(item.id, {
      status: 'failed',
      lastError: error.message,
      retryCount: newRetryCount
    })
    console.error(`同步项 ${item.id} 达到最大重试次数，标记为失败`)
  } else {
    // 更新重试计数，等待下次同步
    await updateSyncItem(item.id, {
      status: 'retrying',
      lastError: error.message,
      retryCount: newRetryCount,
      nextRetry: Date.now() + SYNC_CONFIG.retryDelay * newRetryCount
    })
    console.log(`同步项 ${item.id} 将在稍后重试 (${newRetryCount}/${SYNC_CONFIG.maxRetries})`)
  }
}


/**
 * 手动触发同步
 * @returns {Promise<Object>}
 */
export async function triggerSync() {
  return await syncPendingData()
}

/**
 * 添加训练数据到同步队列（便捷方法）
 * @param {string} action - 操作类型
 * @param {Object} data - 训练数据
 * @returns {Promise<string>}
 */
export async function queueTrainingSync(action, data) {
  return await addToSyncQueue({
    type: 'training',
    action,
    data
  })
}

/**
 * 添加营养数据到同步队列（便捷方法）
 * @param {string} action - 操作类型
 * @param {Object} data - 营养数据
 * @returns {Promise<string>}
 */
export async function queueNutritionSync(action, data) {
  return await addToSyncQueue({
    type: 'nutrition',
    action,
    data
  })
}

/**
 * 添加恢复数据到同步队列（便捷方法）
 * @param {string} action - 操作类型
 * @param {Object} data - 恢复数据
 * @returns {Promise<string>}
 */
export async function queueRecoverySync(action, data) {
  return await addToSyncQueue({
    type: 'recovery',
    action,
    data
  })
}

/**
 * 获取网络连接信息
 * @returns {Object}
 */
export function getNetworkInfo() {
  const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection
  
  if (connection) {
    return {
      type: connection.effectiveType || connection.type || 'unknown',
      downlink: connection.downlink,
      rtt: connection.rtt,
      saveData: connection.saveData
    }
  }
  
  return {
    type: navigator.onLine ? 'online' : 'offline',
    downlink: null,
    rtt: null,
    saveData: false
  }
}

// 导出状态常量
export { SyncStatus }
