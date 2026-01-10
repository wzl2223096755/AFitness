/**
 * 认证模块 API
 * Auth module API - re-exports from shared for module encapsulation
 */

// 从共享模块导入认证API
import { authApi as sharedAuthApi } from '@shared/api/auth.js'

// 导出认证API
export const authApi = sharedAuthApi

// 为方便使用，也导出各个方法
export const {
  login,
  logout,
  register,
  refreshToken,
  forgotPassword,
  resetPassword,
  verifyToken,
  getCurrentUser,
  checkUsername,
  checkEmail,
  sendVerificationCode,
  verifyCode,
  bindThirdParty,
  unbindThirdParty
} = sharedAuthApi

export default authApi
