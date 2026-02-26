/**
 * 用户端性能工具
 * 简化版本，用于 GitHub Pages 演示
 */

// 性能计时
export function startTimer(name) {
  console.time(name)
  return () => {
    console.timeEnd(name)
  }
}

// 防抖函数
export function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 节流函数
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 简单的性能监控
export function measurePerformance(name, fn) {
  return async function(...args) {
    const start = performance.now()
    const result = await fn.apply(this, args)
    const end = performance.now()
    console.log(`[Performance] ${name}: ${(end - start).toFixed(2)}ms`)
    return result
  }
}
