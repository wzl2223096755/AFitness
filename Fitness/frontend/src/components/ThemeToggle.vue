<template>
  <button 
    class="theme-toggle" 
    @click="toggleTheme"
    :title="themeLabel"
    :aria-label="themeLabel"
  >
    <span class="theme-icon">
      <van-icon v-if="currentTheme === 'light'" name="sunny-o" />
      <van-icon v-else-if="currentTheme === 'dark'" name="star-o" />
      <van-icon v-else name="setting-o" />
    </span>
    <span class="theme-text">{{ themeLabel }}</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useTheme } from '../../shared/composables/useTheme'

const { theme, currentTheme, toggleTheme } = useTheme()

const themeLabel = computed(() => {
  const labels = {
    light: '亮色',
    dark: '暗色',
    auto: '自动'
  }
  return labels[theme.value] || '主题'
})
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--btn-secondary-bg);
  border: 1px solid var(--border-brand);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-toggle:hover {
  background: var(--hover-bg);
  border-color: var(--brand-primary);
}

.theme-toggle:active {
  transform: scale(0.98);
}

.theme-icon {
  font-size: 18px;
  color: var(--brand-primary);
}

.theme-text {
  font-weight: 500;
}
</style>
