/**
 * 训练模块
 * Training Module - handles training data, plans, suggestions, and analysis
 * 
 * 模块结构:
 * - views/: 视图组件 (TrainingDataView, TrainingPlanView, etc.)
 * - components/: 子组件 (TrainingRecordManager, Calculators)
 * - api/: API 接口 (strengthTrainingApi, cardioTrainingApi)
 * - stores/: Pinia Store (useTrainingStore)
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
  name: 'training',
  displayName: '训练管理',
  icon: 'Dumbbell',
  order: 2,
  permissions: ['training:read', 'training:write'],
  version: '1.0.0',
  description: '训练数据记录、计划管理、负荷分析和训练建议模块'
}

// 模块路由配置
export const routes = [
  {
    path: '/training-data',
    name: 'TrainingData',
    component: () => import('./views/TrainingDataView.vue'),
    meta: {
      title: '训练数据',
      requiresAuth: true,
      module: 'training',
      icon: 'DataLine'
    }
  },
  {
    path: '/training-plan',
    name: 'TrainingPlan',
    component: () => import('./views/TrainingPlanView.vue'),
    meta: {
      title: '训练计划',
      requiresAuth: true,
      module: 'training',
      icon: 'Calendar'
    }
  },
  {
    path: '/training-suggestions',
    name: 'TrainingSuggestions',
    component: () => import('./views/TrainingSuggestionsView.vue'),
    meta: {
      title: '训练建议',
      requiresAuth: true,
      module: 'training',
      icon: 'TrendCharts'
    }
  },
  {
    path: '/history-statistics',
    name: 'HistoryStatistics',
    component: () => import('./views/HistoryStatisticsView.vue'),
    meta: {
      title: '历史统计',
      requiresAuth: true,
      module: 'training',
      icon: 'Histogram'
    }
  },
  {
    path: '/load-analysis',
    name: 'LoadAnalysis',
    component: () => import('./views/LoadAnalysisView.vue'),
    meta: {
      title: '负荷分析',
      requiresAuth: true,
      module: 'training',
      icon: 'DataAnalysis'
    }
  }
]

// 默认导出模块配置
export default {
  moduleInfo,
  routes
}
