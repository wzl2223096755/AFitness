import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { PerformanceUtils } from './performance.js'

describe('PerformanceUtils', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    PerformanceUtils.cache.clear()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('debounce', () => {
    it('should delay function execution', () => {
      const fn = vi.fn()
      const debouncedFn = PerformanceUtils.debounce(fn, 100)
      
      debouncedFn()
      expect(fn).not.toHaveBeenCalled()
      
      vi.advanceTimersByTime(100)
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should reset timer on subsequent calls', () => {
      const fn = vi.fn()
      const debouncedFn = PerformanceUtils.debounce(fn, 100)
      
      debouncedFn()
      vi.advanceTimersByTime(50)
      debouncedFn()
      vi.advanceTimersByTime(50)
      
      expect(fn).not.toHaveBeenCalled()
      
      vi.advanceTimersByTime(50)
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should pass arguments to the debounced function', () => {
      const fn = vi.fn()
      const debouncedFn = PerformanceUtils.debounce(fn, 100)
      
      debouncedFn('arg1', 'arg2')
      vi.advanceTimersByTime(100)
      
      expect(fn).toHaveBeenCalledWith('arg1', 'arg2')
    })
  })

  describe('throttle', () => {
    it('should execute function immediately on first call', () => {
      const fn = vi.fn()
      const throttledFn = PerformanceUtils.throttle(fn, 100)
      
      throttledFn()
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should prevent execution within delay period', () => {
      const fn = vi.fn()
      const throttledFn = PerformanceUtils.throttle(fn, 100)
      
      throttledFn()
      throttledFn()
      throttledFn()
      
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should allow execution after delay period', () => {
      const fn = vi.fn()
      const throttledFn = PerformanceUtils.throttle(fn, 100)
      
      throttledFn()
      vi.advanceTimersByTime(100)
      throttledFn()
      
      expect(fn).toHaveBeenCalledTimes(2)
    })
  })

  describe('cache', () => {
    describe('setCache and getCache', () => {
      it('should store and retrieve cached values', () => {
        PerformanceUtils.setCache('key1', { data: 'test' })
        
        const result = PerformanceUtils.getCache('key1')
        expect(result).toEqual({ data: 'test' })
      })

      it('should return null for non-existent keys', () => {
        const result = PerformanceUtils.getCache('nonexistent')
        expect(result).toBeNull()
      })

      it('should return null for expired cache', () => {
        PerformanceUtils.setCache('key1', 'value', 100)
        
        vi.advanceTimersByTime(150)
        
        const result = PerformanceUtils.getCache('key1')
        expect(result).toBeNull()
      })

      it('should use default TTL of 5 minutes', () => {
        PerformanceUtils.setCache('key1', 'value')
        
        vi.advanceTimersByTime(4 * 60 * 1000) // 4 minutes
        expect(PerformanceUtils.getCache('key1')).toBe('value')
        
        vi.advanceTimersByTime(2 * 60 * 1000) // 2 more minutes (total 6)
        expect(PerformanceUtils.getCache('key1')).toBeNull()
      })
    })

    describe('clearExpiredCache', () => {
      it('should remove expired entries', () => {
        PerformanceUtils.setCache('key1', 'value1', 100)
        PerformanceUtils.setCache('key2', 'value2', 1000)
        
        vi.advanceTimersByTime(150)
        PerformanceUtils.clearExpiredCache()
        
        expect(PerformanceUtils.getCache('key1')).toBeNull()
        expect(PerformanceUtils.getCache('key2')).toBe('value2')
      })
    })
  })

  describe('calculateVisibleItems', () => {
    it('should calculate correct visible items', () => {
      const items = Array.from({ length: 100 }, (_, i) => ({ id: i }))
      
      const result = PerformanceUtils.calculateVisibleItems(items, 0, 500, 50)
      
      expect(result.startIndex).toBe(0)
      // endIndex = min(0 + ceil(500/50) + 1, 99) = min(11, 99) = 11
      expect(result.endIndex).toBe(11)
      expect(result.visibleItems.length).toBe(12)
      expect(result.offsetY).toBe(0)
    })

    it('should handle scrolled position', () => {
      const items = Array.from({ length: 100 }, (_, i) => ({ id: i }))
      
      const result = PerformanceUtils.calculateVisibleItems(items, 250, 500, 50)
      
      expect(result.startIndex).toBe(5)
      expect(result.offsetY).toBe(250)
    })

    it('should not exceed array bounds', () => {
      const items = Array.from({ length: 10 }, (_, i) => ({ id: i }))
      
      const result = PerformanceUtils.calculateVisibleItems(items, 0, 1000, 50)
      
      expect(result.endIndex).toBe(9)
    })
  })

  describe('paginateList', () => {
    it('should paginate list correctly', () => {
      const list = Array.from({ length: 55 }, (_, i) => i)
      
      const result = PerformanceUtils.paginateList(list, 20)
      
      expect(result.totalItems).toBe(55)
      expect(result.totalPages).toBe(3)
      expect(result.pageSize).toBe(20)
      expect(result.pages[0].length).toBe(20)
      expect(result.pages[2].length).toBe(15)
    })

    it('should use default page size of 20', () => {
      const list = Array.from({ length: 50 }, (_, i) => i)
      
      const result = PerformanceUtils.paginateList(list)
      
      expect(result.pageSize).toBe(20)
      expect(result.totalPages).toBe(3)
    })

    it('should handle empty list', () => {
      const result = PerformanceUtils.paginateList([])
      
      expect(result.totalItems).toBe(0)
      expect(result.totalPages).toBe(0)
      expect(result.pages).toEqual([])
    })
  })
})
