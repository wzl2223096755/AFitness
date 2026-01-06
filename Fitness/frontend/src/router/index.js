import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 统一前端路由配置
 * 支持用户端和管理端在同一个应用中
 * 根据用户角色自动跳转到对应界面
 */

const routes = [
  // ==================== 公共路由 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login'
  },
  
  // ==================== 用户端路由 ====================
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '仪表盘', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/training-data',
    name: 'TrainingData',
    component: () => import('../views/TrainingData.vue'),
    meta: { title: '训练数据', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/load-analysis',
    name: 'LoadAnalysis',
    component: () => import('../views/LoadAnalysis.vue'),
    meta: { title: '负荷分析', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/training-plan-display',
    name: 'TrainingPlanDisplay',
    component: () => import('../views/TrainingPlanDisplay.vue'),
    meta: { title: '训练计划展示', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/mobile-dashboard',
    name: 'MobileDashboard',
    component: () => import('../views/MobileDashboard.vue'),
    meta: { title: '用户数据看板', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/recovery-status',
    name: 'RecoveryStatus',
    component: () => import('../views/RecoveryStatus.vue'),
    meta: { title: '恢复状态', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/training-suggestions',
    name: 'TrainingSuggestions',
    component: () => import('../views/TrainingSuggestions.vue'),
    meta: { title: '训练建议', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/history-stats',
    name: 'HistoryStatistics',
    component: () => import('../views/HistoryStatistics.vue'),
    meta: { title: '历史统计', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/user-profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue'),
    meta: { title: '用户资料', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: { title: '设置', requiresAuth: true }
  },
  {
    path: '/training-records',
    name: 'TrainingRecords',
    component: () => import('../components/TrainingRecordManager.vue'),
    meta: { title: '训练记录管理', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/data-visualization',
    name: 'DataVisualization',
    component: () => import('../components/DataVisualization.vue'),
    meta: { title: '数据可视化', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/user-profile-manage',
    name: 'UserProfileManage',
    component: () => import('../components/UserProfile.vue'),
    meta: { title: '个人资料管理', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/fitness-planner',
    name: 'FitnessPlanner',
    component: () => import('../components/FitnessPlanner.vue'),
    meta: { title: '健身计划制定', requiresAuth: true, role: 'USER' }
  },
  {
    path: '/nutrition-tracking',
    name: 'NutritionTracking',
    component: () => import('../views/NutritionTracking.vue'),
    meta: { title: '营养追踪', requiresAuth: true, role: 'USER' }
  },

  // ==================== 管理端路由 ====================
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('../views/admin/UserManagement.vue'),
    meta: { title: '用户管理', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/stats',
    name: 'SystemStats',
    component: () => import('../views/admin/SystemStats.vue'),
    meta: { title: '系统统计', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/audit-logs',
    name: 'AuditLogs',
    component: () => import('../views/admin/AuditLogs.vue'),
    meta: { title: '审计日志', requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/settings',
    name: 'SystemSettings',
    component: () => import('../views/admin/SystemSettings.vue'),
    meta: { title: '系统设置', requiresAuth: true, role: 'ADMIN' }
  },
  
  // ==================== 404页面 ====================
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面未找到', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

/**
 * 获取用户角色
 */
function getUserRole() {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.role
    }
    return localStorage.getItem('userRole')
  } catch (e) {
    return null
  }
}

/**
 * 检查用户是否为管理员
 */
function isAdminRole() {
  const role = getUserRole()
  return role === 'ADMIN' || role === 'ROLE_ADMIN'
}

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - AFitness` : 'AFitness'
  
  const token = localStorage.getItem('token')
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  const requiresAuth = to.meta.requiresAuth !== false
  const routeRole = to.meta.role
  const userRole = getUserRole()
  const isAdmin = isAdminRole()
  
  // 1. 需要认证但没有token，跳转到登录页
  if (requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 2. 已登录用户访问登录页，根据角色跳转
  if (to.path === '/login' && token && isLoggedIn) {
    if (isAdmin) {
      next('/admin/dashboard')
    } else {
      next('/dashboard')
    }
    return
  }
  
  // 3. 检查路由角色权限
  if (routeRole && token && isLoggedIn) {
    if (routeRole === 'ADMIN' && !isAdmin) {
      // 普通用户访问管理页面，跳转到用户仪表盘
      next('/dashboard')
      return
    }
    if (routeRole === 'USER' && isAdmin) {
      // 管理员访问用户页面，允许（管理员可以查看用户功能）
      // 或者可以改为跳转到管理后台：next('/admin/dashboard')
    }
  }
  
  next()
})

export { isAdminRole, getUserRole }
export default router
