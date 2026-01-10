/**
 * 模块路由聚合导出
 * Module Routes Index - aggregates all module routes for the main router
 * 
 * @module router/modules
 * Requirements: 4.4, 6.2, 7.1
 */

import { authRoutes } from './auth'
import { dashboardRoutes } from './dashboard'
import { trainingRoutes } from './training'
import { nutritionRoutes } from './nutrition'
import { recoveryRoutes } from './recovery'
import { userRoutes } from './user'
import { settingsRoutes } from './settings'
import { adminRoutes } from './admin'
import { commonRoutes } from './common'

// 导出各模块路由
export {
  authRoutes,
  dashboardRoutes,
  trainingRoutes,
  nutritionRoutes,
  recoveryRoutes,
  userRoutes,
  settingsRoutes,
  adminRoutes,
  commonRoutes
}

/**
 * 模块路由映射
 * 将后端模块名称映射到前端路由
 */
const moduleRouteMap = {
  user: [...authRoutes, ...userRoutes, ...settingsRoutes],
  training: trainingRoutes,
  nutrition: nutritionRoutes,
  recovery: recoveryRoutes,
  admin: adminRoutes,
  dashboard: dashboardRoutes
}

/**
 * 合并所有模块路由
 * 路由顺序：认证 -> 仪表盘 -> 训练 -> 营养 -> 恢复 -> 用户 -> 设置 -> 管理 -> 公共
 * 注意：公共路由（包含404）应放在最后
 */
export const mergeModuleRoutes = () => {
  return [
    ...authRoutes,
    ...dashboardRoutes,
    ...trainingRoutes,
    ...nutritionRoutes,
    ...recoveryRoutes,
    ...userRoutes,
    ...settingsRoutes,
    ...adminRoutes,
    ...commonRoutes
  ]
}

/**
 * 根据模块配置过滤路由
 * @param {Object} enabledModules - 模块启用状态对象 { moduleName: boolean }
 * @returns {Array} 过滤后的路由数组
 */
export const filterRoutesByModuleConfig = (enabledModules) => {
  const routes = []
  
  // 认证路由始终包含（用户模块的一部分）
  if (enabledModules.user !== false) {
    routes.push(...authRoutes)
  }
  
  // 仪表盘路由（始终包含，作为基础功能）
  routes.push(...dashboardRoutes)
  
  // 训练模块路由
  if (enabledModules.training !== false) {
    routes.push(...trainingRoutes)
  }
  
  // 营养模块路由
  if (enabledModules.nutrition !== false) {
    routes.push(...nutritionRoutes)
  }
  
  // 恢复模块路由
  if (enabledModules.recovery !== false) {
    routes.push(...recoveryRoutes)
  }
  
  // 用户模块路由
  if (enabledModules.user !== false) {
    routes.push(...userRoutes)
    routes.push(...settingsRoutes)
  }
  
  // 管理模块路由
  if (enabledModules.admin !== false) {
    routes.push(...adminRoutes)
  }
  
  // 公共路由始终包含（404等）
  routes.push(...commonRoutes)
  
  return routes
}

/**
 * 获取指定模块的路由
 * @param {string} moduleName - 模块名称
 * @returns {Array} 模块路由数组
 */
export const getModuleRoutes = (moduleName) => {
  const moduleMap = {
    auth: authRoutes,
    dashboard: dashboardRoutes,
    training: trainingRoutes,
    nutrition: nutritionRoutes,
    recovery: recoveryRoutes,
    user: userRoutes,
    settings: settingsRoutes,
    admin: adminRoutes,
    common: commonRoutes
  }
  return moduleMap[moduleName] || []
}

/**
 * 获取所有模块名称
 * @returns {Array<string>} 模块名称数组
 */
export const getModuleNames = () => {
  return ['auth', 'dashboard', 'training', 'nutrition', 'recovery', 'user', 'settings', 'admin', 'common']
}

/**
 * 获取后端模块对应的前端路由模块名称
 * @param {string} backendModuleName - 后端模块名称
 * @returns {Array<string>} 前端路由模块名称数组
 */
export const getRouteModulesForBackendModule = (backendModuleName) => {
  const mapping = {
    user: ['auth', 'user', 'settings'],
    training: ['training'],
    nutrition: ['nutrition'],
    recovery: ['recovery'],
    admin: ['admin']
  }
  return mapping[backendModuleName] || []
}

export default {
  authRoutes,
  dashboardRoutes,
  trainingRoutes,
  nutritionRoutes,
  recoveryRoutes,
  userRoutes,
  settingsRoutes,
  adminRoutes,
  commonRoutes,
  mergeModuleRoutes,
  filterRoutesByModuleConfig,
  getModuleRoutes,
  getModuleNames,
  getRouteModulesForBackendModule
}
