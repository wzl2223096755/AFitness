import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useLayoutStore } from './layout'

describe('Layout Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      const store = useLayoutStore()
      
      expect(store.layouts).toEqual({})
      expect(store.currentLayoutId).toBe('default')
      expect(store.customLayouts).toEqual({})
    })
  })

  describe('computed properties', () => {
    it('currentLayout should return default layout when no custom layout', () => {
      const store = useLayoutStore()
      
      const layout = store.currentLayout
      expect(layout.id).toBe('default')
      expect(layout.name).toBe('默认布局')
      expect(layout.items).toBeDefined()
    })

    it('availableLayouts should include all preset layouts', () => {
      const store = useLayoutStore()
      
      const layouts = store.availableLayouts
      expect(layouts.length).toBeGreaterThanOrEqual(4)
      
      const layoutIds = layouts.map(l => l.id)
      expect(layoutIds).toContain('default')
      expect(layoutIds).toContain('compact')
      expect(layoutIds).toContain('dashboard')
      expect(layoutIds).toContain('focus')
    })

    it('customLayoutList should return empty array initially', () => {
      const store = useLayoutStore()
      
      expect(store.customLayoutList).toEqual([])
    })
  })

  describe('getLayout', () => {
    it('should return preset layout by id', () => {
      const store = useLayoutStore()
      
      const layout = store.getLayout('compact')
      expect(layout.id).toBe('compact')
      expect(layout.name).toBe('紧凑布局')
    })

    it('should return undefined for non-existent layout', () => {
      const store = useLayoutStore()
      
      const layout = store.getLayout('non-existent')
      expect(layout).toBeUndefined()
    })
  })

  describe('applyLayout', () => {
    it('should change current layout id', () => {
      const store = useLayoutStore()
      
      store.applyLayout('compact')
      
      expect(store.currentLayoutId).toBe('compact')
    })

    it('should not change layout for non-existent id', () => {
      const store = useLayoutStore()
      
      store.applyLayout('non-existent')
      
      expect(store.currentLayoutId).toBe('default')
    })
  })

  describe('saveLayout', () => {
    it('should save custom layout with new id for preset layouts', () => {
      const store = useLayoutStore()
      const layoutData = {
        id: 'default',
        name: 'Custom Default',
        items: []
      }
      
      const newId = store.saveLayout('default', layoutData)
      
      expect(newId).toContain('default_custom_')
      expect(store.customLayouts[newId]).toBeDefined()
    })

    it('should save custom layout directly', () => {
      const store = useLayoutStore()
      const layoutData = {
        id: 'my-layout',
        name: 'My Layout',
        items: []
      }
      
      const id = store.saveLayout('my-layout', layoutData)
      
      expect(id).toBe('my-layout')
      expect(store.customLayouts['my-layout']).toBeDefined()
    })
  })

  describe('deleteLayout', () => {
    it('should delete custom layout', () => {
      const store = useLayoutStore()
      store.customLayouts['custom-1'] = { id: 'custom-1', name: 'Custom', items: [] }
      
      store.deleteLayout('custom-1')
      
      expect(store.customLayouts['custom-1']).toBeUndefined()
    })

    it('should reset to default if deleting current layout', () => {
      const store = useLayoutStore()
      store.customLayouts['custom-1'] = { id: 'custom-1', name: 'Custom', items: [] }
      store.currentLayoutId = 'custom-1'
      
      store.deleteLayout('custom-1')
      
      expect(store.currentLayoutId).toBe('default')
    })

    it('should not delete preset layouts', () => {
      const store = useLayoutStore()
      
      store.deleteLayout('default')
      
      // Preset layouts are not in customLayouts, so nothing happens
      expect(store.getLayout('default')).toBeDefined()
    })
  })

  describe('updateItem', () => {
    it('should update item in current layout', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      
      store.updateItem('welcome', { locked: true })
      
      const layout = store.currentLayout
      const welcomeItem = layout.items.find(item => item.id === 'welcome')
      expect(welcomeItem.locked).toBe(true)
    })

    it('should not update non-existent item', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      
      const itemsBefore = [...store.currentLayout.items]
      store.updateItem('non-existent', { locked: true })
      
      expect(store.currentLayout.items.length).toBe(itemsBefore.length)
    })
  })

  describe('addItem', () => {
    it('should add new item to current layout', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      const initialCount = store.currentLayout.items.length
      
      store.addItem('custom-widget')
      
      expect(store.currentLayout.items.length).toBe(initialCount + 1)
      const newItem = store.currentLayout.items[store.currentLayout.items.length - 1]
      expect(newItem.type).toBe('custom-widget')
    })

    it('should add item with custom position', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      
      store.addItem('custom-widget', { x: 5, y: 5, w: 6, h: 4 })
      
      const newItem = store.currentLayout.items[store.currentLayout.items.length - 1]
      expect(newItem.position).toEqual({ x: 5, y: 5, w: 6, h: 4 })
    })
  })

  describe('removeItem', () => {
    it('should remove item from current layout', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      const initialCount = store.currentLayout.items.length
      
      store.removeItem('welcome')
      
      expect(store.currentLayout.items.length).toBe(initialCount - 1)
      expect(store.currentLayout.items.find(item => item.id === 'welcome')).toBeUndefined()
    })
  })

  describe('lockItem and unlockItem', () => {
    it('should lock item', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      
      store.lockItem('welcome')
      
      const item = store.currentLayout.items.find(i => i.id === 'welcome')
      expect(item.locked).toBe(true)
    })

    it('should unlock item', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      store.lockItem('welcome')
      
      store.unlockItem('welcome')
      
      const item = store.currentLayout.items.find(i => i.id === 'welcome')
      expect(item.locked).toBe(false)
    })
  })

  describe('resetLayout', () => {
    it('should reset to default layout', () => {
      const store = useLayoutStore()
      store.applyLayout('compact')
      
      store.resetLayout()
      
      expect(store.currentLayoutId).toBe('default')
    })
  })

  describe('exportLayout and importLayout', () => {
    it('should export layout as JSON string', () => {
      const store = useLayoutStore()
      
      const exported = store.exportLayout('default')
      
      expect(typeof exported).toBe('string')
      const parsed = JSON.parse(exported)
      expect(parsed.id).toBe('default')
    })

    it('should return null for non-existent layout', () => {
      const store = useLayoutStore()
      
      const exported = store.exportLayout('non-existent')
      
      expect(exported).toBeNull()
    })

    it('should import layout from JSON string', () => {
      const store = useLayoutStore()
      const layoutData = JSON.stringify({
        id: 'imported',
        name: 'Imported Layout',
        items: [{ id: 'test', type: 'test', position: { x: 0, y: 0, w: 4, h: 3 } }]
      })
      
      const importedId = store.importLayout(layoutData)
      
      expect(importedId).toContain('imported_imported_')
      expect(store.customLayouts[importedId]).toBeDefined()
    })

    it('should import layout from object', () => {
      const store = useLayoutStore()
      const layoutData = {
        id: 'imported',
        name: 'Imported Layout',
        items: [{ id: 'test', type: 'test', position: { x: 0, y: 0, w: 4, h: 3 } }]
      }
      
      const importedId = store.importLayout(layoutData)
      
      expect(importedId).toContain('imported_imported_')
    })

    it('should return null for invalid layout data', () => {
      const store = useLayoutStore()
      
      const importedId = store.importLayout('invalid json')
      
      expect(importedId).toBeNull()
    })
  })

  describe('getLayoutName', () => {
    it('should return layout name', () => {
      const store = useLayoutStore()
      
      expect(store.getLayoutName('default')).toBe('默认布局')
      expect(store.getLayoutName('compact')).toBe('紧凑布局')
    })

    it('should return id for non-existent layout', () => {
      const store = useLayoutStore()
      
      expect(store.getLayoutName('non-existent')).toBe('non-existent')
    })
  })

  describe('getLayoutStats', () => {
    it('should return layout statistics', () => {
      const store = useLayoutStore()
      store.applyLayout('default')
      
      const stats = store.getLayoutStats()
      
      expect(stats.totalItems).toBeGreaterThan(0)
      expect(stats.lockedItems).toBeDefined()
      expect(stats.layoutType).toBe('grid')
      expect(stats.columns).toBe(12)
    })
  })

  describe('validateLayout', () => {
    it('should validate correct layout', () => {
      const store = useLayoutStore()
      const layoutData = {
        items: [
          { id: 'item1', type: 'widget', position: { x: 0, y: 0, w: 4, h: 3 } },
          { id: 'item2', type: 'widget', position: { x: 4, y: 0, w: 4, h: 3 } }
        ]
      }
      
      const result = store.validateLayout(layoutData)
      
      expect(result.valid).toBe(true)
      expect(result.errors).toHaveLength(0)
    })

    it('should detect missing items array', () => {
      const store = useLayoutStore()
      
      const result = store.validateLayout({})
      
      expect(result.valid).toBe(false)
      expect(result.errors).toContain('布局数据格式无效')
    })

    it('should detect duplicate item ids', () => {
      const store = useLayoutStore()
      const layoutData = {
        items: [
          { id: 'item1', type: 'widget', position: { x: 0, y: 0, w: 4, h: 3 } },
          { id: 'item1', type: 'widget', position: { x: 4, y: 0, w: 4, h: 3 } }
        ]
      }
      
      const result = store.validateLayout(layoutData)
      
      expect(result.valid).toBe(false)
      expect(result.errors.some(e => e.includes('重复'))).toBe(true)
    })

    it('should detect missing item id', () => {
      const store = useLayoutStore()
      const layoutData = {
        items: [
          { type: 'widget', position: { x: 0, y: 0, w: 4, h: 3 } }
        ]
      }
      
      const result = store.validateLayout(layoutData)
      
      expect(result.valid).toBe(false)
      expect(result.errors.some(e => e.includes('缺少 ID'))).toBe(true)
    })

    it('should detect missing item type', () => {
      const store = useLayoutStore()
      const layoutData = {
        items: [
          { id: 'item1', position: { x: 0, y: 0, w: 4, h: 3 } }
        ]
      }
      
      const result = store.validateLayout(layoutData)
      
      expect(result.valid).toBe(false)
      expect(result.errors.some(e => e.includes('缺少类型'))).toBe(true)
    })

    it('should detect missing position', () => {
      const store = useLayoutStore()
      const layoutData = {
        items: [
          { id: 'item1', type: 'widget' }
        ]
      }
      
      const result = store.validateLayout(layoutData)
      
      expect(result.valid).toBe(false)
      expect(result.errors.some(e => e.includes('缺少位置信息'))).toBe(true)
    })
  })

  describe('initializeLayout', () => {
    it('should load from storage', () => {
      const store = useLayoutStore()
      localStorage.setItem('fitness-layouts', JSON.stringify({
        layouts: {},
        customLayouts: { 'custom-1': { id: 'custom-1', name: 'Custom', items: [] } },
        currentLayoutId: 'compact'
      }))
      
      store.initializeLayout()
      
      expect(store.currentLayoutId).toBe('compact')
      expect(store.customLayouts['custom-1']).toBeDefined()
    })

    it('should use default layout if stored layout not found', () => {
      const store = useLayoutStore()
      localStorage.setItem('fitness-layouts', JSON.stringify({
        layouts: {},
        customLayouts: {},
        currentLayoutId: 'non-existent'
      }))
      
      store.initializeLayout()
      
      expect(store.currentLayoutId).toBe('default')
    })
  })
})
