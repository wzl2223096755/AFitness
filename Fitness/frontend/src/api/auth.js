/**
 * 认证 API - 模拟版本（用于 GitHub Pages 演示）
 */
import { ElMessage } from 'element-plus'

// 模拟用户数据
const mockUsers = [
  {
    username: 'admin',
    password: 'admin',
    role: 'ADMIN',
    userId: 1,
    nickname: '管理员',
    accessToken: 'mock-admin-token',
    refreshToken: 'mock-admin-refresh'
  },
  {
    username: 'user',
    password: 'user',
    role: 'USER',
    userId: 2,
    nickname: '普通用户',
    accessToken: 'mock-user-token',
    refreshToken: 'mock-user-refresh'
  }
]

export const authApi = {
  /**
   * 模拟登录
   */
  async login(credentials) {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const user = mockUsers.find(u => 
      u.username === credentials.username && 
      u.password === credentials.password
    )
    
    if (user) {
      return {
        code: 200,
        data: {
          accessToken: user.accessToken,
          refreshToken: user.refreshToken,
          userId: user.userId,
          username: user.username,
          role: user.role,
          nickname: user.nickname
        },
        message: '登录成功'
      }
    } else {
      return {
        code: 401,
        message: '用户名或密码错误'
      }
    }
  },

  /**
   * 模拟刷新令牌
   */
  async refreshToken() {
    await new Promise(resolve => setTimeout(resolve, 500))
    return {
      code: 200,
      data: {
        accessToken: 'mock-new-token',
        refreshToken: 'mock-new-refresh'
      }
    }
  },

  /**
   * 模拟登出
   */
  async logout() {
    await new Promise(resolve => setTimeout(resolve, 300))
    return {
      code: 200,
      message: '登出成功'
    }
  }
}
