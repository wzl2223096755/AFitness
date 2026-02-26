/**
 * 用户端API缓存模块
 * 简化版本，用于 GitHub Pages 演示
 */

// 简单的内存缓存
const cache = new Map()

export function set(key, value, ttl = 300000) { // 默认5分钟
  cache.set(key, {
    value,
    expires: Date.now() + ttl
  })
}

export function get(key) {
  const item = cache.get(key)
  if (!item) return null
  
  if (Date.now() > item.expires) {
    cache.delete(key)
    return null
  }
  
  return item.value
}

export function del(key) {
  cache.delete(key)
}

export function clear() {
  cache.clear()
}

// 默认导出
export default {
  set,
  get,
  delete: del,
  clear
}
