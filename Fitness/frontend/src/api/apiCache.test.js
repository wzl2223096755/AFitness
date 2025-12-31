/**
 * API缓存模块测试
 */
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { 
  apiCacheManager, 
  cachedRequest, 
  invalidateCache, 
  invalidateAllCache,
  cacheConfig 
} from './apiCache.js'

describe('ApiCacheManager', () => {
  beforeEach(() => {
    // 清除所有缓存
    apiCacheManager.clear()
  })

  describe('generateKey', () => {
    it('should generate consistent keys for same endpoint and params', () => {
      const key1 = apiCacheManager.generateKey('/api/test', { a: 1, b: 2 })
      const key2 = apiCacheManager.generateKey('/api/test', { b: 2, a: 1 })
      expect(key1).toBe(key2)
    })

    it('should filter out _t timestamp parameter', () => {
      const key1 = apiCacheManager.generateKey('/api/test', { a: 1 })
      const key2 = apiCacheManager.generateKey('/api/test', { a: 1, _t: Date.now() })
      expect(key1).toBe(key2)
    })

    it('should generate different keys for different endpoints', () => {
      const key1 = apiCacheManager.generateKey('/api/test1', {})
      const key2 = apiCacheManager.generateKey('/api/test2', {})
      expect(key1).not.toBe(key2)
    })
  })

  describe('set and get', () => {
    it('should store and retrieve cached data', () => {
      const testData = { code: 200, data: { test: 'value' } }
      apiCacheManager.set('test-key', testData, { ttl: 60000 })
      
      const cached = apiCacheManager.get('test-key')
      expect(cached).not.toBeNull()
      expect(cached.data).toEqual(testData)
    })

    it('should return null for non-existent key', () => {
      const cached = apiCacheManager.get('non-existent')
      expect(cached).toBeNull()
    })
  })

  describe('isExpired', () => {
    it('should return true for null entry', () => {
      expect(apiCacheManager.isExpired(null)).toBe(true)
    })

    it('should return false for fresh cache', () => {
      apiCacheManager.set('test-key', { data: 'test' }, { ttl: 60000 })
      const entry = apiCacheManager.get('test-key')
      expect(apiCacheManager.isExpired(entry)).toBe(false)
    })

    it('should return true for expired cache', () => {
      const entry = {
        data: { test: 'value' },
        timestamp: Date.now() - 120000, // 2 minutes ago
        ttl: 60000, // 1 minute TTL
        staleWhileRevalidate: false
      }
      expect(apiCacheManager.isExpired(entry)).toBe(true)
    })
  })

  describe('isStale', () => {
    it('should return false for null entry', () => {
      expect(apiCacheManager.isStale(null)).toBe(false)
    })

    it('should return true for expired entry with staleWhileRevalidate', () => {
      const entry = {
        data: { test: 'value' },
        timestamp: Date.now() - 120000,
        ttl: 60000,
        staleWhileRevalidate: true
      }
      expect(apiCacheManager.isStale(entry)).toBe(true)
    })

    it('should return false for expired entry without staleWhileRevalidate', () => {
      const entry = {
        data: { test: 'value' },
        timestamp: Date.now() - 120000,
        ttl: 60000,
        staleWhileRevalidate: false
      }
      expect(apiCacheManager.isStale(entry)).toBe(false)
    })
  })

  describe('delete', () => {
    it('should remove cached entry', () => {
      apiCacheManager.set('test-key', { data: 'test' })
      apiCacheManager.delete('test-key')
      expect(apiCacheManager.get('test-key')).toBeNull()
    })
  })

  describe('clear', () => {
    it('should remove all cached entries', () => {
      apiCacheManager.set('key1', { data: 'test1' })
      apiCacheManager.set('key2', { data: 'test2' })
      apiCacheManager.clear()
      expect(apiCacheManager.get('key1')).toBeNull()
      expect(apiCacheManager.get('key2')).toBeNull()
    })
  })

  describe('getStats', () => {
    it('should return correct statistics', () => {
      apiCacheManager.set('fresh', { data: 'test' }, { ttl: 60000 })
      
      const stats = apiCacheManager.getStats()
      expect(stats.total).toBe(1)
      expect(stats.fresh).toBe(1)
    })
  })
})

describe('cachedRequest', () => {
  beforeEach(() => {
    apiCacheManager.clear()
  })

  it('should call request function when no cache exists', async () => {
    const mockFn = vi.fn().mockResolvedValue({ code: 200, data: 'test' })
    
    const result = await cachedRequest(mockFn, 'test-key', { ttl: 60000 })
    
    expect(mockFn).toHaveBeenCalledTimes(1)
    expect(result).toEqual({ code: 200, data: 'test' })
  })

  it('should return cached data when cache is fresh', async () => {
    const mockFn = vi.fn().mockResolvedValue({ code: 200, data: 'test' })
    
    // First call - should hit the API
    await cachedRequest(mockFn, 'test-key', { ttl: 60000 })
    
    // Second call - should return cached data
    const result = await cachedRequest(mockFn, 'test-key', { ttl: 60000 })
    
    expect(mockFn).toHaveBeenCalledTimes(1)
    expect(result).toEqual({ code: 200, data: 'test' })
  })

  it('should force refresh when forceRefresh is true', async () => {
    const mockFn = vi.fn().mockResolvedValue({ code: 200, data: 'test' })
    
    // First call
    await cachedRequest(mockFn, 'test-key', { ttl: 60000 })
    
    // Second call with forceRefresh
    await cachedRequest(mockFn, 'test-key', { ttl: 60000, forceRefresh: true })
    
    expect(mockFn).toHaveBeenCalledTimes(2)
  })

  it('should return stale data and revalidate in background', async () => {
    const mockFn = vi.fn()
      .mockResolvedValueOnce({ code: 200, data: 'old' })
      .mockResolvedValueOnce({ code: 200, data: 'new' })
    
    // First call
    await cachedRequest(mockFn, 'test-key', { ttl: 100, staleWhileRevalidate: true })
    
    // Wait for cache to expire
    await new Promise(resolve => setTimeout(resolve, 150))
    
    // Second call - should return stale data immediately
    const result = await cachedRequest(mockFn, 'test-key', { ttl: 100, staleWhileRevalidate: true })
    
    expect(result).toEqual({ code: 200, data: 'old' })
    
    // Wait for background revalidation
    await new Promise(resolve => setTimeout(resolve, 50))
    
    // Third call - should return new data
    const newResult = await cachedRequest(mockFn, 'test-key', { ttl: 100, staleWhileRevalidate: true })
    expect(newResult).toEqual({ code: 200, data: 'new' })
  })
})

describe('invalidateCache', () => {
  beforeEach(() => {
    apiCacheManager.clear()
  })

  it('should invalidate cache for specific endpoint', () => {
    apiCacheManager.set('/api/v1/dashboard:{}', { data: 'dashboard' })
    apiCacheManager.set('/api/v1/user:{}', { data: 'user' })
    
    invalidateCache('/api/v1/dashboard')
    
    expect(apiCacheManager.get('/api/v1/dashboard:{}')).toBeNull()
    expect(apiCacheManager.get('/api/v1/user:{}')).not.toBeNull()
  })
})

describe('invalidateAllCache', () => {
  beforeEach(() => {
    apiCacheManager.clear()
  })

  it('should invalidate all cache', () => {
    apiCacheManager.set('key1', { data: 'test1' })
    apiCacheManager.set('key2', { data: 'test2' })
    
    invalidateAllCache()
    
    expect(apiCacheManager.get('key1')).toBeNull()
    expect(apiCacheManager.get('key2')).toBeNull()
  })
})

describe('cacheConfig', () => {
  it('should have correct default strategies', () => {
    expect(cacheConfig.strategies.dashboard.ttl).toBe(5 * 60 * 1000)
    expect(cacheConfig.strategies.dashboard.staleWhileRevalidate).toBe(true)
    
    expect(cacheConfig.strategies.userProfile.ttl).toBe(10 * 60 * 1000)
    expect(cacheConfig.strategies.userProfile.staleWhileRevalidate).toBe(true)
    
    expect(cacheConfig.strategies.trainingRecords.ttl).toBe(2 * 60 * 1000)
    expect(cacheConfig.strategies.nutritionGoals.staleWhileRevalidate).toBe(false)
  })
})
