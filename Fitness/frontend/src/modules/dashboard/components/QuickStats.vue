<template>
  <section class="stats-section">
    <div class="stats-grid">
      <div 
        v-for="(stat, index) in stats" 
        :key="stat.id"
        class="card-unified card-unified--interactive"
        :class="[`card-unified--${stat.variant}`, `stagger-${index + 1}`]"
        @click="navigateTo(stat.route)"
      >
        <div class="stat-icon icon-lg">{{ stat.icon }}</div>
        <div class="stat-info">
          <div class="stat-value">{{ formatValue(stat.value, stat.unit) }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="getChangeClass(stat.change)">
            {{ formatChange(stat.change, stat.changeLabel) }}
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  weeklyTrainingCount: { type: Number, default: 0 },
  weeklyChange: { type: Number, default: 0 },
  totalVolume: { type: Number, default: 0 },
  recoveryScore: { type: Number, default: 0 },
  goalCompletionRate: { type: Number, default: 0 }
})

const router = useRouter()

const stats = computed(() => [
  {
    id: 'training',
    icon: '🏋️',
    value: props.weeklyTrainingCount,
    unit: '',
    label: '本周训练',
    change: props.weeklyChange,
    changeLabel: 'vs上周',
    variant: 'primary',
    route: '/training-data'
  },
  {
    id: 'volume',
    icon: '📊',
    value: props.totalVolume,
    unit: 'kg',
    label: '总训练量',
    change: 1,
    changeLabel: '持续增长',
    variant: 'success',
    route: '/load-analysis'
  },
  {
    id: 'recovery',
    icon: '💚',
    value: props.recoveryScore,
    unit: '',
    label: '恢复评分',
    change: props.recoveryScore >= 80 ? 1 : props.recoveryScore >= 60 ? 0 : -1,
    changeLabel: getRecoveryText(props.recoveryScore),
    variant: 'warning',
    route: '/recovery-status'
  },
  {
    id: 'goals',
    icon: '🎯',
    value: props.goalCompletionRate,
    unit: '%',
    label: '目标完成',
    change: props.goalCompletionRate >= 80 ? 1 : -1,
    changeLabel: props.goalCompletionRate >= 80 ? '表现优秀' : '继续努力',
    variant: 'danger',
    route: '/fitness-planner'
  }
])

function getRecoveryText(score) {
  if (score >= 80) return '恢复良好'
  if (score >= 60) return '需要休息'
  return '过度疲劳'
}

function formatValue(value, unit) {
  const formatted = new Intl.NumberFormat('zh-CN').format(value || 0)
  return unit ? `${formatted}${unit}` : formatted
}

function formatChange(change, label) {
  if (typeof change === 'number' && label.includes('vs')) {
    return `${change >= 0 ? '+' : ''}${change} ${label}`
  }
  return label
}

function getChangeClass(change) {
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
}

function navigateTo(route) {
  router.push(route)
}
</script>

<style scoped>
.stats-section {
  margin-bottom: var(--spacing-7, 28px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-5, 20px);
}

.stats-grid .card-unified {
  display: flex;
  align-items: center;
  gap: var(--spacing-5, 18px);
}

.stat-icon {
  filter: drop-shadow(0 0 10px rgba(99, 102, 241, 0.4));
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: var(--spacing-2, 6px) 0;
  font-weight: 500;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 600;
  padding: var(--spacing-1, 4px) var(--spacing-3, 10px);
  border-radius: 12px;
  display: inline-block;
}

.stat-change.positive {
  color: var(--color-success, #10b981);
  background: var(--color-success-bg, rgba(16, 185, 129, 0.15));
}

.stat-change.negative {
  color: var(--color-danger, #ef4444);
  background: var(--color-danger-bg, rgba(239, 68, 68, 0.15));
}

.stat-change.neutral {
  color: var(--color-warning, #f59e0b);
  background: var(--color-warning-bg, rgba(245, 158, 11, 0.15));
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
