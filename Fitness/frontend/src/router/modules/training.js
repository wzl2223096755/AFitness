/**
 * 训练模块路由配置
 * Training Module Routes - handles training data, plans, suggestions, and analysis
 * 
 * @module router/modules/training
 * Requirements: 4.4, 7.1
 */

export const trainingRoutes = [
  {
    path: '/training-data',
    name: 'TrainingData',
    component: () => import('@/modules/training/views/TrainingDataView.vue'),
    meta: {
      title: '训练数据',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'DataLine'
    }
  },
  {
    path: '/load-analysis',
    name: 'LoadAnalysis',
    component: () => import('@/modules/training/views/LoadAnalysisView.vue'),
    meta: {
      title: '负荷分析',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'DataAnalysis'
    }
  },
  {
    path: '/training-plan-display',
    name: 'TrainingPlanDisplay',
    component: () => import('@/modules/training/views/TrainingPlanView.vue'),
    meta: {
      title: '训练计划展示',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'Calendar'
    }
  },
  {
    path: '/training-suggestions',
    name: 'TrainingSuggestions',
    component: () => import('@/modules/training/views/TrainingSuggestionsView.vue'),
    meta: {
      title: '训练建议',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'TrendCharts'
    }
  },
  {
    path: '/history-stats',
    name: 'HistoryStatistics',
    component: () => import('@/modules/training/views/HistoryStatisticsView.vue'),
    meta: {
      title: '历史统计',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'Histogram'
    }
  },
  {
    path: '/training-records',
    name: 'TrainingRecords',
    component: () => import('@/modules/training/components/TrainingRecordManager.vue'),
    meta: {
      title: '训练记录管理',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'List'
    }
  },
  {
    path: '/data-visualization',
    name: 'DataVisualization',
    component: () => import('@/components/DataVisualization.vue'),
    meta: {
      title: '数据可视化',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'PieChart'
    }
  },
  {
    path: '/fitness-planner',
    name: 'FitnessPlanner',
    component: () => import('@/components/FitnessPlanner.vue'),
    meta: {
      title: '健身计划制定',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'EditPen'
    }
  },
  {
    path: '/recovery-status',
    name: 'RecoveryStatus',
    component: () => import('@/views/RecoveryStatus.vue'),
    meta: {
      title: '恢复状态',
      requiresAuth: true,
      role: 'USER',
      module: 'training',
      icon: 'Timer'
    }
  }
]

export default trainingRoutes
