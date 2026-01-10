/**
 * 模块配置 API
 * 用于从后端获取模块启用/禁用状态
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 */

import { get } from './request.js'

/**
 * 获取所有模块配置信息
 * @returns {Promise<Object>} 模块配置对象
 */
export const getAllModulesConfig = async () => {
  try {
    const response = await get('/api/v1/modules')
    if (response.code === 200) {
      return response.data
    }
    console.warn('获取模块配置失败:', response.message)
    return null
  } catch (error) {
    console.error('获取模块配置失败:', error)
    return null
  }
}

/**
 * 获取已启用的模块列表
 * @returns {Promise<Object>} 模块启用状态对象 { moduleName: boolean }
 */
export const getEnabledModules = async () => {
  try {
    const response = await get('/api/v1/modules/enabled')
    if (response.code === 200) {
      return response.data
    }
    console.warn('获取启用模块列表失败:', response.message)
    return null
  } catch (error) {
    console.error('获取启用模块列表失败:', error)
    return null
  }
}

/**
 * 检查模块是否启用
 * @param {string} moduleName 模块名称
 * @returns {Promise<boolean>} 模块是否启用
 */
export const isModuleEnabled = async (moduleName) => {
  const enabledModules = await getEnabledModules()
  if (enabledModules) {
    return enabledModules[moduleName] === true
  }
  // 默认启用（后端不可用时）
  return true
}

export default {
  getAllModulesConfig,
  getEnabledModules,
  isModuleEnabled
}
