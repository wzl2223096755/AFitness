<template>
  <div class="volume-calculator tech-theme">
    <!-- 科技感背景装饰 -->
    <div class="tech-bg">
      <div class="grid-overlay"></div>
      <div class="scan-line"></div>
      <div class="energy-blob blob-1"></div>
      <div class="energy-blob blob-2"></div>
    </div>

    <!-- 顶部状态栏 -->
    <div class="tech-header">
      <div class="header-main">
        <van-icon name="balance-o" class="header-icon" />
        <span class="header-title">VOLUME ANALYSIS / 训练容量分析</span>
      </div>
      <div class="header-status">
        <span class="status-dot"></span>
        LIVE MONITOR
      </div>
    </div>

    <!-- 输入表单 -->
    <van-form class="tech-form" @submit.prevent>
      <div class="form-container glass-panel">
        <div class="panel-corner top-left"></div>
        <div class="panel-corner bottom-right"></div>
        
        <van-cell-group inset :border="false" class="transparent-group">
          <!-- 重量输入 -->
          <div class="field-wrapper">
            <div class="field-label">WEIGHT / 重量 (kg)</div>
            <van-field
              v-model.number="form.weight"
              name="weight"
              placeholder="0.0"
              type="digit"
              input-align="right"
              :border="false"
              class="tech-input"
            />
            <div class="input-glow"></div>
          </div>

          <!-- 次数输入 -->
          <div class="field-wrapper">
            <div class="field-label">REPS / 次数</div>
            <van-field
              v-model.number="form.reps"
              name="reps"
              placeholder="0"
              type="digit"
              input-align="right"
              :border="false"
              class="tech-input"
            />
            <div class="input-glow"></div>
          </div>

          <!-- 组数输入 -->
          <div class="field-wrapper">
            <div class="field-label">SETS / 组数</div>
            <van-field
              v-model.number="form.sets"
              name="sets"
              placeholder="0"
              type="digit"
              input-align="right"
              :border="false"
              class="tech-input"
            />
            <div class="input-glow"></div>
          </div>
        </van-cell-group>
      </div>
    </van-form>

    <!-- 计算结果展示 -->
    <transition name="tech-fade">
      <div v-if="totalVolume > 0" class="result-container">
        <!-- 核心数值展示 -->
        <div class="main-result glass-panel pulse-border">
          <div class="result-title">TOTAL VOLUME / 总训练容量</div>
          <div class="result-value-box">
            <span class="result-number">{{ totalVolume }}</span>
            <span class="result-unit">KG</span>
          </div>
          <div class="scanning-bar"></div>
          
          <div class="intensity-tag-container">
            <div class="intensity-tag" :style="{ '--tag-color': volumeLevel.color }">
              {{ volumeLevel.text }}
            </div>
          </div>
        </div>

        <!-- 渐进性负荷建议 -->
        <div class="progression-section glass-panel">
          <div class="section-header">
            <van-icon name="upgrade" class="section-icon" />
            <span class="text">PROGRESSION GOAL / 渐进性目标</span>
          </div>
          
          <div class="goal-grid">
            <div class="goal-item">
              <div class="goal-label">NEXT WEEK (+5%)</div>
              <div class="goal-value">{{ (totalVolume * 1.05).toFixed(0) }} <span class="unit">kg</span></div>
            </div>
            <div class="goal-item">
              <div class="goal-label">ELITE GOAL (+10%)</div>
              <div class="goal-value">{{ (totalVolume * 1.10).toFixed(0) }} <span class="unit">kg</span></div>
            </div>
          </div>
          
          <div class="progression-desc">
            建议每周将训练容量提升 2.5% - 5% 以确保持续进步。
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section">
          <button class="tech-btn primary" :disabled="saving" @click="saveRecord">
            <span class="btn-text">DATA LOGGING / 记录数据</span>
            <div class="btn-glow"></div>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
/**
 * TrainingVolumeCalculator.vue
 * 训练容量计算器组件 - 科技感升级版
 * 技术栈: Vue 3 + Vant UI
 */
import { ref, computed } from 'vue'
import { fitnessApi } from '../api/fitness'
import { showSuccessToast, showFailToast } from 'vant'

// --- 响应式数据 ---
const form = ref({
  weight: 60,
  reps: 10,
  sets: 3
})

const saving = ref(false)

// --- 方法 ---

// 正整数校验函数
const validatePositive = (val) => {
  if (val === undefined || val === null || val === '') return false
  const num = Number(val)
  return Number.isInteger(num) && num > 0
}

// --- 计算属性 (实时渲染) ---

// 实时计算总容量
const totalVolume = computed(() => {
  const { weight, reps, sets } = form.value
  if (validatePositive(weight) && validatePositive(reps) && validatePositive(sets)) {
    return weight * reps * sets
  }
  return 0
})

// 根据容量判断强度等级
const volumeLevel = computed(() => {
  const vol = totalVolume.value
  if (vol === 0) return { text: 'N/A', color: '#88a' }
  if (vol < 500) return { text: 'BASIC / 基础', color: '#00f2fe' }
  if (vol < 1500) return { text: 'MODERATE / 中等', color: '#00ff88' }
  if (vol < 3000) return { text: 'HIGH / 高强', color: '#7000ff' }
  return { text: 'ELITE / 专业', color: '#ff00ff' }
})

// 保存记录到后端
const saveRecord = async () => {
  const { weight, reps, sets } = form.value
  if (!validatePositive(weight) || !validatePositive(reps) || !validatePositive(sets)) {
    showFailToast('请输入合法的训练数据')
    return
  }

  saving.value = true
  try {
    const res = await fitnessApi.calculateVolumeWithRecord({ weight, sets, reps })
    if (res.success) {
      showSuccessToast('训练数据已上传云端')
    } else {
      showFailToast(res.message || '上传失败')
    }
  } catch (error) {
    console.error('保存记录失败:', error)
    showFailToast('核心链路连接超时')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.tech-theme {
  min-height: 100vh;
  background-color: transparent;
  color: #e0e6ed;
  padding: 20px 16px 100px;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 背景特效 - 离子化升级 */
.tech-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.grid-overlay {
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(112, 0, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(112, 0, 255, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #7000ff, transparent);
  animation: scan 4s linear infinite;
  opacity: 0.3;
}

.energy-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.1;
}

.blob-1 {
  width: 300px;
  height: 300px;
  background: #7000ff;
  top: -100px;
  right: -50px;
  animation: float 10s ease-in-out infinite alternate;
}

.blob-2 {
  width: 250px;
  height: 250px;
  background: #ff00ff;
  bottom: 100px;
  left: -50px;
  animation: float 12s ease-in-out infinite alternate-reverse;
}

/* 头部样式 */
.tech-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 24px;
  color: #7000ff;
  text-shadow: 0 0 10px rgba(112, 0, 255, 0.5);
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.header-status {
  font-size: 10px;
  color: #7000ff;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(112, 0, 255, 0.1);
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid rgba(112, 0, 255, 0.2);
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #7000ff;
  border-radius: 50%;
  box-shadow: 0 0 5px #7000ff;
  animation: pulse 1.5s infinite;
}

/* 通用面板 */
.glass-panel {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(112, 0, 255, 0.1);
  border-radius: 12px;
  position: relative;
}

.panel-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #7000ff;
  opacity: 0.6;
}

.top-left { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.bottom-right { bottom: -1px; right: -1px; border-left: none; border-top: none; }

/* 表单样式 */
.tech-form {
  position: relative;
  z-index: 1;
  margin-bottom: 24px;
}

.form-container {
  padding: 16px;
}

.transparent-group {
  background: transparent !important;
}

.field-wrapper {
  margin-bottom: 20px;
  position: relative;
}

.field-label {
  font-size: 12px;
  color: #88a;
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.tech-input {
  background: rgba(0, 0, 0, 0.2) !important;
  border: 1px solid rgba(112, 0, 255, 0.1) !important;
  border-radius: 8px;
  color: #fff !important;
  --van-field-input-text-color: #fff;
  --van-field-placeholder-text-color: #4a5568;
}

.input-glow {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #7000ff;
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(112, 0, 255, 0.5);
}

.field-wrapper:focus-within .input-glow {
  width: 100%;
}

/* 结果区域 */
.result-container {
  position: relative;
  z-index: 1;
}

.main-result {
  padding: 30px;
  text-align: center;
  margin-bottom: 24px;
  border: 1px solid rgba(112, 0, 255, 0.3);
  overflow: hidden;
}

.result-title {
  font-size: 14px;
  color: #88a;
  margin-bottom: 12px;
  letter-spacing: 2px;
}

.result-value-box {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 16px;
}

.result-number {
  font-size: 64px;
  font-weight: 800;
  color: #7000ff;
  text-shadow: 0 0 30px rgba(112, 0, 255, 0.4);
  font-family: 'DIN Alternate', sans-serif;
}

.result-unit {
  font-size: 20px;
  margin-left: 8px;
  color: #7000ff;
  opacity: 0.8;
}

.intensity-tag-container {
  display: flex;
  justify-content: center;
}

.intensity-tag {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--tag-color);
  color: var(--tag-color);
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.scanning-bar {
  position: absolute;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, transparent, rgba(112, 0, 255, 0.05), transparent);
  top: -100%;
  animation: scan-vertical 3s linear infinite;
}

/* 渐进性负荷建议 */
.progression-section {
  padding: 20px;
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-icon {
  color: #ffaa00;
  font-size: 20px;
}

.section-header .text {
  font-size: 13px;
  font-weight: 700;
  color: #88a;
  letter-spacing: 1px;
}

.goal-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.goal-item {
  background: rgba(0, 0, 0, 0.2);
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(112, 0, 255, 0.05);
}

.goal-label {
  font-size: 10px;
  color: #88a;
  margin-bottom: 6px;
}

.goal-value {
  font-size: 20px;
  font-weight: 800;
  color: #7000ff;
  font-family: 'DIN Alternate', sans-serif;
}

.goal-value .unit {
  font-size: 12px;
  font-weight: 400;
  opacity: 0.6;
}

.progression-desc {
  font-size: 12px;
  color: #88a;
  line-height: 1.6;
  padding-top: 12px;
  border-top: 1px solid rgba(112, 0, 255, 0.05);
}

/* 按钮样式 */
.action-section {
  padding: 0 20px;
}

.tech-btn {
  width: 100%;
  height: 54px;
  background: transparent;
  border: 1px solid #7000ff;
  border-radius: 8px;
  color: #7000ff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.tech-btn:active {
  background: rgba(112, 0, 255, 0.1);
  transform: scale(0.98);
}

.btn-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(112, 0, 255, 0.2), transparent);
  transition: left 0.5s;
}

.tech-btn:hover .btn-glow {
  left: 100%;
}

/* 动画 */
@keyframes scan {
  from { transform: translateY(0); }
  to { transform: translateY(100vh); }
}

@keyframes scan-vertical {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

@keyframes float {
  from { transform: translate(0, 0); }
  to { transform: translate(20px, 20px); }
}

@keyframes pulse {
  0% { opacity: 0.5; transform: scale(0.9); }
  100% { opacity: 1; transform: scale(1.1); }
}

.pulse-border {
  animation: pulse-border 2s infinite alternate;
}

@keyframes pulse-border {
  from { border-color: rgba(112, 0, 255, 0.2); }
  to { border-color: rgba(112, 0, 255, 0.6); box-shadow: 0 0 20px rgba(112, 0, 255, 0.1); }
}

.tech-fade-enter-active, .tech-fade-leave-active {
  transition: all 0.5s ease;
}

.tech-fade-enter-from, .tech-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
