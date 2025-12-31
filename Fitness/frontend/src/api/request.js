import axios from 'axios'
import { ElMessage } from '../utils/message.js'
import { PerformanceUtils } from '../utils/performance.js'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 移除baseURL，直接使用vite代理
  timeout: 10000 // 请求超时时间
})

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
      return newToken
    }
    throw new Error('刷新token失败')
  } catch (error) {
    // 刷新失败，清除登录状态
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('isLoggedIn')
    throw error
  }
}

// 请求拦截器
service.interceptors.request.use(
  config => {
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
    console.error('响应错误:', error)
    
    // 处理网络错误
    if (!error.response) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 处理HTTP错误码
      const status = error.response.status
      switch (status) {
        case 401:
          // 处理token过期
          const originalRequest = error.config
          
          // 避免在登录页面或刷新请求中重复处理
          if (window.location.hash.includes('/login') || originalRequest.url.includes('/auth/refresh')) {
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
              // 刷新失败，跳转登录页
              ElMessage.error('登录已过期，请重新登录')
              setTimeout(() => {
                window.location.href = '/#/login'
              }, 1500)
            } finally {
              isRefreshing = false
            }
          } else {
            ElMessage.error('未授权，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            localStorage.removeItem('isLoggedIn')
            setTimeout(() => {
              window.location.href = '/#/login'
            }, 1500)
          }
          break
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

// 重试机制
const retryRequest = async (requestFn, maxRetries = 3, delay = 1000) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await requestFn()
    } catch (error) {
      if (i === maxRetries - 1) throw error
      
      // 对网络错误和服务器错误进行重试
      if (!error.response || (error.response && [500, 502, 503, 504].includes(error.response.status))) {
        console.log(`请求失败，${delay}ms后重试 (${i + 1}/${maxRetries})`)
        await new Promise(resolve => setTimeout(resolve, delay))
        delay *= 2 // 指数退避
      } else {
        throw error
      }
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