/**
 * 主题管理 Composable
 * Theme management composable for shared use across frontend and admin
 */
import { ref, computed, readonly, onUnmounted } from 'vue'

// 主题管理
const theme = ref('light')

// 获取系统主题偏好
const getSystemTheme = () => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

// 获取当前实际主题
const getCurrentTheme = () => {
  if (theme.value === 'auto') {
    return getSystemTheme()
  }
  return theme.value
}

// 应用主题到DOM
const applyTheme = (newTheme) => {
  if (typeof document !== 'undefined') {
    const root = document.documentElement
    root.setAttribute('data-theme', newTheme)
    
    if (newTheme === 'auto') {
      const systemTheme = getSystemTheme()
      root.setAttribute('data-system-theme', systemTheme)
    } else {
      root.removeAttribute('data-system-theme')
    }
  }
}

// 初始化主题
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
    theme.value = savedTheme
  } else {
    theme.value = 'auto'
  }
  
  applyTheme(theme.value)
}

// 切换主题
const toggleTheme = () => {
  const themeOrder = ['light', 'dark', 'auto']
  const currentIndex = themeOrder.indexOf(theme.value)
  const nextIndex = (currentIndex + 1) % themeOrder.length
  setTheme(themeOrder[nextIndex])
}

// 设置主题
const setTheme = (newTheme) => {
  theme.value = newTheme
  localStorage.setItem('theme', newTheme)
  applyTheme(newTheme)
}

// 监听系统主题变化
const setupSystemThemeListener = () => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = () => {
      if (theme.value === 'auto') {
        applyTheme('auto')
      }
    }
    
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange)
    } else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleChange)
    }
    
    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', handleChange)
      } else if (mediaQuery.removeListener) {
        mediaQuery.removeListener(handleChange)
      }
    }
  }
  
  return () => {}
}

// 主题相关的CSS变量
export const getThemeCSSVariables = (theme) => {
  const lightTheme = {
    '--bg-primary': '#ffffff',
    '--bg-secondary': '#f5f7fa',
    '--bg-sidebar': '#001529',
    '--bg-header': '#ffffff',
    '--bg-card': '#ffffff',
    '--text-primary': '#303133',
    '--text-regular': '#606266',
    '--text-secondary': '#909399',
    '--text-placeholder': '#c0c4cc',
    '--text-white': '#ffffff',
    '--border-color': '#dcdfe6',
    '--border-light': '#ebeef5',
    '--border-lighter': '#f2f6fc',
    '--shadow-base': '0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04)',
    '--shadow-light': '0 2px 12px 0 rgba(0, 0, 0, 0.1)'
  }
  
  const darkTheme = {
    '--bg-primary': '#1a1a1a',
    '--bg-secondary': '#2d2d2d',
    '--bg-sidebar': '#000000',
    '--bg-header': '#2d2d2d',
    '--bg-card': '#2d2d2d',
    '--text-primary': '#ffffff',
    '--text-regular': '#e0e0e0',
    '--text-secondary': '#a0a0a0',
    '--text-placeholder': '#666666',
    '--text-white': '#ffffff',
    '--border-color': '#404040',
    '--border-light': '#333333',
    '--border-lighter': '#292929',
    '--shadow-base': '0 2px 4px rgba(0, 0, 0, 0.3), 0 0 6px rgba(0, 0, 0, 0.1)',
    '--shadow-light': '0 2px 12px 0 rgba(0, 0, 0, 0.2)'
  }
  
  return theme === 'dark' ? darkTheme : lightTheme
}

// 响应式断点
export const breakpoints = {
  xs: 480,
  sm: 768,
  md: 992,
  lg: 1200,
  xl: 1920
}

// 响应式工具函数
export const useResponsive = () => {
  const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)
  
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }
  
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', updateWidth)
  }
  
  const currentBreakpoint = computed(() => {
    const width = windowWidth.value
    if (width < breakpoints.xs) return 'xs'
    if (width < breakpoints.sm) return 'sm'
    if (width < breakpoints.md) return 'md'
    if (width < breakpoints.lg) return 'lg'
    return 'xl'
  })
  
  const isMobile = computed(() => currentBreakpoint.value === 'xs')
  const isTablet = computed(() => currentBreakpoint.value === 'sm' || currentBreakpoint.value === 'md')
  const isDesktop = computed(() => currentBreakpoint.value === 'lg' || currentBreakpoint.value === 'xl')
  
  return {
    windowWidth,
    currentBreakpoint,
    isMobile,
    isTablet,
    isDesktop
  }
}

// 导出主题管理
export const useTheme = () => {
  if (typeof window !== 'undefined') {
    initTheme()
    const cleanup = setupSystemThemeListener()
    
    onUnmounted(() => {
      cleanup()
    })
  }
  
  return {
    theme: readonly(theme),
    currentTheme: computed(() => getCurrentTheme()),
    isDark: computed(() => getCurrentTheme() === 'dark'),
    isLight: computed(() => getCurrentTheme() === 'light'),
    toggleTheme,
    setTheme
  }
}

export default useTheme
