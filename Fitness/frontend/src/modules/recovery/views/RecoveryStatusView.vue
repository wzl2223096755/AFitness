<template>
  <div class="recovery-status-view">
    <div class="page-header">
      <h1>恢复状态</h1>
      <p class="subtitle">追踪您的身体恢复情况，获取个性化训练建议</p>
    </div>

    <div class="recovery-content">
      <!-- 当前恢复状态卡片 -->
      <RecoveryCard 
        :status="currentStatus" 
        :loading="loading"
        @refresh="fetchRecoveryStatus"
      />

      <!-- 疲劳指数 -->
      <FatigueIndicator 
        :fatigue-level="fatigueLevel"
        :loading="loading"
      />

      <!-- 训练建议 -->
      <div class="suggestions-section" v-if="suggestions.length > 0">
        <h2>训练建议</h2>
        <ul class="suggestions-list">
          <li v-for="(suggestion, index) in suggestions" :key="index">
            {{ suggestion }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recoveryApi } from '../api'
import RecoveryCard from '../components/RecoveryCard.vue'
import FatigueIndicator from '../components/FatigueIndicator.vue'

const loading = ref(false)
const currentStatus = ref(null)
const fatigueLevel = ref(0)
const suggestions = ref([])

const fetchRecoveryStatus = async () => {
  loading.value = true
  try {
    const userId = localStorage.getItem('userId')
    const [statusRes, fatigueRes, suggestionsRes] = await Promise.all([
      recoveryApi.getCurrentRecoveryStatus(userId),
      recoveryApi.getFatigueIndex(userId),
      recoveryApi.getTrainingSuggestions(userId)
    ])
    currentStatus.value = statusRes.data
    fatigueLevel.value = fatigueRes.data?.fatigueIndex || 0
    suggestions.value = suggestionsRes.data?.suggestions || []
  } catch (error) {
    console.error('获取恢复状态失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecoveryStatus()
})
</script>
