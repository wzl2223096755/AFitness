/**
 * Dashboard模块API导出
 * 重新导出fitness API
 */

export * from '../../../api/fitness.js'

// 导出fitnessApi作为默认导出
import * as fitnessApiModule from '../../../api/fitness.js'
export const fitnessApi = fitnessApiModule.fitnessApi || fitnessApiModule
