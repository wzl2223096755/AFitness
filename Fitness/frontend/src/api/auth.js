import { get, post, put, del } from './request.js'

export const authApi = {
  // 用户登录
  login: (data) => post('/api/v1/auth/login', data),
  
  // 用户登出
  logout: () => post('/api/v1/auth/logout'),
  
  // 用户注册
  register: (data) => post('/api/v1/auth/register', data),
  
  // 刷新token
  refreshToken: (data) => post('/api/v1/auth/refresh', data),
  
  // 忘记密码
  forgotPassword: (data) => post('/api/v1/auth/forgot-password', data),
  
  // 重置密码
  resetPassword: (data) => post('/api/v1/auth/reset-password', data),
  
  // 验证token
  verifyToken: (token) => get('/api/v1/auth/verify', { token }),
  
  // 获取当前用户信息
  getCurrentUser: () => get('/api/v1/auth/me'),
  
  // 检查用户名是否存在
  checkUsername: (username) => get('/api/v1/auth/check-username', { username }),
  
  // 检查邮箱是否存在
  checkEmail: (email) => get('/api/v1/auth/check-email', { email }),
  
  // 发送验证码
  sendVerificationCode: (data) => post('/api/v1/auth/send-code', data),
  
  // 验证验证码
  verifyCode: (data) => post('/api/v1/auth/verify-code', data),
  
  // 绑定第三方账号
  bindThirdParty: (provider, data) => post(`/api/v1/auth/bind/${provider}`, data),
  
  // 解绑第三方账号
  unbindThirdParty: (provider) => del(`/api/v1/auth/unbind/${provider}`)
}