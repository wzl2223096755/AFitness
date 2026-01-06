/**
 * 带缓存的API封装
 * 为仪表盘和用户信息API提供缓存支持
 */

import { get, post, put } from './request.js'
import { 
  cachedRequest, 
  apiCacheManager, 
  cacheConfig,
  invalidateCache 
} from './apiCache.js'

/**
 * 带缓存的仪表盘API
 */
export const cachedDashboardApi = {
  getDashboardStats: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/stats', {})
    return cachedRequest(
      () => get('/api/v1/user/stats'),
      cacheKey,
      { 
        ...cacheConfig.strategies.dashboard,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getMetricsOverview: (timeRange = 'week', options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/dashboard/metrics-overview', { timeRange })
    return cachedRequest(
      () => get('/api/v1/dashboard/metrics-overview', { timeRange }),
      cacheKey,
      { 
        ...cacheConfig.strategies.metricsOverview,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getUserStatsOverview: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/dashboard/user-stats-overview', {})
    return cachedRequest(
      () => get('/api/v1/dashboard/user-stats-overview'),
      cacheKey,
      { 
        ...cacheConfig.strategies.userStats,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getAnalyticsData: (timeRange = 'week', options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/dashboard/analytics', { timeRange })
    return cachedRequest(
      () => get('/api/v1/dashboard/analytics', { timeRange }),
      cacheKey,
      { 
        ...cacheConfig.strategies.analytics,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getRecentTrainingRecords: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/dashboard/recent-training-records', {})
    return cachedRequest(
      () => get('/api/v1/dashboard/recent-training-records'),
      cacheKey,
      { 
        ...cacheConfig.strategies.trainingRecords,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getTrainingHistory: (params = {}, options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/training-history', params)
    return cachedRequest(
      () => get('/api/v1/user/training-history', params),
      cacheKey,
      { 
        ...cacheConfig.strategies.trainingRecords,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  invalidateCache: () => {
    invalidateCache('/api/v1/dashboard')
    invalidateCache('/api/v1/user/stats')
    invalidateCache('/api/v1/user/training-history')
  }
}

/**
 * 带缓存的用户API
 */
export const cachedUserApi = {
  getCurrentUser: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/profile', {})
    return cachedRequest(
      () => get('/api/v1/user/profile'),
      cacheKey,
      { 
        ...cacheConfig.strategies.userProfile,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getUserSettings: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/settings', {})
    return cachedRequest(
      () => get('/api/v1/user/settings'),
      cacheKey,
      { 
        ...cacheConfig.strategies.userProfile,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getUserStats: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/stats', {})
    return cachedRequest(
      () => get('/api/v1/user/stats'),
      cacheKey,
      { 
        ...cacheConfig.strategies.userStats,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getUserAchievements: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/achievements', {})
    return cachedRequest(
      () => get('/api/v1/user/achievements'),
      cacheKey,
      { 
        ttl: 10 * 60 * 1000,
        staleWhileRevalidate: true,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  getBodyRecords: (params = {}, options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/body-records', params)
    return cachedRequest(
      () => get('/api/v1/user/body-records', params),
      cacheKey,
      { 
        ttl: 5 * 60 * 1000,
        staleWhileRevalidate: true,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  updateProfile: async (data) => {
    const result = await put('/api/v1/user/profile', data)
    if (result && result.code === 200) {
      cachedUserApi.invalidateCache()
    }
    return result
  },

  updateUserSettings: async (data) => {
    const result = await put('/api/v1/user/settings', data)
    if (result && result.code === 200) {
      invalidateCache('/api/v1/user/settings')
    }
    return result
  },

  invalidateCache: () => {
    invalidateCache('/api/v1/user/profile')
    invalidateCache('/api/v1/user/settings')
    invalidateCache('/api/v1/user/stats')
    invalidateCache('/api/v1/user/achievements')
    invalidateCache('/api/v1/user/body-records')
  }
}

/**
 * 缓存工具函数
 */
export const cacheUtils = {
  getStats: () => apiCacheManager.getStats(),
  clearAll: () => apiCacheManager.clear(),
  clearExpired: () => apiCacheManager.clearExpired(),
  invalidate: (endpoint) => invalidateCache(endpoint)
}

export default {
  dashboard: cachedDashboardApi,
  user: cachedUserApi,
  utils: cacheUtils
}
