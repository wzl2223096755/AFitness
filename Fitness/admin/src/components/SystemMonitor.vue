<template>
  <div class="system-monitor">
    <!-- 系统信息卡片 -->
    <div class="monitor-section">
      <h3 class="section-title">
        <span class="title-icon">💻</span>
        系统信息
      </h3>
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">🖥️</span>
            <span class="metric-label">操作系统</span>
          </div>
          <div class="metric-value">{{ systemInfo.osName }}</div>
          <div class="metric-sub">{{ systemInfo.osVersion }} ({{ systemInfo.osArch }})</div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">☕</span>
            <span class="metric-label">Java 版本</span>
          </div>
          <div class="metric-value">{{ systemInfo.javaVersion }}</div>
          <div class="metric-sub">{{ systemInfo.javaVendor }}</div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">⏱️</span>
            <span class="metric-label">运行时间</span>
          </div>
          <div class="metric-value">{{ formatUptime(systemInfo.uptime) }}</div>
          <div class="metric-sub">自启动以来</div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">🔢</span>
            <span class="metric-label">CPU 核心</span>
          </div>
          <div class="metric-value">{{ systemInfo.availableProcessors }}</div>
          <div class="metric-sub">可用处理器</div>
        </div>
      </div>
    </div>

    <!-- CPU 和内存使用率 -->
    <div class="monitor-section">
      <h3 class="section-title">
        <span class="title-icon">📊</span>
        资源使用率
      </h3>
      <div class="usage-grid">
        <!-- CPU 使用率 -->
        <div class="usage-card">
          <div class="usage-header">
            <span class="usage-label">CPU 使用率</span>
            <span class="usage-value" :class="getCpuClass(systemInfo.cpuUsage)">
              {{ systemInfo.cpuUsage?.toFixed(1) || 0 }}%
            </span>
          </div>
          <div class="progress-bar">
            <div 
              class="progress-fill cpu" 
              :style="{ width: `${systemInfo.cpuUsage || 0}%` }"
              :class="getCpuClass(systemInfo.cpuUsage)"
            ></div>
          </div>
        </div>
        
        <!-- 堆内存使用率 -->
        <div class="usage-card">
          <div class="usage-header">
            <span class="usage-label">堆内存使用</span>
            <span class="usage-value" :class="getMemoryClass(heapUsagePercent)">
              {{ heapUsagePercent.toFixed(1) }}%
            </span>
          </div>
          <div class="progress-bar">
            <div 
              class="progress-fill memory" 
              :style="{ width: `${heapUsagePercent}%` }"
              :class="getMemoryClass(heapUsagePercent)"
            ></div>
          </div>
          <div class="usage-detail">
            {{ formatBytes(jvmMetrics.heapUsed) }} / {{ formatBytes(jvmMetrics.heapMax) }}
          </div>
        </div>
        
        <!-- 非堆内存 -->
        <div class="usage-card">
          <div class="usage-header">
            <span class="usage-label">非堆内存</span>
            <span class="usage-value">{{ formatBytes(jvmMetrics.nonHeapUsed) }}</span>
          </div>
          <div class="usage-detail">已提交: {{ formatBytes(jvmMetrics.nonHeapCommitted) }}</div>
        </div>
      </div>
    </div>

    <!-- JVM 指标 -->
    <div class="monitor-section">
      <h3 class="section-title">
        <span class="title-icon">⚙️</span>
        JVM 指标
      </h3>
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">🧵</span>
            <span class="metric-label">线程数</span>
          </div>
          <div class="metric-value">{{ jvmMetrics.threadCount }}</div>
          <div class="metric-sub">峰值: {{ jvmMetrics.peakThreadCount }}</div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">🗑️</span>
            <span class="metric-label">GC 次数</span>
          </div>
          <div class="metric-value">{{ jvmMetrics.gcCount }}</div>
          <div class="metric-sub">总耗时: {{ jvmMetrics.gcTime }}ms</div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">📦</span>
            <span class="metric-label">已加载类</span>
          </div>
          <div class="metric-value">{{ formatNumber(jvmMetrics.loadedClassCount) }}</div>
          <div class="metric-sub">总计: {{ formatNumber(jvmMetrics.totalLoadedClassCount) }}</div>
        </div>
        
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-icon">👻</span>
            <span class="metric-label">守护线程</span>
          </div>
          <div class="metric-value">{{ jvmMetrics.daemonThreadCount }}</div>
          <div class="metric-sub">总启动: {{ formatNumber(jvmMetrics.totalStartedThreadCount) }}</div>
        </div>
      </div>
    </div>

    <!-- 数据库连接池 -->
    <div class="monitor-section">
      <h3 class="section-title">
        <span class="title-icon">🗄️</span>
        数据库连接池
      </h3>
      <div class="db-info">
        <div class="db-header">
          <span class="db-name">{{ databaseStats.databaseProductName }}</span>
          <span class="db-version">{{ databaseStats.databaseProductVersion }}</span>
        </div>
        <div class="db-driver">驱动: {{ databaseStats.driverName }} {{ databaseStats.driverVersion }}</div>
      </div>
      <div class="pool-stats">
        <div class="pool-stat">
          <div class="pool-label">活跃连接</div>
          <div class="pool-value active">{{ databaseStats.activeConnections }}</div>
        </div>
        <div class="pool-stat">
          <div class="pool-label">空闲连接</div>
          <div class="pool-value idle">{{ databaseStats.idleConnections }}</div>
        </div>
        <div class="pool-stat">
          <div class="pool-label">总连接数</div>
          <div class="pool-value total">{{ databaseStats.totalConnections }}</div>
        </div>
        <div class="pool-stat">
          <div class="pool-label">最大连接</div>
          <div class="pool-value max">{{ databaseStats.maxConnections }}</div>
        </div>
        <div class="pool-stat">
          <div class="pool-label">等待线程</div>
          <div class="pool-value pending" :class="{ warning: databaseStats.pendingConnections > 0 }">
            {{ databaseStats.pendingConnections }}
          </div>
        </div>
      </div>
      <!-- 连接池使用率 -->
      <div class="pool-usage">
        <div class="usage-header">
          <span class="usage-label">连接池使用率</span>
          <span class="usage-value" :class="getPoolClass(poolUsagePercent)">
            {{ poolUsagePercent.toFixed(1) }}%
          </span>
        </div>
        <div class="progress-bar">
          <div 
            class="progress-fill pool" 
            :style="{ width: `${poolUsagePercent}%` }"
            :class="getPoolClass(poolUsagePercent)"
          ></div>
        </div>
      </div>
    </div>

    <!-- 刷新按钮 -->
    <div class="monitor-actions">
      <button class="refresh-btn" @click="refreshData" :disabled="loading">
        <span class="btn-icon" :class="{ spinning: loading }">🔄</span>
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
      <span class="last-update" v-if="lastUpdate">
        最后更新: {{ formatTime(lastUpdate) }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { adminApi } from '@shared/api/admin'

// Props
const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: true
  },
  refreshInterval: {
    type: Number,
    default: 30000 // 30 seconds
  }
})

// Emits
const emit = defineEmits(['error', 'loaded'])

// State
const loading = ref(false)
const lastUpdate = ref(null)
let refreshTimer = null

const systemInfo = ref({
  osName: '-',
  osVersion: '-',
  osArch: '-',
  availableProcessors: 0,
  totalMemory: 0,
  freeMemory: 0,
  usedMemory: 0,
  maxMemory: 0,
  cpuUsage: 0,
  uptime: 0,
  javaVersion: '-',
  javaVendor: '-'
})

const jvmMetrics = ref({
  heapUsed: 0,
  heapMax: 0,
  heapInit: 0,
  heapCommitted: 0,
  nonHeapUsed: 0,
  nonHeapMax: 0,
  nonHeapCommitted: 0,
  threadCount: 0,
  peakThreadCount: 0,
  daemonThreadCount: 0,
  totalStartedThreadCount: 0,
  gcCount: 0,
  gcTime: 0,
  loadedClassCount: 0,
  totalLoadedClassCount: 0,
  unloadedClassCount: 0
})

const databaseStats = ref({
  activeConnections: 0,
  idleConnections: 0,
  totalConnections: 0,
  maxConnections: 10,
  pendingConnections: 0,
  databaseProductName: '-',
  databaseProductVersion: '-',
  driverName: '-',
  driverVersion: '-',
  poolName: '-',
  url: '-'
})

// Computed
const heapUsagePercent = computed(() => {
  if (!jvmMetrics.value.heapMax || jvmMetrics.value.heapMax <= 0) return 0
  return (jvmMetrics.value.heapUsed / jvmMetrics.value.heapMax) * 100
})

const poolUsagePercent = computed(() => {
  if (!databaseStats.value.maxConnections || databaseStats.value.maxConnections <= 0) return 0
  return (databaseStats.value.activeConnections / databaseStats.value.maxConnections) * 100
})

// Methods
const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatUptime = (ms) => {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) return `${days}天 ${hours % 24}小时`
  if (hours > 0) return `${hours}小时 ${minutes % 60}分钟`
  if (minutes > 0) return `${minutes}分钟 ${seconds % 60}秒`
  return `${seconds}秒`
}

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('zh-CN').format(num)
}

const formatTime = (date) => {
  if (!date) return ''
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const getCpuClass = (usage) => {
  if (usage >= 80) return 'critical'
  if (usage >= 60) return 'warning'
  return 'normal'
}

const getMemoryClass = (usage) => {
  if (usage >= 90) return 'critical'
  if (usage >= 70) return 'warning'
  return 'normal'
}

const getPoolClass = (usage) => {
  if (usage >= 80) return 'critical'
  if (usage >= 60) return 'warning'
  return 'normal'
}

const loadSystemInfo = async () => {
  try {
    const res = await adminApi.getSystemInfo()
    if (res.data) {
      systemInfo.value = { ...systemInfo.value, ...res.data }
    }
  } catch (error) {
    console.error('加载系统信息失败:', error)
    emit('error', { type: 'systemInfo', error })
  }
}

const loadJvmMetrics = async () => {
  try {
    const res = await adminApi.getJvmMetrics()
    if (res.data) {
      jvmMetrics.value = { ...jvmMetrics.value, ...res.data }
    }
  } catch (error) {
    console.error('加载JVM指标失败:', error)
    emit('error', { type: 'jvmMetrics', error })
  }
}

const loadDatabaseStats = async () => {
  try {
    const res = await adminApi.getDatabaseStats()
    if (res.data) {
      databaseStats.value = { ...databaseStats.value, ...res.data }
    }
  } catch (error) {
    console.error('加载数据库统计失败:', error)
    emit('error', { type: 'databaseStats', error })
  }
}

const refreshData = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    await Promise.all([
      loadSystemInfo(),
      loadJvmMetrics(),
      loadDatabaseStats()
    ])
    lastUpdate.value = new Date()
    emit('loaded')
  } finally {
    loading.value = false
  }
}

const startAutoRefresh = () => {
  if (props.autoRefresh && props.refreshInterval > 0) {
    refreshTimer = setInterval(refreshData, props.refreshInterval)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// Lifecycle
onMounted(() => {
  refreshData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

// Expose methods
defineExpose({
  refreshData
})
</script>

<style scoped>
.system-monitor {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.monitor-section {
  background: rgba(22, 33, 62, 0.5);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(233, 69, 96, 0.1);
}

.section-title {
  color: #f8fafc;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1rem;
}

/* Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.metric-card {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(233, 69, 96, 0.15);
  transition: all 0.3s ease;
}

.metric-card:hover {
  border-color: rgba(233, 69, 96, 0.3);
  transform: translateY(-2px);
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.metric-icon {
  font-size: 1.2rem;
}

.metric-label {
  color: #94a3b8;
  font-size: 0.85rem;
}

.metric-value {
  color: #f8fafc;
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.metric-sub {
  color: #64748b;
  font-size: 0.8rem;
}

/* Usage Grid */
.usage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.usage-card {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(233, 69, 96, 0.15);
}

.usage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.usage-label {
  color: #94a3b8;
  font-size: 0.9rem;
}

.usage-value {
  font-size: 1.2rem;
  font-weight: 700;
}

.usage-value.normal { color: #10b981; }
.usage-value.warning { color: #f39c12; }
.usage-value.critical { color: #ef4444; }

.usage-detail {
  color: #64748b;
  font-size: 0.8rem;
  margin-top: 8px;
}

/* Progress Bar */
.progress-bar {
  height: 8px;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-fill.normal { background: linear-gradient(90deg, #10b981, #34d399); }
.progress-fill.warning { background: linear-gradient(90deg, #f39c12, #fbbf24); }
.progress-fill.critical { background: linear-gradient(90deg, #ef4444, #f87171); }

/* Database Info */
.db-info {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid rgba(233, 69, 96, 0.15);
}

.db-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.db-name {
  color: #f8fafc;
  font-size: 1.1rem;
  font-weight: 600;
}

.db-version {
  color: #e94560;
  font-size: 0.85rem;
  background: rgba(233, 69, 96, 0.15);
  padding: 2px 8px;
  border-radius: 4px;
}

.db-driver {
  color: #64748b;
  font-size: 0.8rem;
}

/* Pool Stats */
.pool-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.pool-stat {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
  border: 1px solid rgba(233, 69, 96, 0.15);
}

.pool-label {
  color: #94a3b8;
  font-size: 0.75rem;
  margin-bottom: 4px;
}

.pool-value {
  font-size: 1.3rem;
  font-weight: 700;
}

.pool-value.active { color: #10b981; }
.pool-value.idle { color: #3b82f6; }
.pool-value.total { color: #f8fafc; }
.pool-value.max { color: #f39c12; }
.pool-value.pending { color: #94a3b8; }
.pool-value.pending.warning { color: #ef4444; }

.pool-usage {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(233, 69, 96, 0.15);
}

/* Actions */
.monitor-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(233, 69, 96, 0.1);
  border: 1px solid rgba(233, 69, 96, 0.3);
  border-radius: 10px;
  color: #e94560;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(233, 69, 96, 0.2);
  transform: translateY(-1px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 1rem;
  display: inline-block;
}

.btn-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.last-update {
  color: #64748b;
  font-size: 0.85rem;
}

/* Responsive */
@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .usage-grid {
    grid-template-columns: 1fr;
  }
  
  .pool-stats {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .pool-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .monitor-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
