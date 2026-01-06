/**
 * 性能优化工具类
 */
export class PerformanceUtils {
  /**
   * 防抖函数
   * @param {Function} func - 要防抖的函数
   * @param {number} delay - 延迟时间（毫秒）
   * @returns {Function} 防抖后的函数
   */
  static debounce(func, delay) {
    let timeoutId
    return function (...args) {
      clearTimeout(timeoutId)
      timeoutId = setTimeout(() => func.apply(this, args), delay)
    }
  }

  /**
   * 节流函数
   * @param {Function} func - 要节流的函数
   * @param {number} delay - 延迟时间（毫秒）
   * @returns {Function} 节流后的函数
   */
  static throttle(func, delay) {
    let lastCall = 0
    return function (...args) {
      const now = Date.now()
      if (now - lastCall >= delay) {
        lastCall = now
        return func.apply(this, args)
      }
    }
  }

  /**
   * 内存缓存
   */
  static cache = new Map()

  /**
   * 设置缓存
   * @param {string} key - 缓存键
   * @param {any} value - 缓存值
   * @param {number} ttl - 生存时间（毫秒）
   */
  static setCache(key, value, ttl = 300000) {
    this.cache.set(key, {
      value,
      expiry: Date.now() + ttl
    })
  }

  /**
   * 获取缓存
   * @param {string} key - 缓存键
   * @returns {any} 缓存值或null
   */
  static getCache(key) {
    const item = this.cache.get(key)
    if (!item) return null
    
    if (Date.now() > item.expiry) {
      this.cache.delete(key)
      return null
    }
    
    return item.value
  }

  /**
   * 清除过期缓存
   */
  static clearExpiredCache() {
    const now = Date.now()
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expiry) {
        this.cache.delete(key)
      }
    }
  }

  /**
   * 图片懒加载
   * @param {string} selector - 图片选择器
   */
  static lazyLoadImages(selector = 'img[data-src]') {
    const images = document.querySelectorAll(selector)
    
    const imageObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target
          img.src = img.dataset.src
          img.removeAttribute('data-src')
          observer.unobserve(img)
        }
      })
    })

    images.forEach(img => imageObserver.observe(img))
  }

  /**
   * 虚拟滚动计算可见项
   * @param {Array} items - 所有项目
   * @param {number} scrollTop - 滚动位置
   * @param {number} containerHeight - 容器高度
   * @param {number} itemHeight - 项目高度
   * @returns {Object} 可见项目信息
   */
  static calculateVisibleItems(items, scrollTop, containerHeight, itemHeight) {
    const startIndex = Math.floor(scrollTop / itemHeight)
    const endIndex = Math.min(
      startIndex + Math.ceil(containerHeight / itemHeight) + 1,
      items.length - 1
    )
    
    return {
      startIndex,
      endIndex,
      visibleItems: items.slice(startIndex, endIndex + 1),
      offsetY: startIndex * itemHeight
    }
  }

  /**
   * 批量DOM更新
   * @param {Function} updateFn - 更新函数
   */
  static batchDOMUpdate(updateFn) {
    requestAnimationFrame(() => {
      updateFn()
    })
  }

  /**
   * 预加载资源
   * @param {Array} resources - 资源URL数组
   */
  static preloadResources(resources) {
    resources.forEach(resource => {
      if (resource.endsWith('.js')) {
        const link = document.createElement('link')
        link.rel = 'preload'
        link.as = 'script'
        link.href = resource
        document.head.appendChild(link)
      } else if (resource.endsWith('.css')) {
        const link = document.createElement('link')
        link.rel = 'preload'
        link.as = 'style'
        link.href = resource
        document.head.appendChild(link)
      } else if (resource.match(/\.(jpg|jpeg|png|gif|webp)$/)) {
        const link = document.createElement('link')
        link.rel = 'preload'
        link.as = 'image'
        link.href = resource
        document.head.appendChild(link)
      }
    })
  }

  /**
   * 监控性能指标
   */
  static monitorPerformance() {
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0]
      console.log('页面加载时间:', navigation.loadEventEnd - navigation.fetchStart, 'ms')
    })

    if (performance.memory) {
      setInterval(() => {
        console.log('内存使用:', {
          used: Math.round(performance.memory.usedJSHeapSize / 1048576) + 'MB',
          total: Math.round(performance.memory.totalJSHeapSize / 1048576) + 'MB',
          limit: Math.round(performance.memory.jsHeapSizeLimit / 1048576) + 'MB'
        })
      }, 30000)
    }
  }

  /**
   * 优化列表渲染性能
   * @param {Array} list - 列表数据
   * @param {number} pageSize - 每页大小
   * @returns {Object} 分页数据
   */
  static paginateList(list, pageSize = 20) {
    const pages = []
    for (let i = 0; i < list.length; i += pageSize) {
      pages.push(list.slice(i, i + pageSize))
    }
    return {
      pages,
      totalItems: list.length,
      totalPages: pages.length,
      pageSize
    }
  }
}

// 定期清理过期缓存
setInterval(() => {
  PerformanceUtils.clearExpiredCache()
}, 60000)
