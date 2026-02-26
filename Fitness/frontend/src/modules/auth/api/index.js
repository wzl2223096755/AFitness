/**
 * 认证模块 API
 * Auth module API - re-exports from api/auth.js for module encapsulation
 */

// 从根 API 导入认证API
import { authApi as sharedAuthApi } from '../../api/auth.js'

// 导出认证API
export const authApi = sharedAuthApi

// 为方便使用，也导出各个方法
export const {
  login,
  logout,
  refreshToken
} = sharedAuthApi

export default authApi
