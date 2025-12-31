<template>
  <div class="dashboard-container">
    <DynamicDashboard />
  </div>
</template>

<script setup>
import DynamicDashboard from '../components/DynamicDashboard.vue'
</script>

<style scoped>
/* ===================================
   全局布局和容器样式
   =================================== */
.dashboard-page {
  width: 100%;
  max-width: 100vw;
  box-sizing: border-box;
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
  overflow-x: visible;
}

/* ===================================
   欢迎区域样式
   =================================== */
.welcome-section {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  margin: 20px 20px 30px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(103,126,234,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.welcome-content {
  flex: 1;
  z-index: 2;
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #2c3e50;
  margin: 0 0 12px 0;
  line-height: 1.2;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 1.1rem;
  color: #64748b;
  margin: 0 0 24px 0;
  font-weight: 400;
}

.welcome-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.welcome-actions .el-button {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.welcome-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}

.welcome-visual {
  flex: 0 0 auto;
  z-index: 2;
}

.daily-motivation {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 24px;
  border-radius: 16px;
  text-align: center;
  border: 1px solid rgba(148,163,184,0.2);
  min-width: 200px;
}

.motivation-quote {
  font-size: 0.95rem;
  color: #475569;
  font-style: italic;
  margin-bottom: 12px;
  line-height: 1.4;
}

.motivation-date {
  font-size: 0.85rem;
  color: #94a3b8;
  font-weight: 500;
}

/* ===================================
   区域标题样式
   =================================== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 0 4px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.section-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.time-range-selector {
  display: flex;
  align-items: center;
}

/* ===================================
   核心指标概览样式
   =================================== */
.metrics-overview {
  margin: 0 20px 30px 20px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  padding: 0 4px;
  overflow: visible;
}

.metric-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 28px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255,255,255,0.2);
  position: relative;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--metric-color) 0%, var(--metric-color-light) 100%);
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.12);
}

.primary-metric {
  --metric-color: #3b82f6;
  --metric-color-light: #60a5fa;
}

.success-metric {
  --metric-color: #10b981;
  --metric-color-light: #34d399;
}

.warning-metric {
  --metric-color: #f59e0b;
  --metric-color-light: #fbbf24;
}

.danger-metric {
  --metric-color: #ef4444;
  --metric-color-light: #f87171;
}

.metric-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.metric-info {
  flex: 1;
}

.metric-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.metric-subtitle {
  font-size: 0.85rem;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
  flex-shrink: 0;
}

.metric-icon.primary {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
}

.metric-icon.success {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.metric-icon.warning {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.metric-icon.danger {
  background: linear-gradient(135deg, #ef4444, #f87171);
}

.metric-value {
  margin-bottom: 16px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.value-number {
  font-size: 2.5rem;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
}

.value-unit {
  font-size: 1rem;
  color: #64748b;
  font-weight: 500;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 8px;
  width: fit-content;
}

.metric-trend.positive {
  color: #10b981;
  background: rgba(16,185,129,0.1);
}

.metric-trend.negative {
  color: #ef4444;
  background: rgba(239,68,68,0.1);
}

.metric-trend.neutral {
  color: #f59e0b;
  background: rgba(245,158,11,0.1);
}

/* ===================================
   数据分析区域样式
   =================================== */
.analytics-section {
  margin: 0 20px 30px 20px;
}

.analytics-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: auto auto;
  gap: 20px;
  padding: 0 4px;
  overflow: visible;
}

.chart-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255,255,255,0.2);
  transition: all 0.3s ease;
  overflow: visible;
  width: 100%;
  box-sizing: border-box;
}

.chart-card:hover {
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.main-chart {
  grid-column: 1;
  grid-row: 1 / span 2;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.chart-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chart-content {
  width: 100%;
  height: 300px;
  overflow: visible;
}

.large-chart {
  height: 600px;
}

/* ===================================
   最近活动区域样式
   =================================== */
.recent-activity {
  margin: 0 20px 30px 20px;
}

.activity-content {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255,255,255,0.2);
  overflow: visible;
}

.empty-activity {
  text-align: center;
  padding: 60px 20px;
}

.empty-visual {
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 4rem;
  color: #cbd5e1;
  opacity: 0.6;
}

.empty-content h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #475569;
  margin: 0 0 8px 0;
}

.empty-content p {
  font-size: 1rem;
  color: #94a3b8;
  margin: 0 0 24px 0;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.training-records {
  overflow: visible;
}

/* ===================================
   响应式设计
   =================================== */
@media (max-width: 1200px) {
  .analytics-grid {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto auto auto;
  }
  
  .main-chart {
    grid-column: 1 / span 2;
    grid-row: 1;
  }
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: 0;
    margin: 0;
  }
  
  .welcome-section,
  .metrics-overview,
  .analytics-section,
  .recent-activity {
    margin-left: 12px;
    margin-right: 12px;
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .welcome-content {
    margin-bottom: 24px;
  }
  
  .welcome-visual {
    width: 100%;
  }
  
  .daily-motivation {
    max-width: 300px;
    margin: 0 auto;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 2px;
  }
  
  .metric-card {
    padding: 20px;
  }
  
  .value-number {
    font-size: 2rem;
  }
  
  .analytics-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 2px;
  }
  
  .main-chart {
    grid-column: 1;
    grid-row: 1;
  }
  
  .chart-card {
    padding: 20px;
  }
  
  .chart-content {
    height: 250px;
  }
  
  .large-chart {
    height: 400px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .welcome-section,
  .metrics-overview,
  .analytics-section,
  .recent-activity {
    margin-left: 8px;
    margin-right: 8px;
  }
  
  .welcome-section {
    padding: 20px 16px;
  }
  
  .welcome-title {
    font-size: 1.6rem;
  }
  
  .welcome-subtitle {
    font-size: 1rem;
  }
  
  .welcome-actions {
    justify-content: center;
  }
  
  .welcome-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
  
  .daily-motivation {
    padding: 16px;
    min-width: auto;
  }
  
  .metric-card {
    padding: 16px;
  }
  
  .metric-header {
    margin-bottom: 16px;
  }
  
  .value-number {
    font-size: 1.8rem;
  }
  
  .chart-card {
    padding: 16px;
  }
  
  .chart-content {
    height: 200px;
  }
  
  .large-chart {
    height: 300px;
  }
  
  .activity-content {
    padding: 16px;
  }
  
  .empty-activity {
    padding: 40px 16px;
  }
  
  .empty-icon {
    font-size: 3rem;
  }
  
  .section-title {
    font-size: 1.3rem;
  }
}

/* ===================================
   加载和动画状态
   =================================== */
.loading-placeholder {
  display: inline-block;
  width: 60px;
  height: 1.2em;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading-shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes loading-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* ===================================
   深色主题适配
   =================================== */
@media (prefers-color-scheme: dark) {
  .dashboard-page {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  }
  
  .welcome-section,
  .metric-card,
  .chart-card,
  .activity-content {
    background: rgba(30,41,59,0.95);
    border-color: rgba(71,85,105,0.3);
  }
  
  .welcome-title {
    background: linear-gradient(135deg, #60a5fa, #a78bfa);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .welcome-subtitle {
    color: #94a3b8;
  }
  
  .metric-title,
  .chart-header h3,
  .section-title {
    color: #f1f5f9;
  }
  
  .metric-subtitle {
    color: #94a3b8;
  }
  
  .value-number {
    color: #f1f5f9;
  }
  
  .value-unit {
    color: #94a3b8;
  }
  
  .daily-motivation {
    background: linear-gradient(135deg, #334155 0%, #475569 100%);
    border-color: rgba(71,85,105,0.3);
  }
  
  .motivation-quote {
    color: #cbd5e1;
  }
  
  .motivation-date {
    color: #94a3b8;
  }
  
  .empty-content h3 {
    color: #cbd5e1;
  }
  
  .empty-content p {
    color: #94a3b8;
  }
  
  .empty-icon {
    color: #475569;
  }
}

/* ===================================
   表格样式优化
   =================================== */
:deep(.el-table) {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background: rgba(248,250,252,0.8);
  backdrop-filter: blur(10px);
}

:deep(.el-table th) {
  background: transparent;
  color: #475569;
  font-weight: 600;
  border-bottom: 1px solid rgba(226,232,240,0.6);
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(226,232,240,0.3);
  padding: 16px 12px;
}

:deep(.el-table tr:hover > td) {
  background: rgba(241,245,249,0.5);
}

:deep(.training-record-row) {
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.training-record-row:hover) {
  background: rgba(59,130,246,0.05);
}

@media (prefers-color-scheme: dark) {
  :deep(.el-table__header-wrapper) {
    background: rgba(30,41,59,0.8);
  }
  
  :deep(.el-table th) {
    color: #cbd5e1;
    border-bottom-color: rgba(71,85,105,0.6);
  }
  
  :deep(.el-table td) {
    border-bottom-color: rgba(71,85,105,0.3);
    color: #e2e8f0;
  }
  
  :deep(.el-table tr:hover > td) {
    background: rgba(59,130,246,0.1);
  }
}

/* 对话框样式 */
.recovery-details,
.goal-details {
  text-align: center;
  padding: 20px;
}

.recovery-score {
  margin-bottom: 24px;
}

.score-value {
  font-size: 48px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.score-label {
  font-size: 18px;
  color: #64748b;
  font-weight: 500;
}

.recovery-recommendations {
  text-align: left;
}

.recovery-recommendations h4 {
  color: #1e293b;
  margin-bottom: 16px;
}

.recovery-recommendations ul {
  list-style: none;
  padding: 0;
}

.recovery-recommendations li {
  padding: 8px 0;
  color: #64748b;
  position: relative;
  padding-left: 24px;
}

.recovery-recommendations li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #10b981;
  font-weight: bold;
}

.goal-progress {
  margin-bottom: 24px;
}

.goal-info h4 {
  color: #1e293b;
  margin-bottom: 12px;
}

.goal-info p {
  color: #64748b;
  margin: 0;
}

@media (prefers-color-scheme: dark) {
  .score-value {
    color: #f1f5f9;
  }
  
  .score-label {
    color: #94a3b8;
  }
  
  .recovery-recommendations h4,
  .goal-info h4 {
    color: #f1f5f9;
  }
  
  .recovery-recommendations li,
  .goal-info p {
    color: #94a3b8;
  }
}
</style>