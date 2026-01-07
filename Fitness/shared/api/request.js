import axios from 'axios'
import { ElMessage, ElMessageBox } from '../utils/message.js'
import { PerformanceUtils } from '../utils/performance.js'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 移除baseURL，直接使用vite代理
  timeout: 30000, // 请求超时时间增加到30秒
  withCredentials: true // 携带凭证（cookies, authorization headers）
})

// 请求取消控制器映射
const pendingRequests = new Map()

// 会话过期状态标志（防止重复弹窗）
let isSessionExpiredHandled = false

// 生成请求唯一标识
const generateRequestKey = (config) => {
  const { method, url, params, data } = config
  return `${method}-${url}-${JSON.stringify(params)}-${JSON.stringify(data)}`
}

// 添加请求到待处理列表
const addPendingRequest = (config) => {
  const requestKey = generateRequestKey(config)
  if (!pendingRequests.has(requestKey)) {
    const controller = new AbortController()
    config.signal = controller.signal
    pendingRequests.set(requestKey, controller)
  }
}

// 移除已完成的请求
const removePendingRequest = (config) => {
  const requestKey = generateRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    pendingRequests.delete(requestKey)
  }
}

// 取消所有待处理请求
export const cancelAllRequests = () => {
  pendingRequests.forEach((controller) => {
    controller.abort()
  })
  pendingRequests.clear()
}

// 是否正在刷新token
let isRefreshing = false
// 等待刷新token的请求队列
let refreshSubscribers = []

// 添加等待刷新token的请求到队列
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback)
}

// 刷新token后重试队列中的请求
const onRefreshed = (newToken) => {
  refreshSubscribers.forEach(callback => callback(newToken))
  refreshSubscribers = []
}

/**
 * 清除登录状态
 */
const clearAuthState = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('isLoggedIn')
}

/**
 * 获取登录页面URL
 * 支持hash路由和history路由
 */
const getLoginUrl = () => {
  // 检测当前使用的路由模式
  if (window.location.hash) {
    return '/#/login'
  }
  return '/login'
}

/**
 * 检查是否在登录页面
 */
const isOnLoginPage = () => {
  const path = window.location.pathname
  const hash = window.location.hash
  return path.includes('/login') || hash.includes('/login')
}

/**
 * 处理会话过期
 * @param {boolean} showDialog - 是否显示确认对话框
 */
const handleSessionExpired = async (showDialog = true) => {
  // 防止重复处理
  if (isSessionExpiredHandled) {
    return
  }
  
  // 如果已经在登录页，不需要处理
  if (isOnLoginPage()) {
    return
  }
  
  isSessionExpiredHandled = true
  
  // 清除登录状态
  clearAuthState()
  
  // 取消所有待处理请求
  cancelAllRequests()
  
  if (showDialog) {
    try {
      await ElMessageBox.alert(
        '您的登录已过期，请重新登录以继续使用。',
        '会话已过期',
        {
          confirmButtonText: '重新登录',
          type: 'warning',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
      )
    } catch (e) {
      // 用户关闭对话框
    }
  } else {
    ElMessage.warning('登录已过期，正在跳转到登录页...')
  }
  
  // 跳转到登录页
  setTimeout(() => {
    isSessionExpiredHandled = false
    window.location.href = getLoginUrl()
  }, showDialog ? 0 : 1500)
}

// 刷新token的函数
const refreshToken = async () => {
  const refreshTokenValue = localStorage.getItem('refreshToken')
  if (!refreshTokenValue) {
    throw new Error('没有刷新token')
  }
  
  try {
    const response = await axios.post('/api/v1/auth/refresh', {
      refreshToken: refreshTokenValue
    })
    
    if (response.data.code === 200 && response.data.data.accessToken) {
      const newToken = response.data.data.accessToken
      localStorage.setItem('token', newToken)
      
      // 如果返回了新的refreshToken，也更新它
      if (response.data.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.data.refreshToken)
      }
      
      return newToken
    }
    throw new Error('刷新token失败')
  } catch (error) {
    // 刷新失败，清除登录状态
    clearAuthState()
    throw error
  }
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加请求到待处理列表（用于取消）
    addPendingRequest(config)
    
    // 检查缓存（仅对GET请求）
    if (config.method === 'get' && config.cache !== false) {
      const cacheKey = `${config.url}?${JSON.stringify(config.params)}`
      const cachedData = PerformanceUtils.getCache(cacheKey)
      if (cachedData) {
        // 返回缓存的Promise
        return Promise.resolve({ data: cachedData, fromCache: true })
      }
    }

    // 从localStorage中获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 设置请求头
      config.headers['Authorization'] = `Bearer ${token}`
    }
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 移除已完成的请求
    removePendingRequest(response.config)
    
    const res = response.data
    
    // 缓存GET请求的响应（仅对成功请求）
    if (response.config.method === 'get' && 
        response.config.cache !== false && 
        !response.fromCache &&
        res.code === 200) {
      const cacheKey = `${response.config.url}?${JSON.stringify(response.config.params)}`
      PerformanceUtils.setCache(cacheKey, res, response.config.cacheTTL || 300000) // 默认5分钟
    }
    
    return res
  },
  async (error) => {
    // 移除已完成的请求
    if (error.config) {
      removePendingRequest(error.config)
    }
    
    // 处理请求被取消的情况
    if (axios.isCancel(error)) {
      console.log('请求已取消:', error.message)
      return Promise.reject(error)
    }
    
    console.error('响应错误:', error)
    
    // 处理网络错误
    if (!error.response) {
      // 判断是否是超时错误
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        ElMessage.error('请求超时，请稍后重试')
      } else {
        ElMessage.error('网络错误，请检查网络连接')
      }
    } else {
      // 处理HTTP错误码
      const status = error.response.status
      switch (status) {
        case 401: {
          // 处理token过期
          const originalRequest = error.config
          
          // 避免在登录页面或刷新请求中重复处理
          if (isOnLoginPage() || originalRequest.url.includes('/auth/refresh')) {
            break
          }
          
          // 如果正在刷新token，将请求加入队列
          if (isRefreshing) {
            return new Promise((resolve) => {
              subscribeTokenRefresh((newToken) => {
                originalRequest.headers['Authorization'] = `Bearer ${newToken}`
                resolve(service(originalRequest))
              })
            })
          }
          
          // 尝试刷新token
          const refreshTokenValue = localStorage.getItem('refreshToken')
          if (refreshTokenValue && !originalRequest._retry) {
            originalRequest._retry = true
            isRefreshing = true
            
            try {
              const newToken = await refreshToken()
              onRefreshed(newToken)
              originalRequest.headers['Authorization'] = `Bearer ${newToken}`
              return service(originalRequest)
            } catch (refreshError) {
              // 刷新失败，处理会话过期
              await handleSessionExpired(true)
            } finally {
              isRefreshing = false
            }
          } else {
            // 没有refreshToken或已经重试过，直接处理会话过期
            await handleSessionExpired(true)
          }
          break
        }
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败: ${status}`)
      }
    }
    
    return Promise.reject(error)
  }
)

// 重试机制（指数退避策略）
const retryRequest = async (requestFn, maxRetries = 3, initialDelay = 1000) => {
  let delay = initialDelay
  
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      return await requestFn()
    } catch (error) {
      // 最后一次尝试失败，抛出错误
      if (attempt === maxRetries - 1) {
        throw error
      }
      
      // 判断是否应该重试
      const shouldRetry = !error.response || // 网络错误
        (error.response && [500, 502, 503, 504, 408].includes(error.response.status)) // 服务器错误或超时
      
      if (!shouldRetry) {
        throw error
      }
      
      console.log(`请求失败，${delay}ms后重试 (${attempt + 1}/${maxRetries})`)
      await new Promise(resolve => setTimeout(resolve, delay))
      
      // 指数退避：每次重试间隔翻倍
      delay *= 2
    }
  }
}

// 封装GET请求（带缓存）
export const get = (url, params = {}, options = {}) => {
  const config = {
    params,
    cache: options.cache !== false, // 默认启用缓存
    cacheTTL: options.cacheTTL || 300000 // 默认5分钟
  }
  
  return retryRequest(() => service.get(url, config))
}

// 封装POST请求
export const post = (url, data = {}) => {
  return retryRequest(() => service.post(url, data))
}

// 封装PUT请求
export const put = (url, data = {}) => {
  return retryRequest(() => service.put(url, data))
}

// 封装PATCH请求
export const patch = (url, data = {}) => {
  return retryRequest(() => service.patch(url, data))
}

// 封装DELETE请求
export const del = (url, params = {}) => {
  return retryRequest(() => service.delete(url, { params }))
}

// 封装上传文件请求
export const upload = (url, file, onUploadProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  return retryRequest(() => service.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  }))
}

// 批量请求
export const batchRequest = async (requests) => {
  try {
    const results = await Promise.allSettled(requests)
    return results.map(result => ({
      success: result.status === 'fulfilled',
      data: result.status === 'fulfilled' ? result.value : null,
      error: result.status === 'rejected' ? result.reason : null
    }))
  } catch (error) {
    console.error('批量请求失败:', error)
    throw error
  }
}

export default service
