import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 预设主题配置
const presetThemes = {
  neonPurple: {
    name: '霓虹紫',
    primary: '#8020ff',
    secondary: '#ff00ff',
    accent: '#00f2fe',
    success: '#00ff88',
    warning: '#ffaa00',
    danger: '#ff0055',
    bgPrimary: '#0a0a14',
    bgSecondary: '#121225',
    textPrimary: '#ffffff',
    textSecondary: '#8888aa',
    borderColor: 'rgba(112, 0, 255, 0.2)'
  },
  oceanBlue: {
    name: '海洋蓝',
    primary: '#0ea5e9',
    secondary: '#06b6d4',
    accent: '#22d3ee',
    success: '#10b981',
    warning: '#f59e0b',
    danger: '#ef4444',
    bgPrimary: '#0c1222',
    bgSecondary: '#1e293b',
    textPrimary: '#f8fafc',
    textSecondary: '#94a3b8',
    borderColor: 'rgba(14, 165, 233, 0.2)'
  },
  forestGreen: {
    name: '森林绿',
    primary: '#22c55e',
    secondary: '#10b981',
    accent: '#34d399',
    success: '#4ade80',
    warning: '#fbbf24',
    danger: '#f87171',
    bgPrimary: '#0a1410',
    bgSecondary: '#14261e',
    textPrimary: '#f0fdf4',
    textSecondary: '#86efac',
    borderColor: 'rgba(34, 197, 94, 0.2)'
  },
  sunsetOrange: {
    name: '日落橙',
    primary: '#f97316',
    secondary: '#fb923c',
    accent: '#fbbf24',
    success: '#84cc16',
    warning: '#eab308',
    danger: '#dc2626',
    bgPrimary: '#1a0f0a',
    bgSecondary: '#2d1810',
    textPrimary: '#fff7ed',
    textSecondary: '#fdba74',
    borderColor: 'rgba(249, 115, 22, 0.2)'
  },
  roseGold: {
    name: '玫瑰金',
    primary: '#ec4899',
    secondary: '#f472b6',
    accent: '#fb7185',
    success: '#a3e635',
    warning: '#facc15',
    danger: '#f43f5e',
    bgPrimary: '#1a0a14',
    bgSecondary: '#2d1225',
    textPrimary: '#fdf2f8',
    textSecondary: '#f9a8d4',
    borderColor: 'rgba(236, 72, 153, 0.2)'
  },
  midnightBlack: {
    name: '午夜黑',
    primary: '#6366f1',
    secondary: '#8b5cf6',
    accent: '#a78bfa',
    success: '#22c55e',
    warning: '#eab308',
    danger: '#ef4444',
    bgPrimary: '#09090b',
    bgSecondary: '#18181b',
    textPrimary: '#fafafa',
    textSecondary: '#a1a1aa',
    borderColor: 'rgba(99, 102, 241, 0.2)'
  },
  lightMode: {
    name: '浅色模式',
    primary: '#6366f1',
    secondary: '#8b5cf6',
    accent: '#06b6d4',
    success: '#22c55e',
    warning: '#f59e0b',
    danger: '#ef4444',
    bgPrimary: '#ffffff',
    bgSecondary: '#f8fafc',
    textPrimary: '#1e293b',
    textSecondary: '#64748b',
    borderColor: 'rgba(0, 0, 0, 0.1)'
  }
}

export const useThemeStore = defineStore('theme', () => {
  // 当前主题名称
  const currentTheme = ref('neonPurple')
  
  // 自定义颜色
  const customColors = ref({
    primary: '#8020ff',
    secondary: '#ff00ff',
    accent: '#00f2fe',
    success: '#00ff88',
    warning: '#ffaa00',
    danger: '#ff0055',
    bgPrimary: '#0a0a14',
    bgSecondary: '#121225',
    textPrimary: '#ffffff',
    textSecondary: '#8888aa'
  })
  
  // 是否使用自定义主题
  const useCustomTheme = ref(false)
  
  // 获取当前主题配置
  const themeConfig = computed(() => {
    if (useCustomTheme.value) {
      return {
        name: '自定义',
        ...customColors.value,
        borderColor: `rgba(${hexToRgb(customColors.value.primary)}, 0.2)`
      }
    }
    return presetThemes[currentTheme.value] || presetThemes.neonPurple
  })
  
  // 获取所有预设主题
  const allPresetThemes = computed(() => presetThemes)
  
  // 切换预设主题
  const setTheme = (themeName) => {
    if (presetThemes[themeName]) {
      currentTheme.value = themeName
      useCustomTheme.value = false
      applyTheme()
      saveToStorage()
    }
  }
  
  // 设置自定义颜色
  const setCustomColor = (colorKey, colorValue) => {
    if (Object.prototype.hasOwnProperty.call(customColors.value, colorKey)) {
      customColors.value[colorKey] = colorValue
      useCustomTheme.value = true
      applyTheme()
      saveToStorage()
    }
  }
  
  // 设置完整的自定义主题
  const setCustomTheme = (colors) => {
    customColors.value = { ...customColors.value, ...colors }
    useCustomTheme.value = true
    applyTheme()
    saveToStorage()
  }
  
  // 应用主题到 CSS 变量
  const applyTheme = () => {
    const theme = themeConfig.value
    const root = document.documentElement
    
    // 设置 CSS 变量
    root.style.setProperty('--theme-primary', theme.primary)
    root.style.setProperty('--theme-secondary', theme.secondary)
    root.style.setProperty('--theme-accent', theme.accent)
    root.style.setProperty('--theme-success', theme.success)
    root.style.setProperty('--theme-warning', theme.warning)
    root.style.setProperty('--theme-danger', theme.danger)
    root.style.setProperty('--theme-bg-primary', theme.bgPrimary)
    root.style.setProperty('--theme-bg-secondary', theme.bgSecondary)
    root.style.setProperty('--theme-text-primary', theme.textPrimary)
    root.style.setProperty('--theme-text-secondary', theme.textSecondary)
    root.style.setProperty('--theme-border-color', theme.borderColor)
    
    // 计算并设置 RGB 值（用于 rgba）
    root.style.setProperty('--theme-primary-rgb', hexToRgb(theme.primary))
    root.style.setProperty('--theme-bg-secondary-rgb', hexToRgb(theme.bgSecondary))
    
    // 设置 data-theme 属性
    const isLight = isLightColor(theme.bgPrimary)
    root.setAttribute('data-theme', isLight ? 'light' : 'dark')
  }
  
  // 保存到本地存储
  const saveToStorage = () => {
    const data = {
      currentTheme: currentTheme.value,
      customColors: customColors.value,
      useCustomTheme: useCustomTheme.value
    }
    localStorage.setItem('fitness-theme', JSON.stringify(data))
  }
  
  // 从本地存储加载
  const loadFromStorage = () => {
    try {
      const saved = localStorage.getItem('fitness-theme')
      if (saved) {
        const data = JSON.parse(saved)
        currentTheme.value = data.currentTheme || 'neonPurple'
        customColors.value = { ...customColors.value, ...data.customColors }
        useCustomTheme.value = data.useCustomTheme || false
      }
    } catch (e) {
      console.error('Failed to load theme from storage:', e)
    }
    applyTheme()
  }
  
  // 重置为默认主题
  const resetToDefault = () => {
    currentTheme.value = 'neonPurple'
    useCustomTheme.value = false
    applyTheme()
    saveToStorage()
  }
  
  // 辅助函数：十六进制转 RGB
  function hexToRgb(hex) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    if (result) {
      return `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}`
    }
    return '128, 32, 255'
  }
  
  // 辅助函数：判断是否为浅色
  function isLightColor(hex) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    if (result) {
      const r = parseInt(result[1], 16)
      const g = parseInt(result[2], 16)
      const b = parseInt(result[3], 16)
      const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
      return luminance > 0.5
    }
    return false
  }
  
  return {
    currentTheme,
    customColors,
    useCustomTheme,
    themeConfig,
    allPresetThemes,
    setTheme,
    setCustomColor,
    setCustomTheme,
    applyTheme,
    loadFromStorage,
    resetToDefault
  }
})
