<template>
  <slot v-if="isEnabled" />
  <slot v-else-if="$slots.disabled" name="disabled" />
  <slot v-else-if="$slots.loading && isLoading" name="loading" />
</template>

<script setup>
/**
 * 模块守卫组件
 * 根据模块启用状态条件渲染内容
 * 
 * @see Requirements 6.2 - 支持通过配置文件启用/禁用模块
 * 
 * 使用示例:
 * <ModuleGuard module="training">
 *   <TrainingComponent />
 *   <template #disabled>
 *     <p>训练模块已禁用</p>
 *   </template>
 * </ModuleGuard>
 */

import { computed } from 'vue'
import { useModuleConfig } from '../composables/useModuleConfig.js'

const props = defineProps({
  /**
   * 模块名称
   */
  module: {
    type: String,
    required: true
  },
  /**
   * 是否在加载时显示内容（默认显示）
   */
  showWhileLoading: {
    type: Boolean,
    default: true
  }
})

const { isModuleEnabled, isLoading, isLoaded } = useModuleConfig()

const isEnabled = computed(() => {
  // 如果配置还在加载中，根据 showWhileLoading 决定是否显示
  if (!isLoaded.value) {
    return props.showWhileLoading
  }
  return isModuleEnabled(props.module)
})
</script>
