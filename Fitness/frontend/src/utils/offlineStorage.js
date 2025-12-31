/**
 * 离线数据存储模块
 * 使用IndexedDB实现离线数据的保存、读取和同步队列管理
 * Requirements: 4.2
 */

const DB_NAME = 'FitnessOfflineDB'
const DB_VERSION = 1

// 数据库存储名称
const STORES = {
  SYNC_QUEUE: 'syncQueue',      // 待同步队列
  CACHED_DATA: 'cachedData',    // 缓存数据
  TRAINING_DATA: 'trainingData' // 训练数据离线存储
}

let dbInstance = null

/**
 * 打开或创建IndexedDB数据库
 * @returns {Promise<IDBDatabase>}
 */
export function openDatabase() {
  return new Promise((resolve, reject) => {
    if (dbInstance) {
      resolve(dbInstance)
      return
    }

    const request = indexedDB.open(DB_NAME, DB_VERSION)

    request.onerror = () => {
      console.error('IndexedDB打开失败:', request.error)
      reject(request.error)
    }

    request.onsuccess = () => {
      dbInstance = request.result
      console.log('IndexedDB打开成功')
      resolve(dbInstance)
    }

    request.onupgradeneeded = (event) => {
      const db = event.target.result

      // 创建同步队列存储
      if (!db.objectStoreNames.contains(STORES.SYNC_QUEUE)) {
        const syncStore = db.createObjectStore(STORES.SYNC_QUEUE, { keyPath: 'id' })
        syncStore.createIndex('type', 'type', { unique: false })
        syncStore.createIndex('timestamp', 'timestamp', { unique: false })
        syncStore.createIndex('synced', 'synced', { unique: false })
        syncStore.createIndex('status', 'status', { unique: false })
      }

      // 创建缓存数据存储
      if (!db.objectStoreNames.contains(STORES.CACHED_DATA)) {
        const cacheStore = db.createObjectStore(STORES.CACHED_DATA, { keyPath: 'key' })
        cacheStore.createIndex('expiry', 'expiry', { unique: false })
      }

      // 创建训练数据存储
      if (!db.objectStoreNames.contains(STORES.TRAINING_DATA)) {
        const trainingStore = db.createObjectStore(STORES.TRAINING_DATA, { keyPath: 'id', autoIncrement: true })
        trainingStore.createIndex('date', 'date', { unique: false })
        trainingStore.createIndex('userId', 'userId', { unique: false })
      }

      console.log('IndexedDB数据库结构创建完成')
    }
  })
}


/**
 * 生成唯一ID
 * @returns {string}
 */
function generateId() {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 保存离线数据到缓存
 * @param {string} key - 缓存键
 * @param {any} data - 要缓存的数据
 * @param {number} ttl - 过期时间（毫秒），默认5分钟
 * @returns {Promise<void>}
 */
export async function saveOfflineData(key, data, ttl = 5 * 60 * 1000) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.CACHED_DATA], 'readwrite')
    const store = transaction.objectStore(STORES.CACHED_DATA)
    
    const cacheItem = {
      key,
      data,
      timestamp: Date.now(),
      expiry: Date.now() + ttl
    }
    
    const request = store.put(cacheItem)
    
    request.onsuccess = () => {
      console.log(`离线数据已保存: ${key}`)
      resolve()
    }
    
    request.onerror = () => {
      console.error(`保存离线数据失败: ${key}`, request.error)
      reject(request.error)
    }
  })
}

/**
 * 获取离线缓存数据
 * @param {string} key - 缓存键
 * @returns {Promise<any|null>}
 */
export async function getOfflineData(key) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.CACHED_DATA], 'readonly')
    const store = transaction.objectStore(STORES.CACHED_DATA)
    const request = store.get(key)
    
    request.onsuccess = () => {
      const result = request.result
      
      if (!result) {
        resolve(null)
        return
      }
      
      // 检查是否过期
      if (result.expiry < Date.now()) {
        // 数据已过期，删除并返回null
        deleteOfflineData(key).catch(console.error)
        resolve(null)
        return
      }
      
      resolve(result.data)
    }
    
    request.onerror = () => {
      console.error(`获取离线数据失败: ${key}`, request.error)
      reject(request.error)
    }
  })
}

/**
 * 删除离线缓存数据
 * @param {string} key - 缓存键
 * @returns {Promise<void>}
 */
export async function deleteOfflineData(key) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.CACHED_DATA], 'readwrite')
    const store = transaction.objectStore(STORES.CACHED_DATA)
    const request = store.delete(key)
    
    request.onsuccess = () => resolve()
    request.onerror = () => reject(request.error)
  })
}

/**
 * 清除所有过期的缓存数据
 * @returns {Promise<number>} 清除的数据条数
 */
export async function clearExpiredCache() {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.CACHED_DATA], 'readwrite')
    const store = transaction.objectStore(STORES.CACHED_DATA)
    const index = store.index('expiry')
    const now = Date.now()
    const range = IDBKeyRange.upperBound(now)
    
    let deletedCount = 0
    const request = index.openCursor(range)
    
    request.onsuccess = (event) => {
      const cursor = event.target.result
      if (cursor) {
        cursor.delete()
        deletedCount++
        cursor.continue()
      } else {
        console.log(`清除了 ${deletedCount} 条过期缓存`)
        resolve(deletedCount)
      }
    }
    
    request.onerror = () => reject(request.error)
  })
}


/**
 * 添加数据到同步队列
 * @param {Object} syncItem - 同步项
 * @param {string} syncItem.type - 数据类型 ('training' | 'nutrition' | 'recovery')
 * @param {string} syncItem.action - 操作类型 ('create' | 'update' | 'delete')
 * @param {any} syncItem.data - 要同步的数据
 * @returns {Promise<string>} 同步项ID
 */
export async function addToSyncQueue(syncItem) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.SYNC_QUEUE], 'readwrite')
    const store = transaction.objectStore(STORES.SYNC_QUEUE)
    
    const item = {
      id: generateId(),
      type: syncItem.type,
      action: syncItem.action,
      data: syncItem.data,
      timestamp: Date.now(),
      synced: false,
      status: 'pending',
      retryCount: 0,
      lastError: null
    }
    
    const request = store.add(item)
    
    request.onsuccess = () => {
      console.log(`添加到同步队列: ${item.id}`)
      resolve(item.id)
    }
    
    request.onerror = () => {
      console.error('添加到同步队列失败:', request.error)
      reject(request.error)
    }
  })
}

/**
 * 获取待同步的数据列表
 * @returns {Promise<Array>}
 */
export async function getPendingSyncData() {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.SYNC_QUEUE], 'readonly')
    const store = transaction.objectStore(STORES.SYNC_QUEUE)
    const index = store.index('synced')
    const request = index.getAll(IDBKeyRange.only(false))
    
    request.onsuccess = () => {
      const items = request.result || []
      // 按时间戳排序，先进先出
      items.sort((a, b) => a.timestamp - b.timestamp)
      resolve(items)
    }
    
    request.onerror = () => reject(request.error)
  })
}

/**
 * 标记同步项为已同步
 * @param {string} id - 同步项ID
 * @returns {Promise<void>}
 */
export async function markAsSynced(id) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.SYNC_QUEUE], 'readwrite')
    const store = transaction.objectStore(STORES.SYNC_QUEUE)
    const request = store.get(id)
    
    request.onsuccess = () => {
      const item = request.result
      if (item) {
        item.synced = true
        item.status = 'synced'
        item.syncedAt = Date.now()
        
        const updateRequest = store.put(item)
        updateRequest.onsuccess = () => {
          console.log(`同步项已标记为已同步: ${id}`)
          resolve()
        }
        updateRequest.onerror = () => reject(updateRequest.error)
      } else {
        resolve()
      }
    }
    
    request.onerror = () => reject(request.error)
  })
}

/**
 * 更新同步项状态（用于错误处理）
 * @param {string} id - 同步项ID
 * @param {Object} updates - 更新内容
 * @returns {Promise<void>}
 */
export async function updateSyncItem(id, updates) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.SYNC_QUEUE], 'readwrite')
    const store = transaction.objectStore(STORES.SYNC_QUEUE)
    const request = store.get(id)
    
    request.onsuccess = () => {
      const item = request.result
      if (item) {
        Object.assign(item, updates)
        
        const updateRequest = store.put(item)
        updateRequest.onsuccess = () => resolve()
        updateRequest.onerror = () => reject(updateRequest.error)
      } else {
        resolve()
      }
    }
    
    request.onerror = () => reject(request.error)
  })
}

/**
 * 清除已同步的数据
 * @returns {Promise<number>} 清除的数据条数
 */
export async function clearSyncedData() {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.SYNC_QUEUE], 'readwrite')
    const store = transaction.objectStore(STORES.SYNC_QUEUE)
    const index = store.index('synced')
    const request = index.openCursor(IDBKeyRange.only(true))
    
    let deletedCount = 0
    
    request.onsuccess = (event) => {
      const cursor = event.target.result
      if (cursor) {
        cursor.delete()
        deletedCount++
        cursor.continue()
      } else {
        console.log(`清除了 ${deletedCount} 条已同步数据`)
        resolve(deletedCount)
      }
    }
    
    request.onerror = () => reject(request.error)
  })
}

/**
 * 获取同步队列统计信息
 * @returns {Promise<Object>}
 */
export async function getSyncQueueStats() {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.SYNC_QUEUE], 'readonly')
    const store = transaction.objectStore(STORES.SYNC_QUEUE)
    const request = store.getAll()
    
    request.onsuccess = () => {
      const items = request.result || []
      const stats = {
        total: items.length,
        pending: items.filter(i => !i.synced && i.status === 'pending').length,
        synced: items.filter(i => i.synced).length,
        failed: items.filter(i => i.status === 'failed').length,
        retrying: items.filter(i => i.status === 'retrying').length
      }
      resolve(stats)
    }
    
    request.onerror = () => reject(request.error)
  })
}


/**
 * 保存训练数据到离线存储
 * @param {Object} trainingData - 训练数据
 * @returns {Promise<number>} 存储的ID
 */
export async function saveTrainingDataOffline(trainingData) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.TRAINING_DATA], 'readwrite')
    const store = transaction.objectStore(STORES.TRAINING_DATA)
    
    const data = {
      ...trainingData,
      savedAt: Date.now(),
      isOffline: true
    }
    
    const request = store.add(data)
    
    request.onsuccess = () => {
      console.log('训练数据已离线保存:', request.result)
      resolve(request.result)
    }
    
    request.onerror = () => {
      console.error('离线保存训练数据失败:', request.error)
      reject(request.error)
    }
  })
}

/**
 * 获取离线存储的训练数据
 * @param {Object} options - 查询选项
 * @param {number} options.userId - 用户ID
 * @param {string} options.startDate - 开始日期
 * @param {string} options.endDate - 结束日期
 * @returns {Promise<Array>}
 */
export async function getOfflineTrainingData(options = {}) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.TRAINING_DATA], 'readonly')
    const store = transaction.objectStore(STORES.TRAINING_DATA)
    const request = store.getAll()
    
    request.onsuccess = () => {
      let results = request.result || []
      
      // 根据选项过滤
      if (options.userId) {
        results = results.filter(item => item.userId === options.userId)
      }
      
      if (options.startDate) {
        results = results.filter(item => item.date >= options.startDate)
      }
      
      if (options.endDate) {
        results = results.filter(item => item.date <= options.endDate)
      }
      
      // 按日期排序
      results.sort((a, b) => new Date(b.date) - new Date(a.date))
      
      resolve(results)
    }
    
    request.onerror = () => reject(request.error)
  })
}

/**
 * 删除离线训练数据
 * @param {number} id - 数据ID
 * @returns {Promise<void>}
 */
export async function deleteOfflineTrainingData(id) {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.TRAINING_DATA], 'readwrite')
    const store = transaction.objectStore(STORES.TRAINING_DATA)
    const request = store.delete(id)
    
    request.onsuccess = () => {
      console.log('离线训练数据已删除:', id)
      resolve()
    }
    
    request.onerror = () => reject(request.error)
  })
}

/**
 * 清除所有离线训练数据
 * @returns {Promise<void>}
 */
export async function clearAllOfflineTrainingData() {
  const db = await openDatabase()
  
  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORES.TRAINING_DATA], 'readwrite')
    const store = transaction.objectStore(STORES.TRAINING_DATA)
    const request = store.clear()
    
    request.onsuccess = () => {
      console.log('所有离线训练数据已清除')
      resolve()
    }
    
    request.onerror = () => reject(request.error)
  })
}

// 导出存储名称常量
export { STORES, DB_NAME, DB_VERSION }
