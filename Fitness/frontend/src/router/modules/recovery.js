/**
 * 恢复模块路由配置
 * Recovery Module Routes - handles recovery status and training suggestions
 * 
 * @module router/modules/recovery
 */

export const recoveryRoutes = [
  {
    path: '/recovery',
    name: 'Recovery',
    component: () => import('@/modules/recovery/views/RecoveryStatusView.vue'),
    meta: {
      title: '恢复状态',
      requiresAuth: true,
      module: 'recovery',
      icon: 'Heart'
    }
  }
]

export default recoveryRoutes
