import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { useLongPress, useDoubleTap } from './useMobileGestures'

describe('useMobileGestures', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('useLongPress', () => {
    it('should return event handlers', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress)
      
      expect(handlers.onMouseDown).toBeDefined()
      expect(handlers.onMouseUp).toBeDefined()
      expect(handlers.onMouseLeave).toBeDefined()
      expect(handlers.onTouchStart).toBeDefined()
      expect(handlers.onTouchEnd).toBeDefined()
      expect(handlers.onTouchCancel).toBeDefined()
    })

    it('should trigger callback after delay', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 500)
      
      const mockEvent = { target: document.createElement('div') }
      handlers.onMouseDown(mockEvent)
      
      expect(onLongPress).not.toHaveBeenCalled()
      
      vi.advanceTimersByTime(500)
      
      expect(onLongPress).toHaveBeenCalledWith(mockEvent)
    })

    it('should not trigger callback if released before delay', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 500)
      
      handlers.onMouseDown({})
      vi.advanceTimersByTime(300)
      handlers.onMouseUp()
      vi.advanceTimersByTime(300)
      
      expect(onLongPress).not.toHaveBeenCalled()
    })

    it('should cancel on mouse leave', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 500)
      
      handlers.onMouseDown({})
      vi.advanceTimersByTime(300)
      handlers.onMouseLeave()
      vi.advanceTimersByTime(300)
      
      expect(onLongPress).not.toHaveBeenCalled()
    })

    it('should work with touch events', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 500)
      
      const mockEvent = { target: document.createElement('div') }
      handlers.onTouchStart(mockEvent)
      
      vi.advanceTimersByTime(500)
      
      expect(onLongPress).toHaveBeenCalledWith(mockEvent)
    })

    it('should cancel on touch end', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 500)
      
      handlers.onTouchStart({})
      vi.advanceTimersByTime(300)
      handlers.onTouchEnd()
      vi.advanceTimersByTime(300)
      
      expect(onLongPress).not.toHaveBeenCalled()
    })

    it('should cancel on touch cancel', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 500)
      
      handlers.onTouchStart({})
      vi.advanceTimersByTime(300)
      handlers.onTouchCancel()
      vi.advanceTimersByTime(300)
      
      expect(onLongPress).not.toHaveBeenCalled()
    })

    it('should use custom delay', () => {
      const onLongPress = vi.fn()
      const handlers = useLongPress(onLongPress, 1000)
      
      handlers.onMouseDown({})
      vi.advanceTimersByTime(500)
      
      expect(onLongPress).not.toHaveBeenCalled()
      
      vi.advanceTimersByTime(500)
      
      expect(onLongPress).toHaveBeenCalled()
    })
  })

  describe('useDoubleTap', () => {
    it('should return tap handler', () => {
      const onDoubleTap = vi.fn()
      const { onTap } = useDoubleTap(onDoubleTap)
      
      expect(onTap).toBeDefined()
    })

    it('should trigger callback on double tap', () => {
      const onDoubleTap = vi.fn()
      const { onTap } = useDoubleTap(onDoubleTap, 300)
      
      const mockEvent = { 
        target: document.createElement('div'),
        preventDefault: vi.fn()
      }
      
      // First tap
      onTap(mockEvent)
      
      // Second tap within delay
      vi.advanceTimersByTime(100)
      onTap(mockEvent)
      
      expect(onDoubleTap).toHaveBeenCalledWith(mockEvent)
      expect(mockEvent.preventDefault).toHaveBeenCalled()
    })

    it('should not trigger callback on single tap', () => {
      const onDoubleTap = vi.fn()
      const { onTap } = useDoubleTap(onDoubleTap, 300)
      
      const mockEvent = { 
        target: document.createElement('div'),
        preventDefault: vi.fn()
      }
      
      onTap(mockEvent)
      vi.advanceTimersByTime(400)
      
      expect(onDoubleTap).not.toHaveBeenCalled()
    })

    it('should not trigger callback if taps are too far apart', () => {
      const onDoubleTap = vi.fn()
      const { onTap } = useDoubleTap(onDoubleTap, 300)
      
      const mockEvent = { 
        target: document.createElement('div'),
        preventDefault: vi.fn()
      }
      
      // First tap
      onTap(mockEvent)
      
      // Wait longer than delay
      vi.advanceTimersByTime(400)
      
      // Second tap
      onTap(mockEvent)
      
      expect(onDoubleTap).not.toHaveBeenCalled()
    })

    it('should use custom delay', () => {
      const onDoubleTap = vi.fn()
      const { onTap } = useDoubleTap(onDoubleTap, 500)
      
      const mockEvent = { 
        target: document.createElement('div'),
        preventDefault: vi.fn()
      }
      
      // First tap
      onTap(mockEvent)
      
      // Second tap within custom delay
      vi.advanceTimersByTime(400)
      onTap(mockEvent)
      
      expect(onDoubleTap).toHaveBeenCalled()
    })
  })
})
