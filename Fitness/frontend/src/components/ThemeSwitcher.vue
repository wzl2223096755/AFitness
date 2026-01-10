<template>
  <div class="theme-switcher">
    <!-- 主题切换按钮 -->
    <button class="theme-toggle-btn" @click="togglePanel" :title="'当前主题: ' + themeConfig.name">
      <span class="theme-icon">🎨</span>
    </button>

    <!-- 主题选择面板 -->
    <Transition name="panel-slide">
      <div v-if="showPanel" class="theme-panel">
        <div class="panel-header">
          <h3>主题设置</h3>
          <button class="close-btn" @click="showPanel = false">×</button>
        </div>

        <!-- 预设主题 -->
        <div class="theme-section">
          <h4>预设主题</h4>
          <div class="preset-themes">
            <button
              v-for="(theme, key) in allPresetThemes"
              :key="key"
              class="theme-preset-btn"
              :class="{ active: currentTheme === key && !useCustomTheme }"
              @click="selectPreset(key)"
              :style="{ '--preview-bg': theme.bgPrimary, '--preview-primary': theme.primary }"
            >
              <span class="preview-dot"></span>
              <span class="theme-name">{{ theme.name }}</span>
            </button>
          </div>
        </div>

        <!-- 自定义颜色 -->
        <div class="theme-section">
          <h4>自定义颜色</h4>
          <div class="color-pickers">
            <div class="color-item">
              <label>主色调</label>
              <el-color-picker v-model="localColors.primary" @change="updateCustomColor('primary', $event)" />
            </div>
            <div class="color-item">
              <label>强调色</label>
              <el-color-picker v-model="localColors.accent" @change="updateCustomColor('accent', $event)" />
            </div>
            <div class="color-item">
              <label>背景色</label>
              <el-color-picker v-model="localColors.bgPrimary" @change="updateCustomColor('bgPrimary', $event)" />
            </div>
            <div class="color-item">
              <label>次背景</label>
              <el-color-picker v-model="localColors.bgSecondary" @change="updateCustomColor('bgSecondary', $event)" />
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="panel-actions">
          <button class="reset-btn" @click="resetTheme">
            <span>🔄</span> 重置默认
          </button>
        </div>
      </div>
    </Transition>

    <!-- 遮罩层 -->
    <div v-if="showPanel" class="panel-overlay" @click="showPanel = false"></div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'

const themeStore = useThemeStore()
const { currentTheme, customColors, useCustomTheme, themeConfig, allPresetThemes } = storeToRefs(themeStore)

const showPanel = ref(false)

// 本地颜色状态（用于颜色选择器）
const localColors = ref({
  primary: '#8020ff',
  accent: '#00f2fe',
  bgPrimary: '#0a0a14',
  bgSecondary: '#121225'
})

// 同步本地颜色
watch(customColors, (newColors) => {
  localColors.value = {
    primary: newColors.primary,
    accent: newColors.accent,
    bgPrimary: newColors.bgPrimary,
    bgSecondary: newColors.bgSecondary
  }
}, { immediate: true, deep: true })

const togglePanel = () => {
  showPanel.value = !showPanel.value
}

const selectPreset = (themeName) => {
  themeStore.setTheme(themeName)
}

const updateCustomColor = (colorKey, colorValue) => {
  if (colorValue) {
    themeStore.setCustomColor(colorKey, colorValue)
  }
}

const resetTheme = () => {
  themeStore.resetToDefault()
  localColors.value = {
    primary: '#8020ff',
    accent: '#00f2fe',
    bgPrimary: '#0a0a14',
    bgSecondary: '#121225'
  }
}

onMounted(() => {
  // 同步初始颜色
  localColors.value = {
    primary: customColors.value.primary,
    accent: customColors.value.accent,
    bgPrimary: customColors.value.bgPrimary,
    bgSecondary: customColors.value.bgSecondary
  }
})
</script>

<style scoped>
.theme-switcher {
  position: relative;
  z-index: 1100;
}

.theme-toggle-btn {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-brand);
  color: var(--text-primary);
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.theme-toggle-btn:hover {
  transform: scale(1.05);
  border-color: var(--brand-primary);
  box-shadow: var(--shadow-brand);
}

.panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(4px);
  z-index: 1099;
}

.theme-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 380px;
  max-width: 90vw;
  max-height: 80vh;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: 20px;
  padding: 24px;
  z-index: 1100;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-default);
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--hover-bg);
  border: 1px solid var(--border-default);
  color: var(--text-tertiary);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.theme-section {
  margin-bottom: 24px;
}

.theme-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.preset-themes {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.theme-preset-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--hover-bg);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.theme-preset-btn:hover {
  background: var(--active-bg);
  border-color: var(--border-brand);
  transform: translateY(-2px);
}

.theme-preset-btn.active {
  background: var(--active-bg);
  border-color: var(--brand-primary);
  box-shadow: var(--shadow-brand);
}

.preview-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--preview-bg);
  border: 3px solid var(--preview-primary);
  box-shadow: 0 0 8px var(--preview-primary);
  flex-shrink: 0;
}

.theme-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.color-pickers {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.color-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-item label {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
}

.panel-actions {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-default);
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--hover-bg);
  border: 1px solid var(--border-default);
  border-radius: 10px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: var(--active-bg);
  border-color: var(--brand-primary);
  transform: translateY(-2px);
}

/* 面板动画 */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.9);
}

/* 响应式 */
@media (max-width: 480px) {
  .theme-panel {
    width: 95vw;
    padding: 20px;
  }

  .preset-themes {
    grid-template-columns: 1fr;
  }

  .color-pickers {
    grid-template-columns: 1fr;
  }
}
</style>
