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
 * API缓存管理类
 */
class ApiCacheManager {
  constructor() {
    this.cache = new Map()
    this.revalidatingKeys = new Set()
  }

  generateKey(endpoint, params = {}) {
    const sortedParams = Object.keys(params)
      .sort()
      .reduce((acc, key) => {
        if (key !== '_t') {
          acc[key] = params[key]
        }
        return acc
      }, {})
    return `${endpoint}:${JSON.stringify(sortedParams)}`
  }

  get(key) {
    const entry = this.cache.get(key)
    if (!entry) return null
    return entry
  }

  set(key, data, options = {}) {
    const { ttl = 300000, staleWhileRevalidate = true } = options
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl,
      staleWhileRevalidate
    })
  }

  isExpired(entry) {
    if (!entry) return true
    return Date.now() - entry.timestamp > entry.ttl
  }

  isStale(entry) {
    if (!entry) return false
    return this.isExpired(entry) && entry.staleWhileRevalidate
  }

  delete(key) {
    this.cache.delete(key)
    this.revalidatingKeys.delete(key)
  }

  clear() {
    this.cache.clear()
    this.revalidatingKeys.clear()
  }

  clearExpired() {
    const now = Date.now()
    for (const [key, entry] of this.cache.entries()) {
      const maxAge = entry.staleWhileRevalidate ? entry.ttl * 2 : entry.ttl
      if (now - entry.timestamp > maxAge) {
        this.cache.delete(key)
      }
    }
  }

  markRevalidating(key) {
    this.revalidatingKeys.add(key)
  }

  unmarkRevalidating(key) {
    this.revalidatingKeys.delete(key)
  }

  isRevalidating(key) {
    return this.revalidatingKeys.has(key)
  }

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

export const apiCacheManager = new ApiCacheManager()

export async function cachedRequest(requestFn, cacheKey, options = {}) {
  const { ttl = 300000, staleWhileRevalidate = true, forceRefresh = false } = options

  if (forceRefresh) {
    const result = await requestFn()
    if (result && result.code === 200) {
      apiCacheManager.set(cacheKey, result, { ttl, staleWhileRevalidate })
    }
    return result
  }

  const cached = apiCacheManager.get(cacheKey)

  if (cached && !apiCacheManager.isExpired(cached)) {
    return cached.data
  }

  if (cached && apiCacheManager.isStale(cached)) {
    if (!apiCacheManager.isRevalidating(cacheKey)) {
      apiCacheManager.markRevalidating(cacheKey)
      
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

  try {
    const result = await requestFn()
    if (result && result.code === 200) {
      apiCacheManager.set(cacheKey, result, { ttl, staleWhileRevalidate })
    }
    return result
  } catch (error) {
    if (cached) {
      console.warn('请求失败，返回过时缓存数据')
      return cached.data
    }
    throw error
  }
}

export function invalidateCache(endpoint) {
  for (const key of apiCacheManager.cache.keys()) {
    if (key.startsWith(endpoint)) {
      apiCacheManager.delete(key)
    }
  }
}

export function invalidateAllCache() {
  apiCacheManager.clear()
}

setInterval(() => {
  apiCacheManager.clearExpired()
}, 60000)

export default apiCacheManager
