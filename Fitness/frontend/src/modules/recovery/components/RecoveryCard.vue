<template>
  <div class="recovery-card" :class="{ loading }">
    <div class="card-header">
      <h3>当前恢复状态</h3>
      <button class="refresh-btn" @click="$emit('refresh')" :disabled="loading">
        刷新
      </button>
    </div>
    
    <div class="card-content" v-if="status">
      <div class="status-indicator" :class="statusClass">
        <span class="status-value">{{ status.recoveryScore || 0 }}%</span>
        <span class="status-label">{{ statusLabel }}</span>
      </div>
      
      <div class="status-details">
        <div class="detail-item">
          <span class="label">肌肉恢复</span>
          <span class="value">{{ status.muscleRecovery || 0 }}%</span>
        </div>
        <div class="detail-item">
          <span class="label">心率恢复</span>
          <span class="value">{{ status.heartRateRecovery || 0 }}%</span>
        </div>
        <div class="detail-item">
          <span class="label">睡眠质量</span>
          <span class="value">{{ status.sleepQuality || 0 }}%</span>
        </div>
      </div>
    </div>
    
    <div class="card-placeholder" v-else-if="!loading">
      <p>暂无恢复数据</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

defineEmits(['refresh'])

const statusClass = computed(() => {
  const score = props.status?.recoveryScore || 0
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'moderate'
  return 'low'
})

const statusLabel = computed(() => {
  const score = props.status?.recoveryScore || 0
  if (score >= 80) return '恢复良好'
  if (score >= 60) return '恢复中'
  if (score >= 40) return '需要休息'
  return '疲劳状态'
})
</script>
