import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useLayoutStore = defineStore('layout', () => {
  // 状态
  const layouts = ref({})
  const currentLayoutId = ref('default')
  const customLayouts = ref({})

  // 预设布局配置
  const presetLayouts = {
    default: {
      id: 'default',
      name: '默认布局',
      description: '经典的仪表盘布局，适合大多数用户',
      type: 'grid',
      config: {
        columns: 12,
        gap: 16,
        padding: 24
      },
      items: [
        {
          id: 'welcome',
          type: 'welcome',
          position: { x: 0, y: 0, w: 12, h: 2 },
          size: { width: '100%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'metrics',
          type: 'metrics',
          position: { x: 0, y: 2, w: 12, h: 3 },
          size: { width: '100%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'analytics',
          type: 'analytics',
          position: { x: 0, y: 5, w: 8, h: 6 },
          size: { width: '66.67%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'activity',
          type: 'activity',
          position: { x: 8, y: 5, w: 4, h: 6 },
          size: { width: '33.33%', height: 'auto' },
          locked: false,
          data: {}
        }
      ]
    },
    compact: {
      id: 'compact',
      name: '紧凑布局',
      description: '节省空间的紧凑布局，适合小屏幕',
      type: 'grid',
      config: {
        columns: 8,
        gap: 12,
        padding: 16
      },
      items: [
        {
          id: 'welcome',
          type: 'welcome',
          position: { x: 0, y: 0, w: 8, h: 1 },
          size: { width: '100%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'metrics',
          type: 'metrics',
          position: { x: 0, y: 1, w: 8, h: 2 },
          size: { width: '100%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'analytics',
          type: 'analytics',
          position: { x: 0, y: 3, w: 4, h: 4 },
          size: { width: '50%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'activity',
          type: 'activity',
          position: { x: 4, y: 3, w: 4, h: 4 },
          size: { width: '50%', height: 'auto' },
          locked: false,
          data: {}
        }
      ]
    },
    dashboard: {
      id: 'dashboard',
      name: '仪表盘布局',
      description: '专业仪表盘风格，突出数据分析',
      type: 'grid',
      config: {
        columns: 16,
        gap: 20,
        padding: 32
      },
      items: [
        {
          id: 'welcome',
          type: 'welcome',
          position: { x: 0, y: 0, w: 16, h: 2 },
          size: { width: '100%', height: 'auto' },
          locked: true,
          data: {}
        },
        {
          id: 'metrics',
          type: 'metrics',
          position: { x: 0, y: 2, w: 16, h: 2 },
          size: { width: '100%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'analytics',
          type: 'analytics',
          position: { x: 0, y: 4, w: 10, h: 8 },
          size: { width: '62.5%', height: 'auto' },
          locked: false,
          data: {}
        },
        {
          id: 'activity',
          type: 'activity',
          position: { x: 10, y: 4, w: 6, h: 8 },
          size: { width: '37.5%', height: 'auto' },
          locked: false,
          data: {}
        }
      ]
    },
    focus: {
      id: 'focus',
      name: '专注布局',
      description: '简洁专注的布局，减少干扰',
      type: 'grid',
      config: {
        columns: 6,
        gap: 24,
        padding: 40
      },
      items: [
        {
          id: 'welcome',
          type: 'welcome',
          position: { x: 0, y: 0, w: 6, h: 1 },
          size: { width: '100%', height: 'auto' },
          locked: true,
          data: {}
        },
        {
          id: 'analytics',
          type: 'analytics',
          position: { x: 0, y: 1, w: 6, h: 6 },
          size: { width: '100%', height: 'auto' },
          locked: false,
          data: {}
        }
      ]
    }
  }

  // 计算属性
  const currentLayout = computed(() => {
    return layouts.value[currentLayoutId.value] || presetLayouts[currentLayoutId.value] || presetLayouts.default
  })

  const availableLayouts = computed(() => {
    const allLayouts = { ...presetLayouts, ...customLayouts.value }
    return Object.values(allLayouts)
  })

  const customLayoutList = computed(() => {
    return Object.values(customLayouts.value)
  })

  // 方法
  const saveLayout = (layoutId, layoutData) => {
    if (presetLayouts[layoutId]) {
      // 如果是预设布局，保存为自定义布局
      const customId = `${layoutId}_custom_${Date.now()}`
      customLayouts.value[customId] = { ...layoutData, id: customId }
      saveToStorage()
      return customId
    } else {
      // 保存自定义布局
      layouts.value[layoutId] = layoutData
      customLayouts.value[layoutId] = layoutData
      saveToStorage()
      return layoutId
    }
  }

  const getLayout = (layoutId) => {
    return layouts.value[layoutId] || presetLayouts[layoutId]
  }

  const applyLayout = (layoutId) => {
    const layout = getLayout(layoutId)
    if (layout) {
      currentLayoutId.value = layoutId
      layouts.value[layoutId] = { ...layout }
      saveToStorage()
    }
  }

  const deleteLayout = (layoutId) => {
    if (customLayouts.value[layoutId]) {
      delete customLayouts.value[layoutId]
      if (currentLayoutId.value === layoutId) {
        currentLayoutId.value = 'default'
      }
      saveToStorage()
    }
  }

  const updateItem = (itemId, updates) => {
    const layout = currentLayout.value
    if (layout) {
      const itemIndex = layout.items.findIndex(item => item.id === itemId)
      if (itemIndex > -1) {
        layout.items[itemIndex] = { ...layout.items[itemIndex], ...updates }
        layouts.value[currentLayoutId.value] = { ...layout }
        saveToStorage()
      }
    }
  }

  const addItem = (itemType, position = null) => {
    const layout = currentLayout.value
    if (layout) {
      const newItem = {
        id: `${itemType}_${Date.now()}`,
        type: itemType,
        position: position || { x: 0, y: 0, w: 4, h: 3 },
        size: { width: 'auto', height: 'auto' },
        locked: false,
        data: {}
      }
      layout.items.push(newItem)
      layouts.value[currentLayoutId.value] = { ...layout }
      saveToStorage()
    }
  }

  const removeItem = (itemId) => {
    const layout = currentLayout.value
    if (layout) {
      layout.items = layout.items.filter(item => item.id !== itemId)
      layouts.value[currentLayoutId.value] = { ...layout }
      saveToStorage()
    }
  }

  const lockItem = (itemId, locked = true) => {
    updateItem(itemId, { locked })
  }

  const unlockItem = (itemId) => {
    updateItem(itemId, { locked: false })
  }

  const resetLayout = () => {
    const defaultLayout = presetLayouts.default
    layouts.value[currentLayoutId.value] = { ...defaultLayout }
    currentLayoutId.value = 'default'
    saveToStorage()
  }

  const exportLayout = (layoutId) => {
    const layout = getLayout(layoutId)
    if (layout) {
      return JSON.stringify(layout, null, 2)
    }
    return null
  }

  const importLayout = (layoutData) => {
    try {
      const layout = typeof layoutData === 'string' ? JSON.parse(layoutData) : layoutData
      if (layout.id && layout.items) {
        const customId = `${layout.id}_imported_${Date.now()}`
        customLayouts.value[customId] = { ...layout, id: customId }
        saveToStorage()
        return customId
      }
    } catch (error) {
      console.error('导入布局失败:', error)
    }
    return null
  }

  const getLayoutName = (layoutId) => {
    const layout = getLayout(layoutId)
    return layout ? layout.name : layoutId
  }

  const initializeLayout = () => {
    // 从存储中恢复布局
    loadFromStorage()
    
    // 如果没有当前布局，使用默认布局
    if (!currentLayoutId.value || !getLayout(currentLayoutId.value)) {
      currentLayoutId.value = 'default'
    }
  }

  const getLayoutStats = () => {
    const layout = currentLayout.value
    if (!layout) return null
    
    return {
      totalItems: layout.items.length,
      lockedItems: layout.items.filter(item => item.locked).length,
      layoutType: layout.type,
      columns: layout.config?.columns || 12
    }
  }

  const validateLayout = (layoutData) => {
    if (!layoutData || !layoutData.items || !Array.isArray(layoutData.items)) {
      return { valid: false, errors: ['布局数据格式无效'] }
    }
    
    const errors = []
    const itemIds = new Set()
    
    layoutData.items.forEach((item, index) => {
      if (!item.id) {
        errors.push(`第 ${index + 1} 个组件缺少 ID`)
      } else if (itemIds.has(item.id)) {
        errors.push(`组件 ID "${item.id}" 重复`)
      } else {
        itemIds.add(item.id)
      }
      
      if (!item.type) {
        errors.push(`第 ${index + 1} 个组件缺少类型`)
      }
      
      if (!item.position) {
        errors.push(`第 ${index + 1} 个组件缺少位置信息`)
      }
    })
    
    return {
      valid: errors.length === 0,
      errors
    }
  }

  // 本地存储
  const saveToStorage = () => {
    try {
      const data = {
        layouts: layouts.value,
        customLayouts: customLayouts.value,
        currentLayoutId: currentLayoutId.value
      }
      localStorage.setItem('fitness-layouts', JSON.stringify(data))
    } catch (error) {
      console.error('保存布局到本地存储失败:', error)
    }
  }

  const loadFromStorage = () => {
    try {
      const data = localStorage.getItem('fitness-layouts')
      if (data) {
        const parsed = JSON.parse(data)
        layouts.value = parsed.layouts || {}
        customLayouts.value = parsed.customLayouts || {}
        currentLayoutId.value = parsed.currentLayoutId || 'default'
      }
    } catch (error) {
      console.error('从本地存储加载布局失败:', error)
    }
  }

  return {
    // 状态
    layouts,
    currentLayoutId,
    customLayouts,
    
    // 计算属性
    currentLayout,
    availableLayouts,
    customLayoutList,
    
    // 方法
    saveLayout,
    getLayout,
    applyLayout,
    deleteLayout,
    updateItem,
    addItem,
    removeItem,
    lockItem,
    unlockItem,
    resetLayout,
    exportLayout,
    importLayout,
    getLayoutName,
    initializeLayout,
    getLayoutStats,
    validateLayout
  }
})