/**
 * 用户模块路由配置
 * User Module Routes - handles user profile and account management
 * 
 * @module router/modules/user
 * Requirements: 4.4, 7.1
 */

export const userRoutes = [
  {
    path: '/user-profile',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: {
      title: '用户资料',
      requiresAuth: true,
      role: 'USER',
      module: 'user',
      icon: 'User'
    }
  },
  {
    path: '/user-profile-manage',
    name: 'UserProfileManage',
    component: () => import('@/components/UserProfile.vue'),
    meta: {
      title: '个人资料管理',
      requiresAuth: true,
      role: 'USER',
      module: 'user',
      icon: 'UserFilled'
    }
  }
]

export default userRoutes
