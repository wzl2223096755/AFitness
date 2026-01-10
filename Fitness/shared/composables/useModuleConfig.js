/**
 * 模块配置 Composable
 * 用于在 Vue 组件中获取和使用模块配置
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */

import { ref, computed, onMounted } from 'vue'
import { moduleRegistry } from '../plugins/moduleRegistry.js'
import { getAllModulesConfig, getEnabledModules } from '../api/modules.js'

// 全局状态
const moduleConfig = ref(null)
const enabledModules = ref({})
const isLoading = ref(false)
const isLoaded = ref(false)
const error = ref(null)

/**
 * 加载模块配置
 * @returns {Promise<void>}
 */
const loadModuleConfig = async () => {
  if (isLoading.value || isLoaded.value) {
    return
  }
  
  isLoading.value = true
  error.value = null
  
  try {
    // 从后端获取模块配置
    const config = await getAllModulesConfig()
    if (config) {
      moduleConfig.value = config
      
      // 提取启用状态
      const enabled = {}
      for (const [name, module] of Object.entries(config)) {
        enabled[name] = module.enabled
      }
      enabledModules.value = enabled
      
      // 同步到模块注册器
      await moduleRegistry.syncWithBackend()
    }
    
    isLoaded.value = true
  } catch (err) {
    console.error('加载模块配置失败:', err)
    error.value = err
    
    // 使用默认配置（全部启用）
    enabledModules.value = {
      user: true,
      training: true,
      nutrition: true,
      recovery: true,
      admin: true
    }
  } finally {
    isLoading.value = false
  }
}

/**
 * 使用模块配置
 * @returns {Object} 模块配置相关的响应式数据和方法
 */
export function useModuleConfig() {
  // 组件挂载时自动加载配置
  onMounted(() => {
    if (!isLoaded.value && !isLoading.value) {
      loadModuleConfig()
    }
  })
  
  /**
   * 检查模块是否启用
   * @param {string} moduleName 模块名称
   * @returns {boolean}
   */
  const isModuleEnabled = (moduleName) => {
    if (!isLoaded.value) {
      return true // 默认启用
    }
    return enabledModules.value[moduleName] !== false
  }
  
  /**
   * 获取模块信息
   * @param {string} moduleName 模块名称
   * @returns {Object|null}
   */
  const getModuleInfo = (moduleName) => {
    if (!moduleConfig.value) {
      return null
    }
    return moduleConfig.value[moduleName] || null
  }
  
  /**
   * 获取所有启用的模块名称
   * @returns {string[]}
   */
  const getEnabledModuleNames = () => {
    return Object.entries(enabledModules.value)
      .filter(([_, enabled]) => enabled)
      .map(([name]) => name)
  }
  
  /**
   * 获取按顺序排列的模块列表
   * @param {boolean} enabledOnly 是否只返回启用的模块
   * @returns {Object[]}
   */
  const getOrderedModules = (enabledOnly = true) => {
    if (!moduleConfig.value) {
      return []
    }
    
    let modules = Object.entries(moduleConfig.value)
      .map(([name, config]) => ({ name, ...config }))
    
    if (enabledOnly) {
      modules = modules.filter(m => m.enabled)
    }
    
    return modules.sort((a, b) => (a.order || 0) - (b.order || 0))
  }
  
  return {
    // 状态
    moduleConfig: computed(() => moduleConfig.value),
    enabledModules: computed(() => enabledModules.value),
    isLoading: computed(() => isLoading.value),
    isLoaded: computed(() => isLoaded.value),
    error: computed(() => error.value),
    
    // 方法
    loadModuleConfig,
    isModuleEnabled,
    getModuleInfo,
    getEnabledModuleNames,
    getOrderedModules
  }
}

/**
 * 刷新模块配置
 * @returns {Promise<void>}
 */
export const refreshModuleConfig = async () => {
  isLoaded.value = false
  await loadModuleConfig()
}

export default useModuleConfig
