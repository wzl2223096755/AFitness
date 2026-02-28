/**
 * 仪表盘模块路由配置
 * Dashboard Module Routes - handles main dashboard and mobile dashboard views
 * 
 * @module router/modules/dashboard
 * Requirements: 4.4, 7.1
 */

export const dashboardRoutes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../../modules/dashboard/views/DashboardView.vue'),
    meta: {
      title: '仪表盘',
      requiresAuth: true,
      role: 'USER',
      module: 'dashboard',
      icon: 'Odometer'
    }
  },
  {
    path: '/mobile-dashboard',
    name: 'MobileDashboard',
    component: () => import('../../views/MobileDashboard.vue'),
    meta: {
      title: '用户数据看板',
      requiresAuth: true,
      role: 'USER',
      module: 'dashboard',
      icon: 'Monitor'
    }
  }
]

export default dashboardRoutes
