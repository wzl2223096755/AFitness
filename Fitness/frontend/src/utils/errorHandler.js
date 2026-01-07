/**
 * 用户端错误处理器
 * 从共享模块重新导出，保持向后兼容
 */
export * from '@shared/utils/errorHandler.js'

// 显式导出常用函数，方便使用
export {
  ErrorHandler,
  ResponseHandler,
  apiWrapper,
  errorMessages,
  getErrorMessage,
  isNetworkError,
  isAuthError,
  isPermissionError,
  isRetryableError
} from '@shared/utils/errorHandler.js'
