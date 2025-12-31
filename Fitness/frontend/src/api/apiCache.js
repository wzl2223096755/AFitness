/**
 * API缓存模块
 * 实现stale-while-revalidate策略的API请求缓存
 */

import { PerformanceUtils } from '../utils/performance.js'

/**
 * 缓存配置
 */
export const cacheConfig = {
  strategies: {
    // 仪表盘数据 - 5分钟缓存
    dashboard: { ttl: 5 * 60 * 1000, staleWhileRevalidate: true },
    // 用户信息 - 10分钟缓存
    userProfile: { ttl: 10 * 60 * 1000, staleWhileRevalidate: true },
    // 训练记录列表 - 2分钟缓存
    trainingRecords: { ttl: 2 * 60 * 1000, staleWhileRevalidate: true },
    // 营养目标 - 30分钟缓存
    nutritionGoals: { ttl: 30 * 60 * 1000, staleWhileRevalidate: false },
    // 用户统计 - 5分钟缓存
    userStats: { ttl: 5 * 60 * 1000, staleWhileRevalidate: true },
    // 指标概览 - 3分钟缓存
    metricsOverview: { ttl: 3 * 60 * 1000, staleWhileRevalidate: true },
    // 分析数据 - 5分钟缓存
    analytics: { ttl: 5 * 60 * 1000, staleWhileRevalidate: true }
  }
}

/**
 * 缓存条目结构
 * @typedef {Object} CacheEntry
 * @property {any} data - 缓存的数据
 * @property {number} timestamp - 缓存时间戳
 * @property {number} ttl - 生存时间
 * @property {boolean} staleWhileRevalidate - 是否启用stale-while-revalidate
 * @property {boolean} isRevalidating - 是否正在重新验证
 */

/**
 * API缓存管理类
 */
class ApiCacheManager {
  constructor() {
    this.cache = new Map()
    this.revalidatingKeys = new Set()
  }

  /**
   * 生成缓存键
   * @param {string} endpoint - API端点
   * @param {Object} params - 请求参数
   * @returns {string} 缓存键
   */
  generateKey(endpoint, params = {}) {
    const sortedParams = Object.keys(params)
      .sort()
      .reduce((acc, key) => {
        // 过滤掉时间戳参数
        if (key !== '_t') {
          acc[key] = params[key]
        }
        return acc
      }, {})
    return `${endpoint}:${JSON.stringify(sortedParams)}`
  }

  /**
   * 获取缓存
   * @param {string} key - 缓存键
   * @returns {CacheEntry|null} 缓存条目或null
   */
  get(key) {
    const entry = this.cache.get(key)
    if (!entry) return null
    return entry
  }

  /**
   * 设置缓存
   * @param {string} key - 缓存键
   * @param {any} data - 数据
   * @param {Object} options - 缓存选项
   */
  set(key, data, options = {}) {
    const { ttl = 300000, staleWhileRevalidate = true } = options
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl,
      staleWhileRevalidate
    })
  }

  /**
   * 检查缓存是否过期
   * @param {CacheEntry} entry - 缓存条目
   * @returns {boolean} 是否过期
   */
  isExpired(entry) {
    if (!entry) return true
    return Date.now() - entry.timestamp > entry.ttl
  }

  /**
   * 检查缓存是否过时但可用（stale）
   * @param {CacheEntry} entry - 缓存条目
   * @returns {boolean} 是否过时但可用
   */
  isStale(entry) {
    if (!entry) return false
    return this.isExpired(entry) && entry.staleWhileRevalidate
  }

  /**
   * 删除缓存
   * @param {string} key - 缓存键
   */
  delete(key) {
    this.cache.delete(key)
    this.revalidatingKeys.delete(key)
  }

  /**
   * 清除所有缓存
   */
  clear() {
    this.cache.clear()
    this.revalidatingKeys.clear()
  }

  /**
   * 清除过期缓存
   */
  clearExpired() {
    const now = Date.now()
    for (const [key, entry] of this.cache.entries()) {
      // 对于stale-while-revalidate，保留2倍TTL时间
      const maxAge = entry.staleWhileRevalidate ? entry.ttl * 2 : entry.ttl
      if (now - entry.timestamp > maxAge) {
        this.cache.delete(key)
      }
    }
  }

  /**
   * 标记正在重新验证
   * @param {string} key - 缓存键
   */
  markRevalidating(key) {
    this.revalidatingKeys.add(key)
  }

  /**
   * 取消重新验证标记
   * @param {string} key - 缓存键
   */
  unmarkRevalidating(key) {
    this.revalidatingKeys.delete(key)
  }

  /**
   * 检查是否正在重新验证
   * @param {string} key - 缓存键
   * @returns {boolean}
   */
  isRevalidating(key) {
    return this.revalidatingKeys.has(key)
  }

  /**
   * 获取缓存统计信息
   * @returns {Object} 统计信息
   */
  getStats() {
    let total = 0
    let expired = 0
    let stale = 0
    let fresh = 0

    for (const [, entry] of this.cache.entries()) {
      total++
      if (this.isExpired(entry)) {
        if (entry.staleWhileRevalidate) {
          stale++
        } else {
          expired++
        }
      } else {
        fresh++
      }
    }

    return { total, fresh, stale, expired }
  }
}

// 创建单例实例
export const apiCacheManager = new ApiCacheManager()

/**
 * 带缓存的API请求包装器
 * 实现stale-while-revalidate策略
 * 
 * @param {Function} requestFn - 原始请求函数
 * @param {string} cacheKey - 缓存键
 * @param {Object} options - 缓存选项
 * @returns {Promise<any>} 请求结果
 */
export async function cachedRequest(requestFn, cacheKey, options = {}) {
  const { ttl = 300000, staleWhileRevalidate = true, forceRefresh = false } = options

  // 强制刷新时跳过缓存
  if (forceRefresh) {
    const result = await requestFn()
    if (result && result.code === 200) {
      apiCacheManager.set(cacheKey, result, { ttl, staleWhileRevalidate })
    }
    return result
  }

  // 获取缓存
  const cached = apiCacheManager.get(cacheKey)

  // 缓存命中且未过期
  if (cached && !apiCacheManager.isExpired(cached)) {
    return cached.data
  }

  // 缓存过期但启用stale-while-revalidate
  if (cached && apiCacheManager.isStale(cached)) {
    // 返回过时数据，同时在后台重新验证
    if (!apiCacheManager.isRevalidating(cacheKey)) {
      apiCacheManager.markRevalidating(cacheKey)
      
      // 后台重新验证
      requestFn()
        .then(result => {
          if (result && result.code === 200) {
            apiCacheManager.set(cacheKey, result, { ttl, staleWhileRevalidate })
          }
        })
        .catch(error => {
          console.warn('后台重新验证失败:', error)
        })
        .finally(() => {
          apiCacheManager.unmarkRevalidating(cacheKey)
        })
    }
    
    return cached.data
  }

  // 无缓存或缓存完全过期，执行请求
  try {
    const result = await requestFn()
    if (result && result.code === 200) {
      apiCacheManager.set(cacheKey, result, { ttl, staleWhileRevalidate })
    }
    return result
  } catch (error) {
    // 请求失败时，如果有过时缓存，返回过时数据
    if (cached) {
      console.warn('请求失败，返回过时缓存数据')
      return cached.data
    }
    throw error
  }
}

/**
 * 使指定端点的缓存失效
 * @param {string} endpoint - API端点
 */
export function invalidateCache(endpoint) {
  for (const key of apiCacheManager.cache.keys()) {
    if (key.startsWith(endpoint)) {
      apiCacheManager.delete(key)
    }
  }
}

/**
 * 使所有缓存失效
 */
export function invalidateAllCache() {
  apiCacheManager.clear()
}

// 定期清理过期缓存
setInterval(() => {
  apiCacheManager.clearExpired()
}, 60000) // 每分钟清理一次

export default apiCacheManager
