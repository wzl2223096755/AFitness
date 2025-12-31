/**
 * 静态资源优化工具测试
 */
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import {
  lazyLoadConfig,
  createLazyLoadObserver,
  initLazyLoad,
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
  fontOptimizationConfig
} from './resourceOptimization'

// Mock IntersectionObserver
class MockIntersectionObserver {
  constructor(callback, options) {
    this.callback = callback
    this.options = options
    this.observedElements = new Set()
  }
  
  observe(element) {
    this.observedElements.add(element)
  }
  
  unobserve(element) {
    this.observedElements.delete(element)
  }
  
  disconnect() {
    this.observedElements.clear()
  }
  
  // 模拟触发交叉
  triggerIntersect(entries) {
    this.callback(entries, this)
  }
}

describe('resourceOptimization', () => {
  let originalIntersectionObserver

  beforeEach(() => {
    // 保存原始IntersectionObserver
    originalIntersectionObserver = global.IntersectionObserver
    // 使用Mock
    global.IntersectionObserver = MockIntersectionObserver
    
    // 清理DOM
    document.head.innerHTML = ''
    document.body.innerHTML = ''
  })

  afterEach(() => {
    // 恢复原始IntersectionObserver
    global.IntersectionObserver = originalIntersectionObserver
    vi.restoreAllMocks()
  })

  describe('lazyLoadConfig', () => {
    it('should have default configuration', () => {
      expect(lazyLoadConfig.rootMargin).toBe('50px 0px')
      expect(lazyLoadConfig.threshold).toBe(0.01)
      expect(lazyLoadConfig.placeholder).toContain('data:image/svg+xml')
    })
  })

  describe('PreloadType', () => {
    it('should have all resource types', () => {
      expect(PreloadType.SCRIPT).toBe('script')
      expect(PreloadType.STYLE).toBe('style')
      expect(PreloadType.IMAGE).toBe('image')
      expect(PreloadType.FONT).toBe('font')
      expect(PreloadType.FETCH).toBe('fetch')
    })
  })

  describe('createLazyLoadObserver', () => {
    it('should create an IntersectionObserver', () => {
      const observer = createLazyLoadObserver()
      expect(observer).toBeInstanceOf(MockIntersectionObserver)
    })

    it('should accept custom options', () => {
      const observer = createLazyLoadObserver({
        rootMargin: '100px 0px',
        threshold: 0.5
      })
      expect(observer).toBeInstanceOf(MockIntersectionObserver)
    })
  })

  describe('initLazyLoad', () => {
    it('should observe elements with data-src', () => {
      // 创建测试元素
      const img = document.createElement('img')
      img.dataset.src = 'test.jpg'
      document.body.appendChild(img)

      const observer = initLazyLoad('[data-src]')
      expect(observer).toBeInstanceOf(MockIntersectionObserver)
      expect(img.classList.contains('lazy-image')).toBe(true)
    })

    it('should observe elements with data-bg-src', () => {
      const div = document.createElement('div')
      div.dataset.bgSrc = 'bg.jpg'
      document.body.appendChild(div)

      const observer = initLazyLoad('[data-bg-src]')
      expect(observer).toBeInstanceOf(MockIntersectionObserver)
      expect(div.classList.contains('lazy-image')).toBe(true)
    })
  })

  describe('preloadResource', () => {
    it('should create preload link for script', () => {
      preloadResource('/test.js', PreloadType.SCRIPT)
      
      const link = document.querySelector('link[rel="preload"][href="/test.js"]')
      expect(link).not.toBeNull()
      expect(link.as).toBe('script')
    })

    it('should create preload link for style', () => {
      preloadResource('/test.css', PreloadType.STYLE)
      
      const link = document.querySelector('link[rel="preload"][href="/test.css"]')
      expect(link).not.toBeNull()
      expect(link.as).toBe('style')
    })

    it('should create preload link for image', () => {
      preloadResource('/test.png', PreloadType.IMAGE)
      
      const link = document.querySelector('link[rel="preload"][href="/test.png"]')
      expect(link).not.toBeNull()
      expect(link.as).toBe('image')
    })

    it('should create preload link for font with crossorigin', () => {
      preloadResource('/test.woff2', PreloadType.FONT)
      
      const link = document.querySelector('link[rel="preload"][href="/test.woff2"]')
      expect(link).not.toBeNull()
      expect(link.as).toBe('font')
      expect(link.crossOrigin).toBe('anonymous')
    })

    it('should not duplicate preload links', () => {
      preloadResource('/test.js', PreloadType.SCRIPT)
      preloadResource('/test.js', PreloadType.SCRIPT)
      
      const links = document.querySelectorAll('link[rel="preload"][href="/test.js"]')
      expect(links.length).toBe(1)
    })

    it('should apply custom options', () => {
      preloadResource('/test.js', PreloadType.SCRIPT, { crossOrigin: 'use-credentials' })
      
      const link = document.querySelector('link[rel="preload"][href="/test.js"]')
      expect(link.crossOrigin).toBe('use-credentials')
    })
  })

  describe('prefetchResource', () => {
    it('should create prefetch link', () => {
      prefetchResource('/prefetch.js')
      
      const link = document.querySelector('link[rel="prefetch"][href="/prefetch.js"]')
      expect(link).not.toBeNull()
    })

    it('should not duplicate prefetch links', () => {
      prefetchResource('/prefetch.js')
      prefetchResource('/prefetch.js')
      
      const links = document.querySelectorAll('link[rel="prefetch"][href="/prefetch.js"]')
      expect(links.length).toBe(1)
    })
  })

  describe('preconnect', () => {
    it('should create preconnect link', () => {
      preconnect('https://example.com')
      
      const link = document.querySelector('link[rel="preconnect"][href="https://example.com"]')
      expect(link).not.toBeNull()
    })

    it('should add crossorigin when specified', () => {
      preconnect('https://fonts.gstatic.com', true)
      
      const link = document.querySelector('link[rel="preconnect"][href="https://fonts.gstatic.com"]')
      expect(link).not.toBeNull()
      expect(link.crossOrigin).toBe('anonymous')
    })

    it('should not duplicate preconnect links', () => {
      preconnect('https://example.com')
      preconnect('https://example.com')
      
      const links = document.querySelectorAll('link[rel="preconnect"][href="https://example.com"]')
      expect(links.length).toBe(1)
    })
  })

  describe('dnsPrefetch', () => {
    it('should create dns-prefetch link', () => {
      dnsPrefetch('https://example.com')
      
      const link = document.querySelector('link[rel="dns-prefetch"][href="https://example.com"]')
      expect(link).not.toBeNull()
    })

    it('should not duplicate dns-prefetch links', () => {
      dnsPrefetch('https://example.com')
      dnsPrefetch('https://example.com')
      
      const links = document.querySelectorAll('link[rel="dns-prefetch"][href="https://example.com"]')
      expect(links.length).toBe(1)
    })
  })

  describe('preloadResources', () => {
    it('should preload multiple resources from strings', () => {
      preloadResources(['/test.js', '/test.css', '/test.png'])
      
      expect(document.querySelector('link[href="/test.js"]')).not.toBeNull()
      expect(document.querySelector('link[href="/test.css"]')).not.toBeNull()
      expect(document.querySelector('link[href="/test.png"]')).not.toBeNull()
    })

    it('should preload resources from objects', () => {
      preloadResources([
        { href: '/custom.js', type: PreloadType.SCRIPT },
        { href: '/custom.woff2', type: PreloadType.FONT }
      ])
      
      const scriptLink = document.querySelector('link[href="/custom.js"]')
      const fontLink = document.querySelector('link[href="/custom.woff2"]')
      
      expect(scriptLink).not.toBeNull()
      expect(fontLink).not.toBeNull()
      expect(fontLink.crossOrigin).toBe('anonymous')
    })
  })

  describe('fontOptimizationConfig', () => {
    it('should have default configuration', () => {
      expect(fontOptimizationConfig.display).toBe('swap')
      expect(fontOptimizationConfig.preloadFonts).toEqual([])
      expect(fontOptimizationConfig.subset).toBe('latin,latin-ext')
    })
  })

  describe('optimizeFontLoading', () => {
    it('should add font-display style', () => {
      optimizeFontLoading()
      
      const style = document.getElementById('font-display-style')
      expect(style).not.toBeNull()
      expect(style.textContent).toContain('font-display: swap')
    })

    it('should preload specified fonts', () => {
      optimizeFontLoading({
        preloadFonts: [
          { url: '/font.woff2', type: 'font/woff2' }
        ]
      })
      
      const link = document.querySelector('link[href="/font.woff2"]')
      expect(link).not.toBeNull()
      expect(link.as).toBe('font')
    })

    it('should not duplicate font-display style', () => {
      optimizeFontLoading()
      optimizeFontLoading()
      
      const styles = document.querySelectorAll('#font-display-style')
      expect(styles.length).toBe(1)
    })
  })

  describe('isFontLoaded', () => {
    it('should return true when Font Loading API is not supported', async () => {
      // 模拟不支持Font Loading API
      const originalFonts = document.fonts
      delete document.fonts
      
      const result = await isFontLoaded('Inter')
      expect(result).toBe(true)
      
      // 恢复
      document.fonts = originalFonts
    })
  })

  describe('waitForFonts', () => {
    it('should resolve when Font Loading API is not supported', async () => {
      const originalFonts = document.fonts
      delete document.fonts
      
      await expect(waitForFonts(['Inter'])).resolves.toBeUndefined()
      
      document.fonts = originalFonts
    })
  })

  describe('ResourcePreloader', () => {
    let preloader

    beforeEach(() => {
      preloader = new ResourcePreloader()
    })

    it('should preload critical resources', () => {
      preloader.preloadCritical([
        { href: '/critical.js', type: PreloadType.SCRIPT }
      ])
      
      const link = document.querySelector('link[href="/critical.js"]')
      expect(link).not.toBeNull()
    })

    it('should not duplicate critical resources', () => {
      preloader.preloadCritical([
        { href: '/critical.js', type: PreloadType.SCRIPT }
      ])
      preloader.preloadCritical([
        { href: '/critical.js', type: PreloadType.SCRIPT }
      ])
      
      const links = document.querySelectorAll('link[href="/critical.js"]')
      expect(links.length).toBe(1)
    })

    it('should preconnect to origins', () => {
      preloader.preconnectOrigins(['https://api.example.com'])
      
      const preconnectLink = document.querySelector('link[rel="preconnect"][href="https://api.example.com"]')
      const dnsPrefetchLink = document.querySelector('link[rel="dns-prefetch"][href="https://api.example.com"]')
      
      expect(preconnectLink).not.toBeNull()
      expect(dnsPrefetchLink).not.toBeNull()
    })

    it('should prefetch non-critical resources', async () => {
      // 模拟requestIdleCallback
      vi.stubGlobal('requestIdleCallback', (cb) => setTimeout(cb, 0))
      
      preloader.prefetchNonCritical(['/non-critical.js'])
      
      // 等待idle callback执行
      await new Promise(resolve => setTimeout(resolve, 10))
      
      const link = document.querySelector('link[rel="prefetch"][href="/non-critical.js"]')
      expect(link).not.toBeNull()
      
      vi.unstubAllGlobals()
    })
  })

  describe('resourcePreloader (singleton)', () => {
    it('should be a ResourcePreloader instance', () => {
      expect(resourcePreloader).toBeInstanceOf(ResourcePreloader)
    })
  })

  describe('initResourceOptimization', () => {
    it('should initialize all optimizations', () => {
      const result = initResourceOptimization({
        lazyLoadSelector: '[data-src]',
        preconnectOrigins: ['https://api.example.com'],
        preloadFonts: []
      })
      
      expect(result.lazyObserver).toBeInstanceOf(MockIntersectionObserver)
      expect(result.resourcePreloader).toBeInstanceOf(ResourcePreloader)
    })

    it('should preconnect to specified origins', () => {
      initResourceOptimization({
        preconnectOrigins: ['https://fonts.googleapis.com']
      })
      
      const link = document.querySelector('link[rel="preconnect"][href="https://fonts.googleapis.com"]')
      expect(link).not.toBeNull()
    })
  })
})
