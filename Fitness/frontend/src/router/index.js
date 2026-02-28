import { createRouter, createWebHashHistory } from 'vue-router'

import { mergeModuleRoutes, filterRoutesByModuleConfig } from './modules'



/**

 * 统一前端路由配置

 * 支持用户端和管理端在同一个应用中

 * 根据用户角色自动跳转到对应界面

 * 支持根据后端模块配置动态过滤路由

 * 

 * 模块化路由结构:

 * - modules/auth.js: 认证相关路由

 * - modules/dashboard.js: 仪表盘路由

 * - modules/training.js: 训练模块路由

 * - modules/nutrition.js: 营养模块路由

 * - modules/user.js: 用户模块路由

 * - modules/settings.js: 设置模块路由

 * - modules/admin.js: 管理模块路由

 * - modules/common.js: 公共路由（重定向、404等）

 * 

 * Requirements: 4.4, 6.2, 7.1

 */



// 合并所有模块路由

const routes = mergeModuleRoutes()



const router = createRouter({

  history: createWebHashHistory(),

  routes

})



/**

 * 根据模块配置更新路由

 * @param {Object} enabledModules - 模块启用状态对象 { moduleName: boolean }

 */

export const updateRoutesByModuleConfig = (enabledModules) => {

  // 获取过滤后的路由

  const filteredRoutes = filterRoutesByModuleConfig(enabledModules)

  

  // 移除所有现有路由

  router.getRoutes().forEach(route => {

    if (route.name) {

      router.removeRoute(route.name)

    }

  })

  

  // 添加过滤后的路由

  filteredRoutes.forEach(route => {

    router.addRoute(route)

  })

  

  console.log('[Router] 路由已根据模块配置更新')

}



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

