/**
 * 管理模块路由配置
 * Admin Module Routes - handles admin dashboard, user management, and system settings
 * 
 * @module router/modules/admin
 * Requirements: 4.4, 7.1
 */

export const adminRoutes = [
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/AdminDashboard.vue'),
    meta: {
      title: '管理后台',
      requiresAuth: true,
      role: 'ADMIN',
      module: 'admin',
      icon: 'Platform'
    }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true,
      role: 'ADMIN',
      module: 'admin',
      icon: 'User'
    }
  },
  {
    path: '/admin/stats',
    name: 'SystemStats',
    component: () => import('@/views/admin/SystemStats.vue'),
    meta: {
      title: '系统统计',
      requiresAuth: true,
      role: 'ADMIN',
      module: 'admin',
      icon: 'DataLine'
    }
  },
  {
    path: '/admin/audit-logs',
    name: 'AuditLogs',
    component: () => import('@/views/admin/AuditLogs.vue'),
    meta: {
      title: '审计日志',
      requiresAuth: true,
      role: 'ADMIN',
      module: 'admin',
      icon: 'Document'
    }
  },
  {
    path: '/admin/settings',
    name: 'SystemSettings',
    component: () => import('@/views/admin/SystemSettings.vue'),
    meta: {
      title: '系统设置',
      requiresAuth: true,
      role: 'ADMIN',
      module: 'admin',
      icon: 'Setting'
    }
  }
]

export default adminRoutes
