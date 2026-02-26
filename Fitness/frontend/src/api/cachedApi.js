/**
 * 用户端缓存API模块
 * 简化版本，用于 GitHub Pages 演示
 */

import { get as getCache, set as setCache } from './apiCache.js'

// 缓存装饰器
export function cached(ttl = 300000) {
  return function(target, propertyKey, descriptor) {
    const originalMethod = descriptor.value
    
    descriptor.value = async function(...args) {
      const cacheKey = `${propertyKey}_${JSON.stringify(args)}`
      const cached = getCache(cacheKey)
      
      if (cached) {
        return cached
      }
      
      const result = await originalMethod.apply(this, args)
      setCache(cacheKey, result, ttl)
      return result
    }
    
    return descriptor
  }
}

// 简单的缓存函数
export function withCache(fn, keyGenerator, ttl = 300000) {
  return async function(...args) {
    const cacheKey = keyGenerator(...args)
    const cached = getCache(cacheKey)
    
    if (cached) {
      return cached
    }
    
    const result = await fn(...args)
    setCache(cacheKey, result, ttl)
    return result
  }
}

// 默认导出
export default {
  cached,
  withCache
}
