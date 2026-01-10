/**
 * 恢复模块
 * Recovery Module - handles recovery status tracking and training suggestions
 * 
 * 模块结构:
 * - views/: 视图组件 (RecoveryStatusView)
 * - components/: 子组件
 * - api/: API 接口 (recoveryApi)
 */

// 导出视图
export * from './views'

// 导出组件
export * from './components'

// 导出 API
export * from './api'

// 模块元信息
export const moduleInfo = {
  name: 'recovery',
  displayName: '恢复管理',
  icon: 'Heart',
  order: 4,
  permissions: ['recovery:read', 'recovery:write'],
  version: '1.0.0',
  description: '恢复状态追踪、训练建议和疲劳管理模块'
}

// 模块路由配置
export const routes = [
  {
    path: '/recovery',
    name: 'Recovery',
    component: () => import('./views/RecoveryStatusView.vue'),
    meta: {
      title: '恢复状态',
      requiresAuth: true,
      module: 'recovery',
      icon: 'Heart'
    }
  }
]

// 默认导出模块配置
export default {
  moduleInfo,
  routes
}
