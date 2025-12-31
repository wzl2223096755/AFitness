// 移动端触摸手势支持
import { ref, onMounted, onUnmounted } from 'vue'

export const useTouchGestures = (element) => {
  const startX = ref(0)
  const startY = ref(0)
  const endX = ref(0)
  const endY = ref(0)
  const isSwipeLeft = ref(false)
  const isSwipeRight = ref(false)
  const isSwipeUp = ref(false)
  const isSwipeDown = ref(false)
  
  const threshold = 50 // 滑动阈值
  
  const handleTouchStart = (e) => {
    startX.value = e.touches[0].clientX
    startY.value = e.touches[0].clientY
  }
  
  const handleTouchEnd = (e) => {
    endX.value = e.changedTouches[0].clientX
    endY.value = e.changedTouches[0].clientY
    
    const diffX = endX.value - startX.value
    const diffY = endY.value - startY.value
    
    // 重置状态
    isSwipeLeft.value = false
    isSwipeRight.value = false
    isSwipeUp.value = false
    isSwipeDown.value = false
    
    // 判断滑动方向
    if (Math.abs(diffX) > Math.abs(diffY)) {
      // 水平滑动
      if (Math.abs(diffX) > threshold) {
        if (diffX > 0) {
          isSwipeRight.value = true
        } else {
          isSwipeLeft.value = true
        }
      }
    } else {
      // 垂直滑动
      if (Math.abs(diffY) > threshold) {
        if (diffY > 0) {
          isSwipeDown.value = true
        } else {
          isSwipeUp.value = true
        }
      }
    }
  }
  
  onMounted(() => {
    if (element.value) {
      element.value.addEventListener('touchstart', handleTouchStart, { passive: true })
      element.value.addEventListener('touchend', handleTouchEnd, { passive: true })
    }
  })
  
  onUnmounted(() => {
    if (element.value) {
      element.value.removeEventListener('touchstart', handleTouchStart)
      element.value.removeEventListener('touchend', handleTouchEnd)
    }
  })
  
  return {
    isSwipeLeft,
    isSwipeRight,
    isSwipeUp,
    isSwipeDown
  }
}

// 移动端长按手势
export const useLongPress = (onLongPress, delay = 500) => {
  const timeoutId = ref(null)
  const isPressed = ref(false)
  
  const startPress = (e) => {
    isPressed.value = true
    timeoutId.value = setTimeout(() => {
      if (isPressed.value) {
        onLongPress(e)
      }
    }, delay)
  }
  
  const cancelPress = () => {
    isPressed.value = false
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
      timeoutId.value = null
    }
  }
  
  onUnmounted(() => {
    cancelPress()
  })
  
  return {
    onMouseDown: startPress,
    onMouseUp: cancelPress,
    onMouseLeave: cancelPress,
    onTouchStart: startPress,
    onTouchEnd: cancelPress,
    onTouchCancel: cancelPress
  }
}

// 移动端双击手势
export const useDoubleTap = (onDoubleTap, delay = 300) => {
  const lastTap = ref(0)
  const timeoutId = ref(null)
  
  const handleTap = (e) => {
    const currentTime = new Date().getTime()
    const tapLength = currentTime - lastTap.value
    
    if (tapLength < delay && tapLength > 0) {
      // 双击
      e.preventDefault()
      onDoubleTap(e)
      if (timeoutId.value) {
        clearTimeout(timeoutId.value)
        timeoutId.value = null
      }
    } else {
      // 单击，等待双击
      timeoutId.value = setTimeout(() => {
        timeoutId.value = null
      }, delay)
    }
    
    lastTap.value = currentTime
  }
  
  onUnmounted(() => {
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
    }
  })
  
  return {
    onTap: handleTap
  }
}

// 移动端捏合缩放
export const usePinchZoom = (element, options = {}) => {
  const scale = ref(1)
  const minScale = options.minScale || 0.5
  const maxScale = options.maxScale || 3
  const initialDistance = ref(0)
  
  const getDistance = (touches) => {
    const dx = touches[0].clientX - touches[1].clientX
    const dy = touches[0].clientY - touches[1].clientY
    return Math.sqrt(dx * dx + dy * dy)
  }
  
  const handleTouchStart = (e) => {
    if (e.touches.length === 2) {
      e.preventDefault()
      initialDistance.value = getDistance(e.touches)
    }
  }
  
  const handleTouchMove = (e) => {
    if (e.touches.length === 2) {
      e.preventDefault()
      const currentDistance = getDistance(e.touches)
      
      if (initialDistance.value > 0) {
        const newScale = scale.value * (currentDistance / initialDistance.value)
        scale.value = Math.max(minScale, Math.min(maxScale, newScale))
        
        if (element.value) {
          element.value.style.transform = `scale(${scale.value})`
        }
      }
    }
  }
  
  const reset = () => {
    scale.value = 1
    if (element.value) {
      element.value.style.transform = 'scale(1)'
    }
  }
  
  onMounted(() => {
    if (element.value) {
      element.value.addEventListener('touchstart', handleTouchStart, { passive: false })
      element.value.addEventListener('touchmove', handleTouchMove, { passive: false })
    }
  })
  
  onUnmounted(() => {
    if (element.value) {
      element.value.removeEventListener('touchstart', handleTouchStart)
      element.value.removeEventListener('touchmove', handleTouchMove)
    }
  })
  
  return {
    scale,
    reset
  }
}

// 移动端性能优化
export const useMobileOptimization = () => {
  // 检测是否为移动设备
  const isMobile = () => {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  }
  
  // 检测是否为触摸设备
  const isTouchDevice = () => {
    return 'ontouchstart' in window || navigator.maxTouchPoints > 0
  }
  
  // 优化滚动性能
  const optimizeScroll = () => {
    if (isMobile()) {
      // 启用被动事件监听器
      document.addEventListener('touchstart', () => {}, { passive: true })
      document.addEventListener('touchmove', () => {}, { passive: true })
      
      // 防止过度滚动
      document.body.style.overscrollBehavior = 'contain'
    }
  }
  
  // 优化点击延迟
  const eliminateClickDelay = () => {
    if (isTouchDevice()) {
      // FastClick 替代方案
      document.addEventListener('touchend', function(e) {
        // 创建自定义点击事件
        const clickEvent = new MouseEvent('click', {
          view: window,
          bubbles: true,
          cancelable: true
        })
        e.target.dispatchEvent(clickEvent)
      }, { passive: true })
    }
  }
  
  // 优化图片加载
  const optimizeImages = () => {
    const images = document.querySelectorAll('img[data-src]')
    const imageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target
          img.src = img.dataset.src
          img.classList.remove('lazy-image')
          imageObserver.unobserve(img)
        }
      })
    })
    
    images.forEach(img => {
      imageObserver.observe(img)
    })
  }
  
  onMounted(() => {
    optimizeScroll()
    eliminateClickDelay()
    optimizeImages()
  })
  
  return {
    isMobile,
    isTouchDevice,
    optimizeScroll,
    eliminateClickDelay,
    optimizeImages
  }
}

// 移动端网络状态监听
export const useNetworkStatus = () => {
  const isOnline = ref(navigator.onLine)
  const connectionType = ref('unknown')
  const connectionSpeed = ref(0)
  
  const updateConnectionInfo = () => {
    if ('connection' in navigator) {
      const connection = navigator.connection
      connectionType.value = connection.effectiveType || 'unknown'
      connectionSpeed.value = connection.downlink || 0
    }
  }
  
  const handleOnline = () => {
    isOnline.value = true
    updateConnectionInfo()
  }
  
  const handleOffline = () => {
    isOnline.value = false
  }
  
  onMounted(() => {
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)
    
    if ('connection' in navigator) {
      navigator.connection.addEventListener('change', updateConnectionInfo)
    }
    
    updateConnectionInfo()
  })
  
  onUnmounted(() => {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)
    
    if ('connection' in navigator) {
      navigator.connection.removeEventListener('change', updateConnectionInfo)
    }
  })
  
  return {
    isOnline,
    connectionType,
    connectionSpeed
  }
}