/**
 * 共享API模块入口
 * 导出所有API模块供用户端和管理端使用
 */

// 基础请求模块
export * from './request.js'
export { default as request } from './request.js'

// 认证API
export * from './auth.js'

// 健身API
export * from './fitness.js'

// 管理端API
export * from './admin.js'

// 缓存API
export * from './cachedApi.js'
export * from './apiCache.js'

// 模块配置API
export * from './modules.js'
