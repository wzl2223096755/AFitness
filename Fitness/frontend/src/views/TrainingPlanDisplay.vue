<template>
  <div class="tech-theme">
    <!-- 科技感背景装饰 -->
    <div class="tech-bg">
      <div class="grid-overlay"></div>
      <div class="scan-line"></div>
      <div class="energy-blob blob-1"></div>
      <div class="energy-blob blob-2"></div>
    </div>

    <!-- 顶部导航栏 -->
    <van-nav-bar 
      title="我的训练计划" 
      fixed 
      placeholder 
      left-arrow 
      @click-left="$router.back()"
      class="tech-nav"
    >
      <template #right>
        <div class="nav-plus-btn" @click="goToPlanner">
          <van-icon name="plus" size="20" />
        </div>
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="tech-refresh">
      <template #pulling>
        <span class="refresh-text">下拉同步云端数据...</span>
      </template>
      <template #loosing>
        <span class="refresh-text">释放开始同步...</span>
      </template>
      <template #loading>
        <van-loading size="20px" color="#7000ff">同步中...</van-loading>
      </template>

      <div class="content">
        <!-- 计划状态切换 -->
        <div class="tabs-container glass-panel">
          <div class="panel-corner top-left"></div>
          <div class="panel-corner bottom-right"></div>
          <van-tabs 
            v-model:active="statusTab" 
            @change="loadPlans" 
            background="transparent"
            color="#7000ff"
            title-active-color="#7000ff"
            title-inactive-color="rgba(255,255,255,0.5)"
            line-width="30px"
            animated
            swipeable
            class="tech-tabs"
          >
            <van-tab title="进行中" name="ACTIVE" />
            <van-tab title="已完成" name="COMPLETED" />
            <van-tab title="全部" name="" />
          </van-tabs>
        </div>

        <!-- 计划卡片列表 -->
        <div v-if="plans.length > 0" class="plan-list">
          <div 
            v-for="(plan, index) in plans" 
            :key="plan.id" 
            class="plan-card-wrapper"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="viewDetails(plan)"
          >
            <div class="plan-card glass-panel">
              <div class="panel-corner top-left"></div>
              <div class="panel-corner bottom-right"></div>
              
              <div class="plan-card-header">
                <span class="plan-date">
                  <van-icon name="calendar-o" />
                  {{ formatDateRange(plan) }}
                </span>
                <div class="status-tag" :class="plan.status.toLowerCase()">
                  {{ getStatusText(plan.status) }}
                </div>
              </div>

              <div class="plan-card-body">
                <h3 class="plan-name">{{ plan.name }}</h3>
                <p class="plan-desc van-multi-ellipsis--l2">{{ plan.description || '核心任务：持续突破，科学进阶' }}</p>
              </div>

              <div class="plan-card-footer">
                <div class="plan-meta">
                  <div class="meta-item">
                    <van-icon name="apps-o" />
                    <span>{{ plan.exerciseCount || 0 }} 项科目</span>
                  </div>
                </div>
                <div class="action-hint">
                  <span>查看档案</span>
                  <van-icon name="arrow" />
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <van-empty 
          v-else 
          image="https://fastly.jsdelivr.net/npm/@vant/assets/custom-empty-image.png"
          description="尚未录入任何训练计划" 
          class="tech-empty"
        >
          <template #description>
            <p class="empty-desc">尚未录入任何训练计划</p>
          </template>
          <van-button round class="tech-btn pulse" @click="goToPlanner">
            <van-icon name="plus" /> 立即构建计划
          </van-button>
        </van-empty>
      </div>
    </van-pull-refresh>

    <!-- 计划详情弹出层 -->
    <van-popup 
      v-model:show="showDetails" 
      position="right" 
      class="tech-details-popup"
    >
      <div class="details-page">
        <van-nav-bar 
          :title="selectedPlan.name" 
          left-arrow 
          @click-left="showDetails = false" 
          fixed
          placeholder
          class="tech-nav"
        />
        
        <div class="details-content" v-if="selectedPlan.id">
          <!-- 核心属性 -->
          <div class="section-title">核心属性</div>
          <div class="glass-panel detail-group">
            <div class="detail-row">
              <span class="label">计划周期</span>
              <span class="value">{{ formatDateRange(selectedPlan) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">当前状态</span>
              <span class="value" :class="selectedPlan.status.toLowerCase()">
                {{ getStatusText(selectedPlan.status) }}
              </span>
            </div>
            <div class="detail-row vertical">
              <span class="label">任务简述</span>
              <p class="desc">{{ selectedPlan.description || '暂无详细任务描述' }}</p>
            </div>
          </div>

          <!-- 训练科目 -->
          <div class="section-title">训练科目 ({{ exercises.length }})</div>
          <div class="exercise-list">
            <div 
              v-for="ex in exercises" 
              :key="ex.id" 
              class="ex-item glass-panel"
              :class="{ 'is-completed': ex.completed }"
            >
              <div class="ex-info">
                <div class="ex-name">{{ ex.name }}</div>
                <div class="ex-params">
                  <span class="param">{{ ex.sets }} <small>SETS</small></span>
                  <span class="divider">/</span>
                  <span class="param">{{ ex.reps }} <small>REPS</small></span>
                  <span v-if="ex.intensity" class="ex-tag">RPE {{ ex.intensity }}</span>
                </div>
              </div>
              <div class="ex-action" @click="toggleExercise(ex)">
                <div class="tech-checkbox" :class="{ checked: ex.completed }">
                  <van-icon v-if="ex.completed" name="success" />
                </div>
              </div>
            </div>
            
            <div v-if="exercises.length === 0" class="no-exercises glass-panel">
              <van-icon name="warning-o" />
              <p>尚未在该计划中配置任何科目</p>
            </div>
          </div>

          <!-- 交互操作 -->
          <div class="details-actions">
            <button class="tech-btn primary" @click="editPlan">
              <van-icon name="edit" /> 编辑作战方案
            </button>
            <button class="tech-btn danger-outline" @click="confirmDelete">
              <van-icon name="delete-o" /> 销毁当前计划
            </button>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
/**
 * TrainingPlanDisplay.vue
 * 科技感训练计划展示页
 * 技术栈: Vue 3 + Vant UI + dayjs
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showNotify } from 'vant'
import { fitnessApi } from '../api/fitness'
import dayjs from 'dayjs'

// --- 路由与状态 ---
const router = useRouter()
const plans = ref([])
const statusTab = ref('ACTIVE')
const refreshing = ref(false)
const showDetails = ref(false)
const selectedPlan = ref({})
const exercises = ref([])

// --- 方法 ---

// 加载用户的训练计划列表
const loadPlans = async () => {
  try {
    const response = await fitnessApi.getUserFitnessPlansByStatus(statusTab.value)
    if (response.success) {
      plans.value = response.data
    }
  } catch (error) {
    showNotify({ type: 'danger', message: '核心链路连接异常', background: '#ff4d4f' })
  } finally {
    refreshing.value = false
  }
}

// 下拉刷新处理
const onRefresh = () => {
  loadPlans()
}

// 查看某个计划的详细信息
const viewDetails = async (plan) => {
  selectedPlan.value = plan
  try {
    const response = await fitnessApi.getFitnessPlanById(plan.id)
    if (response.success) {
      exercises.value = response.data.exercises || []
      showDetails.value = true
    }
  } catch (error) {
    showToast('档案解密失败')
  }
}

// 快速切换动作项目的完成状态
const toggleExercise = async (ex) => {
  const originalStatus = ex.completed
  ex.completed = !ex.completed // 先行 UI 变更提高响应感
  try {
    const res = await fitnessApi.toggleExerciseCompletion(ex.id)
    if (res.success) {
      showToast({
        message: ex.completed ? '科目已达成' : '科目已重置',
        position: 'bottom',
        className: 'tech-toast'
      })
    } else {
      throw new Error()
    }
  } catch (error) {
    ex.completed = originalStatus 
    showToast('同步指令执行失败')
  }
}

// 格式化状态文本
const getStatusText = (status) => {
  const map = { ACTIVE: '作战中', COMPLETED: '已收官', PAUSED: '暂缓执行' }
  return map[status] || status
}

// 格式化日期范围
const formatDateRange = (plan) => {
  const start = dayjs(plan.startDate).format('YY.MM.DD')
  const end = plan.endDate ? dayjs(plan.endDate).format('YY.MM.DD') : '至今'
  return `${start} - ${end}`
}

// 跳转
const goToPlanner = () => router.push('/fitness-planner')
const editPlan = () => router.push({ path: '/fitness-planner', query: { id: selectedPlan.value.id } })

// 删除计划
const confirmDelete = () => {
  showConfirmDialog({
    title: '销毁确认',
    message: '该计划及其附属作战数据将被永久抹除，是否继续？',
    confirmButtonColor: '#ff4d4f',
    cancelButtonColor: 'rgba(255,255,255,0.4)',
    className: 'tech-dialog'
  }).then(async () => {
    try {
      const response = await fitnessApi.deleteFitnessPlan(selectedPlan.value.id)
      if (response.success) {
        showNotify({ type: 'success', message: '目标计划已销毁', background: '#7000ff' })
        showDetails.value = false
        loadPlans()
      }
    } catch (error) {
      showToast('销毁指令执行失败')
    }
  })
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.tech-theme {
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
  position: relative;
  overflow: hidden;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 科技背景 */
.tech-bg {
  position: fixed;
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
    linear-gradient(var(--border-subtle) 1px, transparent 1px),
    linear-gradient(90deg, var(--border-subtle) 1px, transparent 1px);
  background-size: 30px 30px;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--brand-primary), transparent);
  animation: scan 4s linear infinite;
  opacity: 0.3;
}

@keyframes scan {
  0% { top: -2%; }
  100% { top: 102%; }
}

.energy-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.2;
}

.blob-1 {
  width: 300px;
  height: 300px;
  background: var(--brand-gradient);
  top: -100px;
  right: -50px;
  animation: float 10s ease-in-out infinite alternate;
}

.blob-2 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, var(--brand-secondary), #ec4899);
  bottom: 10%;
  left: -80px;
  animation: float 12s ease-in-out infinite alternate-reverse;
}

@keyframes float {
  from { transform: translate(0, 0); }
  to { transform: translate(30px, 40px); }
}

/* 导航栏 */
:deep(.tech-nav) {
  background: var(--bg-elevated) !important;
  backdrop-filter: blur(15px);
  border-bottom: 1px solid var(--border-default);
}

:deep(.tech-nav .van-nav-bar__title) {
  color: var(--brand-primary) !important;
  font-weight: 700;
  letter-spacing: 1px;
}

:deep(.tech-nav .van-icon) {
  color: var(--brand-primary) !important;
}

.nav-plus-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--btn-secondary-bg);
  border: 1px solid var(--border-brand);
  border-radius: 8px;
  color: var(--brand-primary);
  box-shadow: var(--shadow-sm);
}

/* 刷新器 */
.tech-refresh {
  min-height: calc(100vh - 46px);
}

.refresh-text {
  font-size: 12px;
  color: var(--brand-primary);
  opacity: 0.7;
}

/* 内容布局 */
.content {
  padding: 16px;
  position: relative;
  z-index: 1;
}

.glass-panel {
  background: var(--glass-bg);
  backdrop-filter: blur(15px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  position: relative;
  box-shadow: var(--shadow-md);
}

.panel-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid var(--brand-primary);
  opacity: 0.4;
}

.top-left { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.bottom-right { bottom: -1px; right: -1px; border-left: none; border-top: none; }

/* 状态切换 */
.tabs-container {
  margin-bottom: 20px;
  padding: 4px;
}

:deep(.tech-tabs .van-tabs__nav) {
  background: transparent;
}

/* 计划卡片 */
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-card-wrapper {
  opacity: 0;
  transform: translateY(20px);
  animation: slideUp 0.6s cubic-bezier(0.23, 1, 0.32, 1) forwards;
}

@keyframes slideUp {
  to { opacity: 1; transform: translateY(0); }
}

.plan-card {
  padding: 20px;
  transition: all 0.3s ease;
}

.plan-card:active {
  transform: scale(0.98);
  background: var(--active-bg);
}

.plan-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plan-date {
  font-size: 12px;
  color: var(--text-disabled);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-tag {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--hover-bg);
  color: var(--text-tertiary);
  border: 1px solid var(--border-default);
}

.status-tag.active { color: var(--brand-primary); border-color: var(--border-brand); background: var(--btn-secondary-bg); }
.status-tag.completed { color: var(--color-success); border-color: rgba(16, 185, 129, 0.3); background: rgba(16, 185, 129, 0.08); }

.plan-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.plan-desc {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.6;
}

.plan-card-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-subtle);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-disabled);
}

.action-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--brand-primary);
  opacity: 0.8;
}

/* 空状态 */
.tech-empty {
  padding-top: 80px;
}

.empty-desc {
  color: var(--text-disabled);
  margin-bottom: 24px;
}

.tech-btn {
  background: var(--brand-gradient);
  border: none;
  color: var(--text-inverse);
  font-weight: 700;
  padding: 0 24px;
  height: 44px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: var(--shadow-brand);
}

.pulse {
  animation: btnPulse 2s infinite;
}

@keyframes btnPulse {
  0% { transform: scale(1); box-shadow: var(--shadow-brand); }
  50% { transform: scale(1.05); box-shadow: 0 4px 24px rgba(124, 58, 237, 0.4); }
  100% { transform: scale(1); box-shadow: var(--shadow-brand); }
}

/* 详情弹窗 */
:deep(.tech-details-popup) {
  width: 100%;
  height: 100%;
  background: var(--bg-page);
}

.details-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.details-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  padding-bottom: 120px;
}

.section-title {
  font-size: 12px;
  color: var(--text-disabled);
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 20px 0 10px 4px;
}

.detail-group {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-row.vertical {
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.detail-row .label {
  font-size: 13px;
  color: var(--text-disabled);
}

.detail-row .value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
}

.detail-row .desc {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.6;
  margin: 0;
}

/* 训练科目列表 */
.exercise-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ex-item {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.ex-item.is-completed {
  background: rgba(16, 185, 129, 0.08);
  border-color: rgba(16, 185, 129, 0.2);
}

.ex-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.ex-params {
  display: flex;
  align-items: center;
  gap: 8px;
}

.param {
  font-family: 'DIN Alternate', sans-serif;
  color: var(--brand-primary);
  font-size: 16px;
  font-weight: 700;
}

.param small {
  font-size: 10px;
  opacity: 0.5;
  margin-left: 2px;
}

.divider {
  color: var(--border-default);
}

.ex-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(236, 72, 153, 0.1);
  color: #ec4899;
  border: 1px solid rgba(236, 72, 153, 0.2);
}

.tech-checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border-default);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.tech-checkbox.checked {
  background: var(--color-success);
  border-color: var(--color-success);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.no-exercises {
  padding: 40px;
  text-align: center;
  color: var(--text-disabled);
}

.no-exercises p {
  margin-top: 12px;
  font-size: 13px;
}

/* 详情操作 */
.details-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 20px 16px 30px;
  background: linear-gradient(to top, var(--bg-page) 80%, transparent);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tech-btn.primary {
  background: var(--brand-gradient);
  color: var(--text-inverse);
}

.tech-btn.danger-outline {
  background: transparent;
  border: 1px solid rgba(239, 68, 68, 0.4);
  color: var(--color-danger);
  box-shadow: none;
}

.tech-btn.danger-outline:active {
  background: rgba(239, 68, 68, 0.08);
}

/* Vant 全局覆盖 */
:deep(.van-dialog.tech-dialog) {
  background: var(--bg-elevated);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-default);
  color: var(--text-primary);
}

:deep(.tech-dialog .van-dialog__header),
:deep(.tech-dialog .van-dialog__message) {
  color: var(--text-primary);
}

:deep(.tech-toast) {
  background: var(--bg-elevated) !important;
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-brand);
  color: var(--brand-primary) !important;
}
</style>
