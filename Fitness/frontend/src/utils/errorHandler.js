// 全局错误处理器
class ErrorHandler {
  static handle(error, context = '') {
    console.error(`[Error${context ? ` in ${context}` : ''}]:`, error)
    
    // 如果是API响应错误
    if (error.response) {
      return this.handleApiError(error.response, context)
    }
    
    // 如果是网络错误
    if (error.request) {
      return this.handleNetworkError(error, context)
    }
    
    // 其他错误
    return this.handleGenericError(error, context)
  }
  
  static handleApiError(response, context) {
    const { status, data } = response
    let message = '请求失败'
    
    // 根据状态码和响应数据提供更具体的错误信息
    switch (status) {
      case 400:
        message = data?.message || '请求参数错误'
        break
      case 401:
        message = '登录已过期，请重新登录'
        this.handleAuthError()
        break
      case 403:
        message = '没有权限执行此操作'
        break
      case 404:
        message = '请求的资源不存在'
        break
      case 409:
        message = data?.message || '数据冲突，请刷新后重试'
        break
      case 422:
        message = data?.message || '数据验证失败'
        break
      case 429:
        message = '请求过于频繁，请稍后再试'
        break
      case 500:
        message = '服务器内部错误，请稍后重试'
        break
      case 502:
        message = '服务暂时不可用，请稍后重试'
        break
      case 503:
        message = '系统维护中，请稍后重试'
        break
      default:
        message = data?.message || `请求失败 (${status})`
    }
    
    // 记录错误日志
    this.logError({
      type: 'API_ERROR',
      status,
      message: data?.message || message,
      url: response.config?.url,
      method: response.config?.method,
      context,
      timestamp: new Date().toISOString()
    })
    
    return {
      success: false,
      message,
      code: status,
      data: data
    }
  }
  
  static handleNetworkError(error, context) {
    const message = navigator.onLine 
      ? '网络连接异常，请检查网络设置' 
      : '网络连接已断开，请检查网络连接'
    
    this.logError({
      type: 'NETWORK_ERROR',
      message: error.message,
      context,
      timestamp: new Date().toISOString()
    })
    
    return {
      success: false,
      message,
      code: 'NETWORK_ERROR'
    }
  }
  
  static handleGenericError(error, context) {
    const message = error.message || '操作失败，请重试'
    
    this.logError({
      type: 'GENERIC_ERROR',
      message: error.message,
      stack: error.stack,
      context,
      timestamp: new Date().toISOString()
    })
    
    return {
      success: false,
      message,
      code: 'GENERIC_ERROR'
    }
  }
  
  static handleAuthError() {
    // 清除认证信息
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('isLoggedIn')
    
    // 如果不在登录页面，跳转到登录页
    if (!window.location.pathname.includes('/login')) {
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    }
  }
  
  static logError(errorInfo) {
    // 在开发环境中输出详细错误信息
    if (import.meta.env.DEV) {
      console.group('🚨 Error Details')
      console.error('Type:', errorInfo.type)
      console.error('Message:', errorInfo.message)
      console.error('Context:', errorInfo.context)
      console.error('Timestamp:', errorInfo.timestamp)
      if (errorInfo.stack) console.error('Stack:', errorInfo.stack)
      console.groupEnd()
    }
    
    // 在生产环境中可以将错误发送到错误监控服务
    if (import.meta.env.PROD) {
      // 这里可以集成 Sentry 或其他错误监控服务
      // Sentry.captureException(errorInfo)
    }
    
    // 存储错误日志到本地（可选）
    this.storeErrorLog(errorInfo)
  }
  
  static storeErrorLog(errorInfo) {
    try {
      const logs = JSON.parse(localStorage.getItem('errorLogs') || '[]')
      logs.push(errorInfo)
      
      // 只保留最近50条错误日志
      if (logs.length > 50) {
        logs.splice(0, logs.length - 50)
      }
      
      localStorage.setItem('errorLogs', JSON.stringify(logs))
    } catch (e) {
      console.warn('Failed to store error log:', e)
    }
  }
  
  static getErrorLogs() {
    try {
      return JSON.parse(localStorage.getItem('errorLogs') || '[]')
    } catch (e) {
      return []
    }
  }
  
  static clearErrorLogs() {
    localStorage.removeItem('errorLogs')
  }
}

// 响应数据标准化处理器
class ResponseHandler {
  static normalize(response) {
    // 标准化响应数据格式
    if (typeof response === 'boolean') {
      return {
        success: response,
        data: null,
        message: response ? '操作成功' : '操作失败'
      }
    }
    
    if (response && typeof response === 'object') {
      // 如果已经是标准格式，直接返回
      if (response.hasOwnProperty('success')) {
        return response
      }
      
      // 否则包装为标准格式
      return {
        success: true,
        data: response,
        message: '操作成功'
      }
    }
    
    // 其他类型数据
    return {
      success: true,
      data: response,
      message: '操作成功'
    }
  }
  
  static isSuccess(response) {
    const normalized = this.normalize(response)
    return normalized.success === true
  }
  
  static getData(response) {
    const normalized = this.normalize(response)
    return normalized.data
  }
  
  static getMessage(response) {
    const normalized = this.normalize(response)
    return normalized.message
  }
}

// API调用包装器
export const apiWrapper = {
  async request(requestFn, context = '') {
    try {
      const response = await requestFn()
      return ResponseHandler.normalize(response)
    } catch (error) {
      return ErrorHandler.handle(error, context)
    }
  },
  
  async get(url, params = {}, context = '') {
    return this.request(() => import('./request.js').then(m => m.get(url, params)), context)
  },
  
  async post(url, data = {}, context = '') {
    return this.request(() => import('./request.js').then(m => m.post(url, data)), context)
  },
  
  async put(url, data = {}, context = '') {
    return this.request(() => import('./request.js').then(m => m.put(url, data)), context)
  },
  
  async delete(url, params = {}, context = '') {
    return this.request(() => import('./request.js').then(m => m.del(url, params)), context)
  }
}

export { ErrorHandler, ResponseHandler }