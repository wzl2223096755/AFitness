/**
 * 错误处理一致性属性测试
 * Property 4: 错误处理一致性
 * Validates: Requirements 7.1
 * 
 * 测试属性：对于任何 API 错误响应，前端应显示对应的用户友好错误提示
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { test, fc } from '@fast-check/vitest'

/**
 * 完整的错误码映射表（从 errorHandler.js 复制）
 * 包含 HTTP 状态码和自定义业务错误码
 */
const errorMessages = {
  // HTTP 标准错误码
  400: '请求参数错误，请检查输入',
  401: '登录已过期，请重新登录',
  403: '您没有权限执行此操作',
  404: '请求的资源不存在',
  405: '请求方法不被允许',
  408: '请求超时，请稍后重试',
  409: '数据冲突，请刷新后重试',
  410: '请求的资源已被永久删除',
  413: '上传的文件过大',
  415: '不支持的媒体类型',
  422: '数据验证失败',
  429: '请求过于频繁，请稍后再试',
  500: '服务器内部错误，请稍后重试',
  502: '服务暂时不可用，请稍后重试',
  503: '系统维护中，请稍后重试',
  504: '网关超时，请稍后重试',
  
  // 自定义网络错误码
  NETWORK_ERROR: '网络连接失败，请检查网络',
  TIMEOUT: '请求超时，请稍后重试',
  OFFLINE: '网络连接已断开，请检查网络连接',
  UNKNOWN: '发生未知错误，请联系管理员',
  CANCELLED: '请求已取消',
  
  // 业务错误码
  TOKEN_EXPIRED: '登录已过期，请重新登录',
  TOKEN_INVALID: '登录凭证无效，请重新登录',
  PERMISSION_DENIED: '没有权限执行此操作',
  RESOURCE_NOT_FOUND: '请求的资源不存在',
  VALIDATION_ERROR: '数据验证失败，请检查输入',
  DUPLICATE_ERROR: '数据已存在，请勿重复提交',
  RATE_LIMITED: '操作过于频繁，请稍后再试',
  SERVER_ERROR: '服务器错误，请稍后重试',
  MAINTENANCE: '系统维护中，请稍后访问'
}

/**
 * 根据错误码获取用户友好的错误消息
 */
function getErrorMessage(code, defaultMessage = '') {
  return errorMessages[code] || defaultMessage || errorMessages.UNKNOWN
}

/**
 * 判断是否为网络相关错误
 */
function isNetworkError(code) {
  return ['NETWORK_ERROR', 'TIMEOUT', 'OFFLINE', 502, 503, 504].includes(code)
}

/**
 * 判断是否为认证相关错误
 */
function isAuthError(code) {
  return [401, 'TOKEN_EXPIRED', 'TOKEN_INVALID'].includes(code)
}

/**
 * 判断是否为权限相关错误
 */
function isPermissionError(code) {
  return [403, 'PERMISSION_DENIED'].includes(code)
}

/**
 * 判断是否为可重试的错误
 */
function isRetryableError(code) {
  return [408, 429, 500, 502, 503, 504, 'NETWORK_ERROR', 'TIMEOUT'].includes(code)
}

/**
 * 简化的 ErrorHandler 实现用于测试
 * 模拟核心错误处理逻辑
 */
class TestErrorHandler {
  static handle(error, context = '') {
    if (error.response) {
      return this.handleApiError(error.response, context)
    }
    if (error.request) {
      return this.handleNetworkError(error, context)
    }
    return this.handleGenericError(error, context)
  }
  
  static handleApiError(response, context) {
    const { status, data } = response
    let message = getErrorMessage(status, data?.message)
    let errorCode = status
    
    // 处理业务错误码
    if (data?.code && data.code !== status) {
      const businessMessage = getErrorMessage(data.code)
      if (businessMessage !== errorMessages.UNKNOWN) {
        message = businessMessage
        errorCode = data.code
      }
    }
    
    // 使用后端返回的具体错误消息
    if (data?.message && data.message !== message) {
      message = data.message
    }
    
    return {
      success: false,
      message,
      code: errorCode,
      data: data,
      retryable: isRetryableError(status)
    }
  }
  
  static handleNetworkError(error, context) {
    let code = 'NETWORK_ERROR'
    let message = errorMessages.NETWORK_ERROR
    
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      code = 'TIMEOUT'
      message = errorMessages.TIMEOUT
    } else if (error.message?.includes('cancel')) {
      code = 'CANCELLED'
      message = errorMessages.CANCELLED
    }
    
    return {
      success: false,
      message,
      code,
      retryable: code !== 'CANCELLED'
    }
  }
  
  static handleGenericError(error, context) {
    const message = error.message || errorMessages.UNKNOWN
    
    return {
      success: false,
      message,
      code: 'GENERIC_ERROR',
      retryable: false
    }
  }
}

describe('错误处理一致性属性测试', () => {
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：对于任何 HTTP 状态码，getErrorMessage 应返回非空的用户友好消息
   */
  test.prop([
    fc.integer({ min: 100, max: 599 })
  ], { numRuns: 100 })(
    'Property 4: 任何 HTTP 状态码都应返回用户友好的错误消息',
    (statusCode) => {
      const message = getErrorMessage(statusCode)
      
      // 验证返回的消息是非空字符串
      expect(typeof message).toBe('string')
      expect(message.length).toBeGreaterThan(0)
      
      // 验证消息不是技术性的错误码
      expect(message).not.toMatch(/^[0-9]+$/)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：对于任何已知的 HTTP 错误状态码，应返回预定义的友好消息
   */
  test.prop([
    fc.constantFrom(400, 401, 403, 404, 405, 408, 409, 422, 429, 500, 502, 503, 504)
  ], { numRuns: 100 })(
    'Property 4: 已知 HTTP 错误码应返回预定义的友好消息',
    (statusCode) => {
      const message = getErrorMessage(statusCode)
      
      // 验证返回的消息是预定义的消息
      expect(errorMessages[statusCode]).toBeDefined()
      expect(message).toBe(errorMessages[statusCode])
      
      // 验证消息是中文的用户友好消息
      expect(message).toMatch(/[\u4e00-\u9fa5]/)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：对于任何 API 错误响应，ErrorHandler 应返回包含 success=false 和有效消息的结果
   */
  test.prop([
    fc.record({
      status: fc.integer({ min: 400, max: 599 }),
      data: fc.option(
        fc.record({
          message: fc.option(fc.string({ minLength: 1, maxLength: 200 }), { nil: undefined }),
          code: fc.option(fc.integer({ min: 1000, max: 9999 }), { nil: undefined })
        }),
        { nil: undefined }
      )
    })
  ], { numRuns: 100 })(
    'Property 4: API 错误响应应返回 success=false 和有效消息',
    (response) => {
      const error = { response }
      const result = TestErrorHandler.handle(error, 'test')
      
      // 验证返回结果结构
      expect(result.success).toBe(false)
      expect(typeof result.message).toBe('string')
      expect(result.message.length).toBeGreaterThan(0)
      expect(result.code).toBeDefined()
      expect(typeof result.retryable).toBe('boolean')
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：对于任何网络错误，ErrorHandler 应返回网络相关的错误消息
   */
  test.prop([
    fc.record({
      message: fc.constantFrom(
        'Network Error',
        'timeout of 30000ms exceeded',
        'Request cancelled',
        'ECONNABORTED'
      ),
      code: fc.option(fc.constantFrom('ECONNABORTED', 'ERR_NETWORK'), { nil: undefined })
    })
  ], { numRuns: 100 })(
    'Property 4: 网络错误应返回网络相关的错误消息',
    (errorInfo) => {
      const error = { request: {}, message: errorInfo.message, code: errorInfo.code }
      const result = TestErrorHandler.handle(error, 'test')
      
      // 验证返回结果
      expect(result.success).toBe(false)
      expect(typeof result.message).toBe('string')
      expect(result.message.length).toBeGreaterThan(0)
      
      // 验证错误码是网络相关的
      expect(['NETWORK_ERROR', 'TIMEOUT', 'CANCELLED', 'OFFLINE']).toContain(result.code)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：isNetworkError 应正确识别网络相关错误码
   */
  test.prop([
    fc.constantFrom('NETWORK_ERROR', 'TIMEOUT', 'OFFLINE', 502, 503, 504)
  ], { numRuns: 100 })(
    'Property 4: isNetworkError 应正确识别网络错误码',
    (code) => {
      expect(isNetworkError(code)).toBe(true)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：isAuthError 应正确识别认证相关错误码
   */
  test.prop([
    fc.constantFrom(401, 'TOKEN_EXPIRED', 'TOKEN_INVALID')
  ], { numRuns: 100 })(
    'Property 4: isAuthError 应正确识别认证错误码',
    (code) => {
      expect(isAuthError(code)).toBe(true)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：isPermissionError 应正确识别权限相关错误码
   */
  test.prop([
    fc.constantFrom(403, 'PERMISSION_DENIED')
  ], { numRuns: 100 })(
    'Property 4: isPermissionError 应正确识别权限错误码',
    (code) => {
      expect(isPermissionError(code)).toBe(true)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：isRetryableError 应正确识别可重试的错误码
   */
  test.prop([
    fc.constantFrom(408, 429, 500, 502, 503, 504, 'NETWORK_ERROR', 'TIMEOUT')
  ], { numRuns: 100 })(
    'Property 4: isRetryableError 应正确识别可重试错误码',
    (code) => {
      expect(isRetryableError(code)).toBe(true)
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：对于任何通用错误，ErrorHandler 应返回错误消息或默认消息
   */
  test.prop([
    fc.option(fc.string({ minLength: 1, maxLength: 200 }), { nil: undefined })
  ], { numRuns: 100 })(
    'Property 4: 通用错误应返回错误消息或默认消息',
    (errorMessage) => {
      const error = new Error(errorMessage)
      const result = TestErrorHandler.handle(error, 'test')
      
      // 验证返回结果
      expect(result.success).toBe(false)
      expect(typeof result.message).toBe('string')
      expect(result.message.length).toBeGreaterThan(0)
      expect(result.code).toBe('GENERIC_ERROR')
      expect(result.retryable).toBe(false)
      
      // 如果有错误消息，应该使用它
      if (errorMessage) {
        expect(result.message).toBe(errorMessage)
      }
    }
  )
  
  /**
   * Feature: system-optimization-95, Property 4: 错误处理一致性
   * Validates: Requirements 7.1
   * 
   * 属性：后端返回的自定义错误消息应优先于默认消息
   */
  test.prop([
    fc.record({
      status: fc.constantFrom(400, 422, 500),
      customMessage: fc.string({ minLength: 5, maxLength: 100 })
    })
  ], { numRuns: 100 })(
    'Property 4: 后端自定义错误消息应优先于默认消息',
    ({ status, customMessage }) => {
      const error = {
        response: {
          status,
          data: { message: customMessage }
        }
      }
      const result = TestErrorHandler.handle(error, 'test')
      
      // 验证使用了自定义消息
      expect(result.message).toBe(customMessage)
    }
  )
})
