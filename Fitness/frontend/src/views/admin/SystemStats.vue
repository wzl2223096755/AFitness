<template>
  <AdminLayout>
    <template #header><h2>系统统计</h2></template>
    <div class="stats-page">
      <div class="stats-cards">
        <div class="stat-card">
          <h3>用户统计</h3>
          <div class="stat-row"><span>总用户数</span><span class="value">11</span></div>
          <div class="stat-row"><span>本月新增</span><span class="value">3</span></div>
          <div class="stat-row"><span>活跃用户</span><span class="value">8</span></div>
        </div>
        <div class="stat-card">
          <h3>训练统计</h3>
          <div class="stat-row"><span>总训练记录</span><span class="value">150</span></div>
          <div class="stat-row"><span>本周训练</span><span class="value">45</span></div>
          <div class="stat-row"><span>平均时长</span><span class="value">42分钟</span></div>
        </div>
        <div class="stat-card">
          <h3>系统状态</h3>
          <div class="stat-row"><span>CPU使用率</span><span class="value">23%</span></div>
          <div class="stat-row"><span>内存使用</span><span class="value">512MB</span></div>
          <div class="stat-row"><span>运行时间</span><span class="value">7天</span></div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from './AdminLayout.vue'
</script>

<style scoped>
.stats-page { padding: 24px; }
.stats-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px; }
.stat-card {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(233, 69, 96, 0.2);
  border-radius: 16px;
  padding: 24px;
}
.stat-card h3 { color: #f8fafc; margin-bottom: 20px; font-size: 18px; }
.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid rgba(233, 69, 96, 0.1);
  color: #94a3b8;
}
.stat-row:last-child { border-bottom: none; }
.stat-row .value { color: #e94560; font-weight: 600; }
</style>
