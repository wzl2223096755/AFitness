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
  /**
   * 获取仪表盘统计数据（带缓存）
   * @param {Object} options - 选项
   * @param {boolean} options.forceRefresh - 是否强制刷新
   * @returns {Promise<any>}
   */
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

  /**
   * 获取指标概览（带缓存）
   * @param {string} timeRange - 时间范围
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 获取用户统计概览（带缓存）
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 获取分析数据（带缓存）
   * @param {string} timeRange - 时间范围
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 获取最近训练记录（带缓存）
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 获取训练历史（带缓存）
   * @param {Object} params - 查询参数
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 使仪表盘缓存失效
   */
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
  /**
   * 获取当前用户信息（带缓存）
   * @param {Object} options - 选项
   * @param {boolean} options.forceRefresh - 是否强制刷新
   * @returns {Promise<any>}
   */
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

  /**
   * 获取用户设置（带缓存）
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 获取用户统计数据（带缓存）
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
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

  /**
   * 获取用户成就（带缓存）
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
  getUserAchievements: (options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/achievements', {})
    return cachedRequest(
      () => get('/api/v1/user/achievements'),
      cacheKey,
      { 
        ttl: 10 * 60 * 1000, // 10分钟
        staleWhileRevalidate: true,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  /**
   * 获取用户身体数据记录（带缓存）
   * @param {Object} params - 查询参数
   * @param {Object} options - 选项
   * @returns {Promise<any>}
   */
  getBodyRecords: (params = {}, options = {}) => {
    const cacheKey = apiCacheManager.generateKey('/api/v1/user/body-records', params)
    return cachedRequest(
      () => get('/api/v1/user/body-records', params),
      cacheKey,
      { 
        ttl: 5 * 60 * 1000, // 5分钟
        staleWhileRevalidate: true,
        forceRefresh: options.forceRefresh 
      }
    )
  },

  /**
   * 更新用户资料（同时使缓存失效）
   * @param {Object} data - 用户数据
   * @returns {Promise<any>}
   */
  updateProfile: async (data) => {
    const result = await put('/api/v1/user/profile', data)
    // 更新成功后使用户缓存失效
    if (result && result.code === 200) {
      cachedUserApi.invalidateCache()
    }
    return result
  },

  /**
   * 更新用户设置（同时使缓存失效）
   * @param {Object} data - 设置数据
   * @returns {Promise<any>}
   */
  updateUserSettings: async (data) => {
    const result = await put('/api/v1/user/settings', data)
    // 更新成功后使设置缓存失效
    if (result && result.code === 200) {
      invalidateCache('/api/v1/user/settings')
    }
    return result
  },

  /**
   * 使用户缓存失效
   */
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
  /**
   * 获取缓存统计信息
   * @returns {Object}
   */
  getStats: () => apiCacheManager.getStats(),

  /**
   * 清除所有缓存
   */
  clearAll: () => apiCacheManager.clear(),

  /**
   * 清除过期缓存
   */
  clearExpired: () => apiCacheManager.clearExpired(),

  /**
   * 使指定端点缓存失效
   * @param {string} endpoint - API端点
   */
  invalidate: (endpoint) => invalidateCache(endpoint)
}

export default {
  dashboard: cachedDashboardApi,
  user: cachedUserApi,
  utils: cacheUtils
}
