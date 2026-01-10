/**
 * 公共路由配置
 * Common Routes - handles redirects and 404 pages
 * 
 * @module router/modules/common
 * Requirements: 4.4, 7.1
 */

export const commonRoutes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面未找到',
      requiresAuth: false
    }
  }
]

export default commonRoutes
