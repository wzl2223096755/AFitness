/**
 * 设置模块路由配置
 * Settings Module Routes - handles user settings and preferences
 * 
 * @module router/modules/settings
 * Requirements: 4.4, 7.1
 */

export const settingsRoutes = [
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/modules/settings/views/SettingsView.vue'),
    meta: {
      title: '设置',
      requiresAuth: true,
      module: 'settings',
      icon: 'Setting'
    }
  }
]

export default settingsRoutes
