<template>
  <div class="fatigue-indicator" :class="{ loading }">
    <h3>疲劳指数</h3>
    
    <div class="indicator-content">
      <div class="gauge">
        <div class="gauge-fill" :style="{ width: `${fatigueLevel}%` }" :class="fatigueClass"></div>
      </div>
      
      <div class="fatigue-info">
        <span class="fatigue-value">{{ fatigueLevel }}%</span>
        <span class="fatigue-label">{{ fatigueLabel }}</span>
      </div>
    </div>
    
    <p class="fatigue-advice">{{ fatigueAdvice }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  fatigueLevel: { type: Number, default: 0 },
  loading: { type: Boolean, default: false }
})

const fatigueClass = computed(() => {
  if (props.fatigueLevel >= 80) return 'high'
  if (props.fatigueLevel >= 50) return 'moderate'
  return 'low'
})

const fatigueLabel = computed(() => {
  if (props.fatigueLevel >= 80) return '高疲劳'
  if (props.fatigueLevel >= 50) return '中等疲劳'
  return '低疲劳'
})

const fatigueAdvice = computed(() => {
  if (props.fatigueLevel >= 80) return '建议今天休息或进行轻度恢复训练'
  if (props.fatigueLevel >= 50) return '可以进行中等强度训练，注意恢复'
  return '身体状态良好，可以进行高强度训练'
})
</script>
