/**
 * 仪表盘模块
 * Dashboard Module - handles main dashboard view with stats, quick actions, and recent activity
 * 
 * 模块结构:
 * - views/: 视图组件 (DashboardView)
 * - components/: 子组件 (WelcomeSection, QuickStats)
 * - api/: API 接口 (fitnessApi)
 */

// 导出视图
export * from './views'

// 导出组件
export * from './components'

// 导出 API
export * from './api'

// 模块元信息
export const moduleInfo = {
  name: 'dashboard',
  displayName: '仪表盘',
  icon: 'Odometer',
  order: 1,
  permissions: ['dashboard:read'],
  version: '1.0.0',
  description: '主仪表盘，显示训练统计、快捷操作和最近活动'
}

// 模块路由配置
export const routes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('./views/DashboardView.vue'),
    meta: {
      title: '仪表盘',
      requiresAuth: true,
      module: 'dashboard',
      icon: 'Odometer'
    }
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

// 默认导出模块配置
export default {
  moduleInfo,
  routes
}
