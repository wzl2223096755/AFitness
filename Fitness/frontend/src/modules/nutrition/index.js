/**
 * 营养模块
 * Nutrition Module - handles nutrition tracking, records, and analysis
 * 
 * 模块结构:
 * - views/: 视图组件 (NutritionTrackingView)
 * - components/: 子组件
 * - api/: API 接口 (nutritionApi)
 */

// 导出视图
export * from './views'

// 导出组件
export * from './components'

// 导出 API
export * from './api'

// 模块元信息
export const moduleInfo = {
  name: 'nutrition',
  displayName: '营养管理',
  icon: 'Food',
  order: 3,
  permissions: ['nutrition:read', 'nutrition:write'],
  version: '1.0.0',
  description: '营养追踪、饮食记录和营养分析模块'
}

// 模块路由配置
export const routes = [
  {
    path: '/nutrition-tracking',
    name: 'NutritionTracking',
    component: () => import('./views/NutritionTrackingView.vue'),
    meta: {
      title: '营养追踪',
      requiresAuth: true,
      module: 'nutrition',
      icon: 'Food'
    }
  }
]

// 默认导出模块配置
export default {
  moduleInfo,
  routes
}
