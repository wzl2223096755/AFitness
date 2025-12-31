/**
 * 静态资源优化工具
 * 提供图片懒加载、资源预加载、字体优化等功能
 */

/**
 * 图片懒加载配置
 */
export const lazyLoadConfig = {
  // 根元素边距，提前加载
  rootMargin: '50px 0px',
  // 可见阈值
  threshold: 0.01,
  // 占位图
  placeholder: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%231a1a2e" width="100" height="100"/%3E%3C/svg%3E'
}

/**
 * 创建图片懒加载观察器
 * @param {Object} options - 配置选项
 * @returns {IntersectionObserver} 观察器实例
 */
export function createLazyLoadObserver(options = {}) {
  const config = { ...lazyLoadConfig, ...options }
  
  return new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const element = entry.target
        
        // 处理图片元素
        if (element.tagName === 'IMG') {
          loadImage(element)
        }
        // 处理背景图片
        else if (element.dataset.bgSrc) {
          loadBackgroundImage(element)
        }
        
        observer.unobserve(element)
      }
    })
  }, {
    rootMargin: config.rootMargin,
    threshold: config.threshold
  })
}

/**
 * 加载图片
 * @param {HTMLImageElement} img - 图片元素
 */
function loadImage(img) {
  const src = img.dataset.src
  const srcset = img.dataset.srcset
  
  if (src) {
    // 创建临时图片预加载
    const tempImg = new Image()
    
    tempImg.onload = () => {
      img.src = src
      if (srcset) {
        img.srcset = srcset
      }
      img.classList.add('lazy-loaded')
      img.classList.remove('lazy-image')
      img.removeAttribute('data-src')
      img.removeAttribute('data-srcset')
    }
    
    tempImg.onerror = () => {
      img.classList.add('lazy-error')
      console.warn('图片加载失败:', src)
    }
    
    tempImg.src = src
  }
}

/**
 * 加载背景图片
 * @param {HTMLElement} element - 元素
 */
function loadBackgroundImage(element) {
  const bgSrc = element.dataset.bgSrc
  
  if (bgSrc) {
    const tempImg = new Image()
    
    tempImg.onload = () => {
      element.style.backgroundImage = `url(${bgSrc})`
      element.classList.add('lazy-loaded')
      element.removeAttribute('data-bg-src')
    }
    
    tempImg.onerror = () => {
      element.classList.add('lazy-error')
      console.warn('背景图片加载失败:', bgSrc)
    }
    
    tempImg.src = bgSrc
  }
}

/**
 * 初始化图片懒加载
 * @param {string} selector - 选择器
 * @param {Object} options - 配置选项
 * @returns {IntersectionObserver} 观察器实例
 */
export function initLazyLoad(selector = '[data-src], [data-bg-src]', options = {}) {
  const observer = createLazyLoadObserver(options)
  const elements = document.querySelectorAll(selector)
  
  elements.forEach(element => {
    // 添加懒加载样式类
    element.classList.add('lazy-image')
    observer.observe(element)
  })
  
  return observer
}

/**
 * 资源预加载类型
 */
export const PreloadType = {
  SCRIPT: 'script',
  STYLE: 'style',
  IMAGE: 'image',
  FONT: 'font',
  FETCH: 'fetch'
}

/**
 * 预加载资源
 * @param {string} href - 资源URL
 * @param {string} type - 资源类型
 * @param {Object} options - 额外选项
 */
export function preloadResource(href, type, options = {}) {
  // 检查是否已存在
  const existing = document.querySelector(`link[href="${href}"][rel="preload"]`)
  if (existing) return existing
  
  const link = document.createElement('link')
  link.rel = 'preload'
  link.href = href
  link.as = type
  
  // 字体需要crossorigin属性
  if (type === PreloadType.FONT) {
    link.crossOrigin = 'anonymous'
  }
  
  // 应用额外选项
  if (options.crossOrigin) {
    link.crossOrigin = options.crossOrigin
  }
  if (options.type) {
    link.type = options.type
  }
  
  document.head.appendChild(link)
  return link
}

/**
 * 预取资源（低优先级）
 * @param {string} href - 资源URL
 */
export function prefetchResource(href) {
  const existing = document.querySelector(`link[href="${href}"][rel="prefetch"]`)
  if (existing) return existing
  
  const link = document.createElement('link')
  link.rel = 'prefetch'
  link.href = href
  document.head.appendChild(link)
  return link
}

/**
 * 预连接到域名
 * @param {string} origin - 域名
 * @param {boolean} crossOrigin - 是否跨域
 */
export function preconnect(origin, crossOrigin = false) {
  const existing = document.querySelector(`link[href="${origin}"][rel="preconnect"]`)
  if (existing) return existing
  
  const link = document.createElement('link')
  link.rel = 'preconnect'
  link.href = origin
  if (crossOrigin) {
    link.crossOrigin = 'anonymous'
  }
  document.head.appendChild(link)
  return link
}

/**
 * DNS预解析
 * @param {string} origin - 域名
 */
export function dnsPrefetch(origin) {
  const existing = document.querySelector(`link[href="${origin}"][rel="dns-prefetch"]`)
  if (existing) return existing
  
  const link = document.createElement('link')
  link.rel = 'dns-prefetch'
  link.href = origin
  document.head.appendChild(link)
  return link
}

/**
 * 批量预加载资源
 * @param {Array} resources - 资源配置数组
 */
export function preloadResources(resources) {
  resources.forEach(resource => {
    if (typeof resource === 'string') {
      // 自动检测类型
      const type = detectResourceType(resource)
      preloadResource(resource, type)
    } else {
      preloadResource(resource.href, resource.type, resource.options)
    }
  })
}

/**
 * 检测资源类型
 * @param {string} url - 资源URL
 * @returns {string} 资源类型
 */
function detectResourceType(url) {
  const ext = url.split('.').pop().toLowerCase().split('?')[0]
  
  const typeMap = {
    js: PreloadType.SCRIPT,
    css: PreloadType.STYLE,
    jpg: PreloadType.IMAGE,
    jpeg: PreloadType.IMAGE,
    png: PreloadType.IMAGE,
    gif: PreloadType.IMAGE,
    webp: PreloadType.IMAGE,
    svg: PreloadType.IMAGE,
    woff: PreloadType.FONT,
    woff2: PreloadType.FONT,
    ttf: PreloadType.FONT,
    otf: PreloadType.FONT,
    eot: PreloadType.FONT
  }
  
  return typeMap[ext] || PreloadType.FETCH
}

/**
 * 字体加载优化配置
 */
export const fontOptimizationConfig = {
  // 字体显示策略
  display: 'swap',
  // 预加载的字体
  preloadFonts: [],
  // 字体子集
  subset: 'latin,latin-ext'
}

/**
 * 优化字体加载
 * @param {Object} options - 配置选项
 */
export function optimizeFontLoading(options = {}) {
  const config = { ...fontOptimizationConfig, ...options }
  
  // 预加载关键字体
  config.preloadFonts.forEach(font => {
    preloadResource(font.url, PreloadType.FONT, {
      type: font.type || 'font/woff2',
      crossOrigin: 'anonymous'
    })
  })
  
  // 添加字体显示策略样式
  if (!document.getElementById('font-display-style')) {
    const style = document.createElement('style')
    style.id = 'font-display-style'
    style.textContent = `
      @font-face {
        font-display: ${config.display};
      }
    `
    document.head.appendChild(style)
  }
}

/**
 * 检测字体是否已加载
 * @param {string} fontFamily - 字体名称
 * @returns {Promise<boolean>} 是否已加载
 */
export async function isFontLoaded(fontFamily) {
  if ('fonts' in document) {
    try {
      await document.fonts.load(`16px "${fontFamily}"`)
      return document.fonts.check(`16px "${fontFamily}"`)
    } catch (e) {
      return false
    }
  }
  return true // 不支持Font Loading API时假设已加载
}

/**
 * 等待字体加载完成
 * @param {Array<string>} fontFamilies - 字体名称数组
 * @param {number} timeout - 超时时间（毫秒）
 * @returns {Promise<void>}
 */
export async function waitForFonts(fontFamilies, timeout = 3000) {
  if (!('fonts' in document)) return
  
  const promises = fontFamilies.map(font => 
    document.fonts.load(`16px "${font}"`)
  )
  
  try {
    await Promise.race([
      Promise.all(promises),
      new Promise((_, reject) => 
        setTimeout(() => reject(new Error('字体加载超时')), timeout)
      )
    ])
  } catch (e) {
    console.warn('字体加载警告:', e.message)
  }
}

/**
 * 关键资源预加载管理器
 */
export class ResourcePreloader {
  constructor() {
    this.loadedResources = new Set()
    this.pendingResources = new Map()
  }
  
  /**
   * 预加载关键资源
   * @param {Array} resources - 资源列表
   */
  preloadCritical(resources) {
    resources.forEach(resource => {
      if (!this.loadedResources.has(resource.href)) {
        preloadResource(resource.href, resource.type, resource.options)
        this.loadedResources.add(resource.href)
      }
    })
  }
  
  /**
   * 预取非关键资源
   * @param {Array<string>} urls - URL列表
   */
  prefetchNonCritical(urls) {
    // 使用requestIdleCallback在空闲时预取
    const prefetch = () => {
      urls.forEach(url => {
        if (!this.loadedResources.has(url)) {
          prefetchResource(url)
          this.loadedResources.add(url)
        }
      })
    }
    
    if ('requestIdleCallback' in window) {
      requestIdleCallback(prefetch, { timeout: 2000 })
    } else {
      setTimeout(prefetch, 1000)
    }
  }
  
  /**
   * 预连接到API域名
   * @param {Array<string>} origins - 域名列表
   */
  preconnectOrigins(origins) {
    origins.forEach(origin => {
      preconnect(origin, true)
      dnsPrefetch(origin)
    })
  }
}

/**
 * 创建全局资源预加载器实例
 */
export const resourcePreloader = new ResourcePreloader()

/**
 * 初始化静态资源优化
 * @param {Object} options - 配置选项
 */
export function initResourceOptimization(options = {}) {
  const {
    lazyLoadSelector = '[data-src], [data-bg-src]',
    preloadFonts = [],
    preconnectOrigins = [],
    prefetchRoutes = []
  } = options
  
  // 初始化图片懒加载
  const lazyObserver = initLazyLoad(lazyLoadSelector)
  
  // 优化字体加载
  optimizeFontLoading({ preloadFonts })
  
  // 预连接到关键域名
  resourcePreloader.preconnectOrigins(preconnectOrigins)
  
  // 预取路由资源
  if (prefetchRoutes.length > 0) {
    resourcePreloader.prefetchNonCritical(prefetchRoutes)
  }
  
  return {
    lazyObserver,
    resourcePreloader
  }
}

export default {
  initLazyLoad,
  createLazyLoadObserver,
  preloadResource,
  prefetchResource,
  preconnect,
  dnsPrefetch,
  preloadResources,
  optimizeFontLoading,
  isFontLoaded,
  waitForFonts,
  ResourcePreloader,
  resourcePreloader,
  initResourceOptimization,
  PreloadType,
  lazyLoadConfig,
  fontOptimizationConfig
}
