/**
 * 认证模块路由配置
 * Auth Module Routes - handles login, registration, and password recovery
 * 
 * @module router/modules/auth
 * Requirements: 4.4, 7.1
 */

export const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../../modules/auth/views/LoginView.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      module: 'auth',
      layout: 'auth'
    }
  }
]

export default authRoutes
