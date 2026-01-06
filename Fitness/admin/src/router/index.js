import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 管理端路由配置
 * 仅允许管理员角色访问
 * 非管理员用户将被拒绝并重定向到用户端
 */

// 用户端前端URL
const USER_FRONTEND_URL = 'http://localhost:3001'

const routes = [
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLogin.vue'),
    meta: { title: '管理员登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'AdminDashboard',
    component: () => import('../views/AdminDashboard.vue'),
    meta: { title: '管理仪表盘', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('../views/UserManagement.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/statistics',
    name: 'SystemStats',
    component: () => import('../views/SystemStats.vue'),
    meta: { title: '系统统计', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/audit-logs',
    name: 'AuditLogs',
    component: () => import('../views/AuditLogs.vue'),
    meta: { title: '审计日志', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/settings',
    name: 'SystemSettings',
    component: () => import('../views/SystemSettings.vue'),
    meta: { title: '系统设置', requiresAuth: true, requiresAdmin: true }
  },
  // 404页面
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
 * 检查用户是否为管理员角色
 * @returns {boolean}
 */
function isAdminRole() {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.role === 'ADMIN' || user.role === 'ROLE_ADMIN'
    }
    const role = localStorage.getItem('userRole')
    return role === 'ADMIN' || role === 'ROLE_ADMIN'
  } catch (e) {
    return false
  }
}

/**
 * 重定向非管理员到用户端前端
 */
function redirectToUserFrontend() {
  window.location.href = USER_FRONTEND_URL
}

/**
 * 处理URL中的token参数（从用户端重定向过来时）
 */
function handleTokenFromUrl() {
  const urlParams = new URLSearchParams(window.location.search)
  const token = urlParams.get('token')
  const refreshToken = urlParams.get('refreshToken')
  
  if (token) {
    localStorage.setItem('token', token)
    localStorage.setItem('isLoggedIn', 'true')
  }
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken)
  }
  
  // 清除URL中的token参数
  if (token || refreshToken) {
    const cleanUrl = window.location.origin + window.location.pathname + window.location.hash
    window.history.replaceState({}, document.title, cleanUrl)
  }
}

// 初始化时处理URL中的token
handleTokenFromUrl()

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - AFitness Admin` : 'AFitness Admin'
  
  const token = localStorage.getItem('token')
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  const requiresAuth = to.meta.requiresAuth !== false
  const requiresAdmin = to.meta.requiresAdmin === true
  
  // 检查是否需要认证
  if (requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 检查是否需要管理员权限
  if (requiresAdmin && token) {
    if (!isAdminRole()) {
      // 非管理员用户尝试访问管理端，显示错误并重定向
      console.warn('非管理员用户尝试访问管理端，将重定向到用户端')
      // 清除登录状态
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
      // 重定向到用户端
      redirectToUserFrontend()
      return
    }
  }
  
  // 已登录管理员访问登录页，跳转到仪表盘
  if (to.path === '/login' && token && isLoggedIn && isAdminRole()) {
    next('/dashboard')
    return
  }
  
  next()
})

export { USER_FRONTEND_URL, isAdminRole, redirectToUserFrontend }

export default router
