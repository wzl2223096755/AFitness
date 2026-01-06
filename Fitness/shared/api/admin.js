import { get, post, put, del } from './request.js'

/**
 * 管理端API模块
 * 提供用户管理、系统统计、审计日志和系统设置等管理功能
 */
export const adminApi = {
  // ==================== 用户管理 ====================
  
  /**
   * 获取用户列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.role - 角色筛选
   * @param {string} params.status - 状态筛选
   * @returns {Promise<any>}
   */
  getUsers: (params = {}) => get('/api/v1/admin/users', params),
  
  /**
   * 根据ID获取用户详情
   * @param {number} id - 用户ID
   * @returns {Promise<any>}
   */
  getUserById: (id) => get(`/api/v1/admin/users/${id}`),
  
  /**
   * 更新用户信息
   * @param {number} id - 用户ID
   * @param {Object} data - 用户数据
   * @returns {Promise<any>}
   */
  updateUser: (id, data) => put(`/api/v1/admin/users/${id}`, data),
  
  /**
   * 删除用户
   * @param {number} id - 用户ID
   * @returns {Promise<any>}
   */
  deleteUser: (id) => del(`/api/v1/admin/users/${id}`),
  
  /**
   * 禁用/启用用户
   * @param {number} id - 用户ID
   * @param {boolean} enabled - 是否启用
   * @returns {Promise<any>}
   */
  toggleUserStatus: (id, enabled) => put(`/api/v1/admin/users/${id}/status`, { enabled }),
  
  /**
   * 重置用户密码
   * @param {number} id - 用户ID
   * @returns {Promise<any>}
   */
  resetUserPassword: (id) => post(`/api/v1/admin/users/${id}/reset-password`),
  
  // ==================== 系统统计 ====================
  
  /**
   * 获取系统统计数据
   * @returns {Promise<any>}
   */
  getSystemStats: () => get('/api/v1/admin/stats'),
  
  /**
   * 获取用户增长趋势
   * @param {Object} params - 查询参数
   * @param {string} params.startDate - 开始日期
   * @param {string} params.endDate - 结束日期
   * @returns {Promise<any>}
   */
  getUserGrowthTrend: (params = {}) => get('/api/v1/admin/stats/user-growth', params),
  
  /**
   * 获取活跃用户统计
   * @param {Object} params - 查询参数
   * @param {string} params.period - 统计周期 (day/week/month)
   * @returns {Promise<any>}
   */
  getActiveUserStats: (params = {}) => get('/api/v1/admin/stats/active-users', params),
  
  /**
   * 获取训练数据统计
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  getTrainingStats: (params = {}) => get('/api/v1/admin/stats/training', params),
  
  // ==================== 审计日志 ====================
  
  /**
   * 获取审计日志列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.action - 操作类型筛选
   * @param {string} params.userId - 用户ID筛选
   * @param {string} params.startDate - 开始日期
   * @param {string} params.endDate - 结束日期
   * @returns {Promise<any>}
   */
  getAuditLogs: (params = {}) => get('/api/v1/admin/audit-logs', params),
  
  /**
   * 获取审计日志详情
   * @param {number} id - 日志ID
   * @returns {Promise<any>}
   */
  getAuditLogById: (id) => get(`/api/v1/admin/audit-logs/${id}`),
  
  /**
   * 导出审计日志
   * @param {Object} params - 查询参数
   * @returns {Promise<any>}
   */
  exportAuditLogs: (params = {}) => get('/api/v1/admin/audit-logs/export', params),
  
  // ==================== 系统设置 ====================
  
  /**
   * 获取系统设置
   * @returns {Promise<any>}
   */
  getSettings: () => get('/api/v1/admin/settings'),
  
  /**
   * 更新系统设置
   * @param {Object} data - 设置数据
   * @returns {Promise<any>}
   */
  updateSettings: (data) => put('/api/v1/admin/settings', data),
  
  /**
   * 获取系统健康状态
   * @returns {Promise<any>}
   */
  getSystemHealth: () => get('/api/v1/admin/health'),
  
  /**
   * 清除系统缓存
   * @returns {Promise<any>}
   */
  clearCache: () => post('/api/v1/admin/cache/clear')
}

export default adminApi
