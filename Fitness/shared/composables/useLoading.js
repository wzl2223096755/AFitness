/**
 * 加载状态管理 Composable
 * Loading state management composable for shared use
 */
import { ref, computed } from 'vue'

/**
 * 创建加载状态管理
 * @param {boolean} initialState 初始加载状态
 */
export function useLoading(initialState = false) {
  const loading = ref(initialState)
  const loadingCount = ref(0)
  const loadingMessage = ref('')
  
  /**
   * 开始加载
   * @param {string} message 可选的加载消息
   */
  const startLoading = (message = '') => {
    loadingCount.value++
    loading.value = true
    if (message) {
      loadingMessage.value = message
    }
  }
  
  /**
   * 结束加载
   */
  const stopLoading = () => {
    loadingCount.value = Math.max(0, loadingCount.value - 1)
    if (loadingCount.value === 0) {
      loading.value = false
      loadingMessage.value = ''
    }
  }
  
  /**
   * 强制结束所有加载
   */
  const forceStopLoading = () => {
    loadingCount.value = 0
    loading.value = false
    loadingMessage.value = ''
  }
  
  /**
   * 设置加载消息
   * @param {string} message 加载消息
   */
  const setLoadingMessage = (message) => {
    loadingMessage.value = message
  }
  
  /**
   * 包装异步函数，自动管理加载状态
   * @param {Function} asyncFn 异步函数
   * @param {string} message 可选的加载消息
   */
  const withLoading = async (asyncFn, message = '') => {
    startLoading(message)
    try {
      return await asyncFn()
    } finally {
      stopLoading()
    }
  }
  
  const isLoading = computed(() => loading.value)
  
  return {
    loading,
    loadingCount,
    loadingMessage,
    isLoading,
    startLoading,
    stopLoading,
    forceStopLoading,
    setLoadingMessage,
    withLoading
  }
}

export default useLoading
