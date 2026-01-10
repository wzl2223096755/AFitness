/**
 * 营养模块路由配置
 * Nutrition Module Routes - handles nutrition tracking and analysis
 * 
 * @module router/modules/nutrition
 * Requirements: 4.4, 7.1
 */

export const nutritionRoutes = [
  {
    path: '/nutrition-tracking',
    name: 'NutritionTracking',
    component: () => import('@/modules/nutrition/views/NutritionTrackingView.vue'),
    meta: {
      title: '营养追踪',
      requiresAuth: true,
      role: 'USER',
      module: 'nutrition',
      icon: 'Food'
    }
  }
]

export default nutritionRoutes
