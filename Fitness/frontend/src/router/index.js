import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 路由懒加载配置
 * 使用动态导入实现代码分割，减少首屏加载时间
 * 每个路由组件会被打包成独立的chunk
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import(/* webpackChunkName: "auth" */ '../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import(/* webpackChunkName: "dashboard" */ '../views/Dashboard.vue'),
    meta: { title: '仪表盘', requiresAuth: true }
  },
  {
    path: '/training-data',
    name: 'TrainingData',
    component: () => import(/* webpackChunkName: "training" */ '../views/TrainingData.vue'),
    meta: { title: '训练数据', requiresAuth: true }
  },
  {
    path: '/load-analysis',
    name: 'LoadAnalysis',
    component: () => import(/* webpackChunkName: "training" */ '../views/LoadAnalysis.vue'),
    meta: { title: '负荷分析', requiresAuth: true }
  },
  {
    path: '/training-plan-display',
    name: 'TrainingPlanDisplay',
    component: () => import(/* webpackChunkName: "training" */ '../views/TrainingPlanDisplay.vue'),
    meta: { title: '训练计划展示', requiresAuth: true }
  },
  {
    path: '/mobile-dashboard',
    name: 'MobileDashboard',
    component: () => import(/* webpackChunkName: "mobile" */ '../views/MobileDashboard.vue'),
    meta: { title: '用户数据看板', requiresAuth: true }
  },
  {
    path: '/recovery-status',
    name: 'RecoveryStatus',
    component: () => import(/* webpackChunkName: "recovery" */ '../views/RecoveryStatus.vue'),
    meta: { title: '恢复状态', requiresAuth: true }
  },
  {
    path: '/training-suggestions',
    name: 'TrainingSuggestions',
    component: () => import(/* webpackChunkName: "training" */ '../views/TrainingSuggestions.vue'),
    meta: { title: '训练建议', requiresAuth: true }
  },
  {
    path: '/history-stats',
    name: 'HistoryStatistics',
    component: () => import(/* webpackChunkName: "statistics" */ '../views/HistoryStatistics.vue'),
    meta: { title: '历史统计', requiresAuth: true }
  },
  {
    path: '/user-profile',
    name: 'UserProfile',
    component: () => import(/* webpackChunkName: "user" */ '../views/UserProfile.vue'),
    meta: { title: '用户资料', requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import(/* webpackChunkName: "settings" */ '../views/Settings.vue'),
    meta: { title: '设置', requiresAuth: true }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import(/* webpackChunkName: "admin" */ '../views/Users.vue'),
    meta: { title: '用户管理', requiresAuth: true }
  },
  {
    path: '/training-records',
    name: 'TrainingRecords',
    component: () => import(/* webpackChunkName: "training" */ '../components/TrainingRecordManager.vue'),
    meta: { title: '训练记录管理', requiresAuth: true }
  },
  {
    path: '/data-visualization',
    name: 'DataVisualization',
    component: () => import(/* webpackChunkName: "visualization" */ '../components/DataVisualization.vue'),
    meta: { title: '数据可视化', requiresAuth: true }
  },
  {
    path: '/user-profile-manage',
    name: 'UserProfileManage',
    component: () => import(/* webpackChunkName: "user" */ '../components/UserProfile.vue'),
    meta: { title: '个人资料管理', requiresAuth: true }
  },
  {
    path: '/fitness-planner',
    name: 'FitnessPlanner',
    component: () => import(/* webpackChunkName: "planner" */ '../components/FitnessPlanner.vue'),
    meta: { title: '健身计划制定', requiresAuth: true }
  },
  {
    path: '/nutrition-tracking',
    name: 'NutritionTracking',
    component: () => import(/* webpackChunkName: "nutrition" */ '../views/NutritionTracking.vue'),
    meta: { title: '营养追踪', requiresAuth: true }
  },
  // 404页面 - 使用懒加载
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import(/* webpackChunkName: "error" */ '../views/NotFound.vue'),
    meta: { title: '页面未找到', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 力量训练负荷与恢复监控系统` : '力量训练负荷与恢复监控系统'
  
  // 检查是否需要认证
  const token = localStorage.getItem('token')
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !token) {
    // 需要认证但没有token，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && (token && isLoggedIn)) {
    // 已登录用户访问登录页，跳转到仪表盘
    next('/dashboard')
  } else {
    next()
  }
})

export default router
