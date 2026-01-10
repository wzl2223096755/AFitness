/**
 * 模块注册器
 * Module Registry for managing frontend modules
 * 
 * 支持功能：
 * - 模块注册、获取、加载
 * - 模块生命周期管理
 * - 模块依赖解析
 * - 动态模块加载
 * - 从后端同步模块配置
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */

import { getAllModulesConfig, getEnabledModules } from '../api/modules.js'

/**
 * 模块状态枚举
 */
export const ModuleStatus = {
  REGISTERED: 'registered',
  LOADING: 'loading',
  LOADED: 'loaded',
  ACTIVE: 'active',
  ERROR: 'error',
  DISABLED: 'disabled'
}

/**
 * 模块注册器类
 */
class ModuleRegistry {
  constructor() {
    this.modules = new Map()
    this.loadedModules = new Set()
    this.listeners = new Map()
    this.backendConfig = null
    this.configLoaded = false
  }

  /**
   * 从后端同步模块配置
   * @returns {Promise<boolean>} 同步是否成功
   */
  async syncWithBackend() {
    try {
      const config = await getAllModulesConfig()
      if (config) {
        this.backendConfig = config
        this.configLoaded = true
        
        // 更新已注册模块的启用状态
        for (const [moduleName, moduleEntry] of this.modules) {
          const backendModule = config[moduleName]
          if (backendModule) {
            const wasEnabled = moduleEntry.info.enabled
            moduleEntry.info.enabled = backendModule.enabled
            moduleEntry.info.order = backendModule.order || moduleEntry.info.order
            moduleEntry.info.description = backendModule.description || moduleEntry.info.description
            
            // 更新状态
            if (!backendModule.enabled && wasEnabled) {
              moduleEntry.status = ModuleStatus.DISABLED
              this._emit('disabled', moduleEntry)
            } else if (backendModule.enabled && !wasEnabled) {
              moduleEntry.status = ModuleStatus.REGISTERED
              this._emit('enabled', moduleEntry)
            }
          }
        }
        
        console.log('[ModuleRegistry] 已从后端同步模块配置')
        this._emit('configSynced', config)
        return true
      }
      return false
    } catch (error) {
      console.error('[ModuleRegistry] 同步后端配置失败:', error)
      return false
    }
  }

  /**
   * 获取后端模块配置
   * @returns {Object|null} 后端配置
   */
  getBackendConfig() {
    return this.backendConfig
  }

  /**
   * 检查后端配置是否已加载
   * @returns {boolean}
   */
  isConfigLoaded() {
    return this.configLoaded
  }

  /**
   * 根据后端配置检查模块是否启用
   * @param {string} moduleName 模块名称
   * @returns {boolean}
   */
  isModuleEnabledByBackend(moduleName) {
    if (!this.backendConfig) {
      return true // 默认启用
    }
    const moduleConfig = this.backendConfig[moduleName]
    return moduleConfig ? moduleConfig.enabled : true
  }

  /**
   * 注册模块
   * @param {Object} moduleInfo 模块元信息
   * @param {Object} options 模块配置选项
   * @returns {boolean} 注册是否成功
   */
  register(moduleInfo, options = {}) {
    if (!moduleInfo || !moduleInfo.name) {
      console.error('[ModuleRegistry] Invalid module info: name is required')
      return false
    }

    const { routes = [], stores = [], components = {}, api = {} } = options

    if (this.modules.has(moduleInfo.name)) {
      console.warn(`[ModuleRegistry] Module "${moduleInfo.name}" is already registered`)
      return false
    }

    const moduleEntry = {
      info: {
        name: moduleInfo.name,
        displayName: moduleInfo.displayName || moduleInfo.name,
        icon: moduleInfo.icon || null,
        order: moduleInfo.order || 0,
        permissions: moduleInfo.permissions || [],
        dependencies: moduleInfo.dependencies || [],
        enabled: moduleInfo.enabled !== false,
        version: moduleInfo.version || '1.0.0'
      },
      routes,
      stores,
      components,
      api,
      status: ModuleStatus.REGISTERED,
      loadedAt: null,
      error: null
    }

    this.modules.set(moduleInfo.name, moduleEntry)
    this._emit('registered', moduleEntry)
    
    console.log(`[ModuleRegistry] Module "${moduleInfo.name}" registered successfully`)
    return true
  }

  /**
   * 获取模块
   * @param {string} name 模块名称
   * @returns {Object|null} 模块对象
   */
  getModule(name) {
    return this.modules.get(name) || null
  }

  /**
   * 获取所有模块
   * @param {Object} options 过滤选项
   * @returns {Array} 模块数组
   */
  getAllModules(options = {}) {
    const { enabledOnly = false, sortByOrder = true } = options
    
    let modules = Array.from(this.modules.values())
    
    if (enabledOnly) {
      modules = modules.filter(m => m.info.enabled)
    }
    
    if (sortByOrder) {
      modules.sort((a, b) => a.info.order - b.info.order)
    }
    
    return modules
  }

  /**
   * 获取模块信息列表
   * @param {Object} options 过滤选项
   * @returns {Array} 模块信息数组
   */
  getModuleInfoList(options = {}) {
    return this.getAllModules(options).map(m => m.info)
  }

  /**
   * 检查模块是否存在
   * @param {string} name 模块名称
   * @returns {boolean}
   */
  hasModule(name) {
    return this.modules.has(name)
  }

  /**
   * 检查模块是否已加载
   * @param {string} name 模块名称
   * @returns {boolean}
   */
  isLoaded(name) {
    return this.loadedModules.has(name)
  }

  /**
   * 检查模块是否启用
   * @param {string} name 模块名称
   * @returns {boolean}
   */
  isEnabled(name) {
    const module = this.modules.get(name)
    return module ? module.info.enabled : false
  }

  /**
   * 加载模块
   * @param {string} name 模块名称
   * @returns {Promise<Object>} 加载的模块
   */
  async loadModule(name) {
    const module = this.modules.get(name)
    
    if (!module) {
      throw new Error(`Module "${name}" not found`)
    }

    if (!module.info.enabled) {
      throw new Error(`Module "${name}" is disabled`)
    }

    if (this.loadedModules.has(name)) {
      return module
    }

    // 检查并加载依赖
    await this._loadDependencies(module)

    module.status = ModuleStatus.LOADING
    this._emit('loading', module)

    try {
      // 执行模块初始化钩子
      if (module.onInit && typeof module.onInit === 'function') {
        await module.onInit()
      }

      module.status = ModuleStatus.LOADED
      module.loadedAt = new Date()
      this.loadedModules.add(name)
      
      this._emit('loaded', module)
      console.log(`[ModuleRegistry] Module "${name}" loaded successfully`)
      
      return module
    } catch (error) {
      module.status = ModuleStatus.ERROR
      module.error = error
      this._emit('error', { module, error })
      throw error
    }
  }

  /**
   * 加载所有启用的模块
   * @returns {Promise<Array>} 加载的模块数组
   */
  async loadAllModules() {
    const enabledModules = this.getAllModules({ enabledOnly: true })
    const loadedModules = []

    for (const module of enabledModules) {
      try {
        await this.loadModule(module.info.name)
        loadedModules.push(module)
      } catch (error) {
        console.error(`[ModuleRegistry] Failed to load module "${module.info.name}":`, error)
      }
    }

    return loadedModules
  }

  /**
   * 卸载模块
   * @param {string} name 模块名称
   * @returns {boolean}
   */
  async unloadModule(name) {
    const module = this.modules.get(name)
    
    if (!module || !this.loadedModules.has(name)) {
      return false
    }

    try {
      // 执行模块销毁钩子
      if (module.onDestroy && typeof module.onDestroy === 'function') {
        await module.onDestroy()
      }

      module.status = ModuleStatus.REGISTERED
      module.loadedAt = null
      this.loadedModules.delete(name)
      
      this._emit('unloaded', module)
      console.log(`[ModuleRegistry] Module "${name}" unloaded successfully`)
      
      return true
    } catch (error) {
      console.error(`[ModuleRegistry] Failed to unload module "${name}":`, error)
      return false
    }
  }

  /**
   * 启用模块
   * @param {string} name 模块名称
   * @returns {boolean}
   */
  enableModule(name) {
    const module = this.modules.get(name)
    if (!module) return false
    
    module.info.enabled = true
    module.status = ModuleStatus.REGISTERED
    this._emit('enabled', module)
    return true
  }

  /**
   * 禁用模块
   * @param {string} name 模块名称
   * @returns {boolean}
   */
  disableModule(name) {
    const module = this.modules.get(name)
    if (!module) return false
    
    module.info.enabled = false
    module.status = ModuleStatus.DISABLED
    this._emit('disabled', module)
    return true
  }

  /**
   * 获取模块路由
   * @param {string} name 模块名称
   * @returns {Array} 路由数组
   */
  getModuleRoutes(name) {
    const module = this.modules.get(name)
    return module ? module.routes : []
  }

  /**
   * 获取所有模块路由
   * @param {Object} options 过滤选项
   * @returns {Array} 路由数组
   */
  getAllRoutes(options = { enabledOnly: true }) {
    const modules = this.getAllModules(options)
    return modules.flatMap(m => m.routes)
  }

  /**
   * 获取模块 Store
   * @param {string} name 模块名称
   * @returns {Array} Store 数组
   */
  getModuleStores(name) {
    const module = this.modules.get(name)
    return module ? module.stores : []
  }

  /**
   * 添加事件监听器
   * @param {string} event 事件名称
   * @param {Function} callback 回调函数
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听器
   * @param {string} event 事件名称
   * @param {Function} callback 回调函数
   */
  off(event, callback) {
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   * @private
   */
  _emit(event, data) {
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      callbacks.forEach(cb => {
        try {
          cb(data)
        } catch (error) {
          console.error(`[ModuleRegistry] Error in event listener for "${event}":`, error)
        }
      })
    }
  }

  /**
   * 加载模块依赖
   * @private
   */
  async _loadDependencies(module) {
    const dependencies = module.info.dependencies || []
    
    for (const depName of dependencies) {
      if (!this.loadedModules.has(depName)) {
        const depModule = this.modules.get(depName)
        if (!depModule) {
          throw new Error(`Dependency "${depName}" not found for module "${module.info.name}"`)
        }
        await this.loadModule(depName)
      }
    }
  }

  /**
   * 清空所有模块
   */
  clear() {
    this.modules.clear()
    this.loadedModules.clear()
    this.listeners.clear()
  }

  /**
   * 获取模块统计信息
   * @returns {Object}
   */
  getStats() {
    const modules = Array.from(this.modules.values())
    return {
      total: modules.length,
      enabled: modules.filter(m => m.info.enabled).length,
      disabled: modules.filter(m => !m.info.enabled).length,
      loaded: this.loadedModules.size,
      byStatus: {
        [ModuleStatus.REGISTERED]: modules.filter(m => m.status === ModuleStatus.REGISTERED).length,
        [ModuleStatus.LOADING]: modules.filter(m => m.status === ModuleStatus.LOADING).length,
        [ModuleStatus.LOADED]: modules.filter(m => m.status === ModuleStatus.LOADED).length,
        [ModuleStatus.ACTIVE]: modules.filter(m => m.status === ModuleStatus.ACTIVE).length,
        [ModuleStatus.ERROR]: modules.filter(m => m.status === ModuleStatus.ERROR).length,
        [ModuleStatus.DISABLED]: modules.filter(m => m.status === ModuleStatus.DISABLED).length
      }
    }
  }
}

// 创建单例实例
export const moduleRegistry = new ModuleRegistry()

// 导出类以便测试或创建新实例
export { ModuleRegistry }

export default moduleRegistry
