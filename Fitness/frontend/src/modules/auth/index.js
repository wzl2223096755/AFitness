/**
 * 认证模块
 * Auth Module - handles user authentication, registration, and password recovery
 * 
 * 模块结构:
 * - views/: 视图组件 (LoginView)
 * - components/: 子组件 (RegisterModal, ForgotPasswordModal)
 * - api/: API 接口
 * - stores/: Pinia Store
 */

// 导出视图
export * from './views'

// 导出组件
export * from './components'

// 导出 API
export * from './api'

// 导出 Store
export * from './stores'

// 模块元信息
export const moduleInfo = {
  name: 'auth',
  displayName: '认证管理',
  icon: 'Lock',
  order: 0,
  permissions: [],
  version: '1.0.0',
  description: '用户认证、注册和密码管理模块'
}

// 模块路由配置
export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/LoginView.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      layout: 'auth'
    }
  }
]

// 默认导出模块配置
export default {
  moduleInfo,
  routes
}
