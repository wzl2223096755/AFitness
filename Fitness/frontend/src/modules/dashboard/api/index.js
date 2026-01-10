/**
 * Dashboard模块API导出
 * 重新导出共享的fitness API
 */

export * from '@shared/api/fitness.js'

// 导出fitnessApi作为默认导出
import * as fitnessApiModule from '@shared/api/fitness.js'
export const fitnessApi = fitnessApiModule.fitnessApi || fitnessApiModule
