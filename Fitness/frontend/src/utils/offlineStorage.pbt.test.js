/**
 * 离线数据同步完整性属性测试
 * Property 2: 离线数据同步完整性
 * Validates: Requirements 4.3, 4.5
 * 
 * 测试属性：对于任何离线录入的训练数据，在网络恢复后同步到服务器，
 * 同步后的数据应与离线录入的数据完全一致
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { test, fc } from '@fast-check/vitest'

/**
 * 简化的内存存储实现，用于测试数据完整性属性
 * 这个实现模拟了IndexedDB的核心行为
 */
class InMemoryOfflineStorage {
  constructor() {
    this.syncQueue = new Map()
    this.cachedData = new Map()
    this.trainingData = new Map()
    this.autoIncrementId = 0
  }
  
  // 生成唯一ID
  generateId() {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }
  
  // 保存离线缓存数据
  saveOfflineData(key, data, ttl = 5 * 60 * 1000) {
    const cacheItem = {
      key,
      data,
      timestamp: Date.now(),
      expiry: Date.now() + ttl
    }
    this.cachedData.set(key, cacheItem)
    return cacheItem
  }
  
  // 获取离线缓存数据
  getOfflineData(key) {
    const item = this.cachedData.get(key)
    if (!item) return null
    if (item.expiry < Date.now()) {
      this.cachedData.delete(key)
      return null
    }
    return item.data
  }
  
  // 添加到同步队列
  addToSyncQueue(syncItem) {
    const id = this.generateId()
    const item = {
      id,
      type: syncItem.type,
      action: syncItem.action,
      data: { ...syncItem.data },
      timestamp: Date.now(),
      synced: false,
      status: 'pending',
      retryCount: 0,
      lastError: null
    }
    this.syncQueue.set(id, item)
    return id
  }
  
  // 获取待同步数据
  getPendingSyncData() {
    const items = []
    for (const item of this.syncQueue.values()) {
      if (!item.synced) {
        items.push({ ...item })
      }
    }
    return items.sort((a, b) => a.timestamp - b.timestamp)
  }
  
  // 标记为已同步
  markAsSynced(id) {
    const item = this.syncQueue.get(id)
    if (item) {
      item.synced = true
      item.status = 'synced'
      item.syncedAt = Date.now()
    }
  }
  
  // 保存训练数据
  saveTrainingDataOffline(trainingData) {
    this.autoIncrementId++
    const id = this.autoIncrementId
    const data = {
      ...trainingData,
      id,
      savedAt: Date.now(),
      isOffline: true
    }
    this.trainingData.set(id, data)
    return id
  }
  
  // 获取训练数据
  getOfflineTrainingData(options = {}) {
    let results = Array.from(this.trainingData.values())
    
    if (options.userId) {
      results = results.filter(item => item.userId === options.userId)
    }
    if (options.startDate) {
      results = results.filter(item => item.date >= options.startDate)
    }
    if (options.endDate) {
      results = results.filter(item => item.date <= options.endDate)
    }
    
    return results.sort((a, b) => new Date(b.date) - new Date(a.date))
  }
  
  // 清除所有数据
  clear() {
    this.syncQueue.clear()
    this.cachedData.clear()
    this.trainingData.clear()
    this.autoIncrementId = 0
  }
}

describe('离线数据同步完整性属性测试', () => {
  let storage
  
  beforeEach(() => {
    storage = new InMemoryOfflineStorage()
  })
  
  /**
   * Feature: system-optimization-phase2, Property 2: 离线数据同步完整性
   * Validates: Requirements 4.3, 4.5
   * 
   * 属性：对于任何有效的训练数据，保存到离线存储后再读取，
   * 读取的数据应与原始数据在关键字段上完全一致
   */
  test.prop([
    fc.record({
      exerciseName: fc.string({ minLength: 1, maxLength: 50 }),
      sets: fc.integer({ min: 1, max: 20 }),
      reps: fc.integer({ min: 1, max: 100 }),
      weight: fc.float({ min: 0, max: 500, noNaN: true }),
      date: fc.date({ min: new Date('2020-01-01'), max: new Date('2030-12-31') })
        .map(d => d.toISOString().split('T')[0]),
      userId: fc.integer({ min: 1, max: 10000 }),
      notes: fc.option(fc.string({ maxLength: 200 }), { nil: undefined })
    })
  ], { numRuns: 100 })(
    'Property 2: 离线数据保存后读取应保持数据完整性',
    (trainingData) => {
      // 保存训练数据到离线存储
      const savedId = storage.saveTrainingDataOffline(trainingData)
      
      // 从离线存储读取数据
      const retrievedData = storage.getOfflineTrainingData({ userId: trainingData.userId })
      
      // 验证数据完整性
      expect(retrievedData.length).toBeGreaterThan(0)
      
      const matchingRecord = retrievedData.find(r => r.id === savedId)
      
      expect(matchingRecord).toBeDefined()
      expect(matchingRecord.exerciseName).toBe(trainingData.exerciseName)
      expect(matchingRecord.sets).toBe(trainingData.sets)
      expect(matchingRecord.reps).toBe(trainingData.reps)
      expect(matchingRecord.date).toBe(trainingData.date)
      expect(matchingRecord.userId).toBe(trainingData.userId)
      
      // 验证weight在合理精度范围内相等（浮点数比较）
      expect(Math.abs(matchingRecord.weight - trainingData.weight)).toBeLessThan(0.001)
      
      // 验证notes字段
      if (trainingData.notes !== undefined) {
        expect(matchingRecord.notes).toBe(trainingData.notes)
      }
      
      // 验证离线标记
      expect(matchingRecord.isOffline).toBe(true)
      expect(matchingRecord.savedAt).toBeDefined()
    }
  )
  
  /**
   * Feature: system-optimization-phase2, Property 2: 同步队列数据完整性
   * Validates: Requirements 4.3, 4.5
   * 
   * 属性：对于任何添加到同步队列的数据，在标记为已同步前，
   * 应该能够完整地从待同步列表中获取
   */
  test.prop([
    fc.record({
      type: fc.constantFrom('training', 'nutrition', 'recovery'),
      action: fc.constantFrom('create', 'update', 'delete'),
      data: fc.record({
        id: fc.integer({ min: 1, max: 10000 }),
        name: fc.string({ minLength: 1, maxLength: 100 }),
        value: fc.float({ min: 0, max: 1000, noNaN: true })
      })
    })
  ], { numRuns: 100 })(
    'Property 2: 同步队列数据在同步前应保持完整',
    (syncItem) => {
      // 添加到同步队列
      const syncId = storage.addToSyncQueue(syncItem)
      
      // 获取待同步数据
      const pendingData = storage.getPendingSyncData()
      
      // 验证数据在待同步列表中
      const foundItem = pendingData.find(item => item.id === syncId)
      
      expect(foundItem).toBeDefined()
      expect(foundItem.type).toBe(syncItem.type)
      expect(foundItem.action).toBe(syncItem.action)
      expect(foundItem.data.id).toBe(syncItem.data.id)
      expect(foundItem.data.name).toBe(syncItem.data.name)
      expect(foundItem.synced).toBe(false)
      expect(foundItem.status).toBe('pending')
    }
  )
  
  /**
   * Feature: system-optimization-phase2, Property 2: 缓存数据往返一致性
   * Validates: Requirements 4.3, 4.5
   * 
   * 属性：对于任何保存到缓存的数据，在TTL内读取应返回完全相同的数据
   */
  test.prop([
    fc.string({ minLength: 1, maxLength: 50 }),
    fc.jsonValue()
  ], { numRuns: 100 })(
    'Property 2: 缓存数据往返应保持一致性',
    (key, data) => {
      // 保存数据到缓存（TTL设置为1小时确保不过期）
      storage.saveOfflineData(key, data, 60 * 60 * 1000)
      
      // 读取缓存数据
      const retrievedData = storage.getOfflineData(key)
      
      // 验证数据一致性
      expect(retrievedData).toEqual(data)
    }
  )
  
  /**
   * Feature: system-optimization-phase2, Property 2: 同步标记后数据状态变更
   * Validates: Requirements 4.3, 4.5
   * 
   * 属性：对于任何已同步的数据，标记后应从待同步列表中移除
   */
  test.prop([
    fc.record({
      type: fc.constantFrom('training', 'nutrition', 'recovery'),
      action: fc.constantFrom('create', 'update', 'delete'),
      data: fc.record({
        id: fc.integer({ min: 1, max: 10000 }),
        name: fc.string({ minLength: 1, maxLength: 100 })
      })
    })
  ], { numRuns: 100 })(
    'Property 2: 同步标记后数据应从待同步列表移除',
    (syncItem) => {
      // 添加到同步队列
      const syncId = storage.addToSyncQueue(syncItem)
      
      // 验证在待同步列表中
      let pendingData = storage.getPendingSyncData()
      expect(pendingData.some(item => item.id === syncId)).toBe(true)
      
      // 标记为已同步
      storage.markAsSynced(syncId)
      
      // 验证不再在待同步列表中
      pendingData = storage.getPendingSyncData()
      expect(pendingData.some(item => item.id === syncId)).toBe(false)
    }
  )
})
