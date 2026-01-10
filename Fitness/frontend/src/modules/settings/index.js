/**
 * 设置模块
 * Settings Module - handles user settings, preferences, and data management
 * 
 * 模块结构:
 * - views/: 视图组件 (SettingsView)
 * - components/: 子组件 (ProfileEditor, PreferencesPanel)
 * - api/: API 接口 (userApi)
 */

// 导出视图
export * from './views'

// 导出组件
export * from './components'

// 导出 API
export * from './api'

// 模块元信息
export const moduleInfo = {
  name: 'settings',
  displayName: '系统设置',
  icon: 'Setting',
  order: 10,
  permissions: ['settings:read', 'settings:write'],
  version: '1.0.0',
  description: '用户设置、偏好配置和数据管理模块'
}

// 模块路由配置
export const routes = [
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('./views/SettingsView.vue'),
    meta: {
      title: '系统设置',
      requiresAuth: true,
      module: 'settings',
      icon: 'Setting'
    }
  }
]

// 默认导出模块配置
export default {
  moduleInfo,
  routes
}
