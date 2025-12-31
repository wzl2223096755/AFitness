<template>
  <div class="recent-activity">
    <div class="section-header">
      <h2 class="section-title">
        <span class="title-icon">🕒</span>
        最近训练记录
      </h2>
      <div class="section-actions">
        <el-button type="primary" @click="goToTrainingData">
          <el-icon><Plus /></el-icon>
          添加记录
        </el-button>
        <el-button @click="navigateTo('training-data')">
          <el-icon><Document /></el-icon>
          查看全部
        </el-button>
      </div>
    </div>

    <div class="activity-content">
      <!-- 空数据状态 -->
      <div v-if="!loading && recentTrainingRecords.length === 0" class="empty-activity">
        <div class="empty-visual">
          <el-icon class="empty-icon"><Document /></el-icon>
        </div>
        <div class="empty-content">
          <h3 class="empty-title">还没有训练记录</h3>
          <p class="empty-description">开始记录您的第一次训练，开启健身之旅！</p>
          <el-button type="primary" size="large" @click="goToTrainingData">
            <el-icon><Plus /></el-icon>
            立即记录训练
          </el-button>
        </div>
      </div>

      <!-- 训练记录列表 -->
      <div v-else class="training-records">
        <el-table 
          :data="recentTrainingRecords" 
          style="width: 100%"
          row-class-name="training-record-row"
          @row-click="handleRowClick"
          stripe
          v-loading="loading"
        >
          <el-table-column prop="date" label="训练日期" width="120" />
          <el-table-column prop="exerciseName" label="动作名称" width="150" />
          <el-table-column prop="exerciseType" label="类型" width="100" />
          <el-table-column prop="weight" label="重量" width="100">
            <template #default="scope">
              {{ scope?.row?.weight }} kg
            </template>
          </el-table-column>
          <el-table-column prop="sets" label="组数" width="80" />
          <el-table-column prop="reps" label="次数" width="80" />
          <el-table-column prop="trainingVolume" label="训练量" width="120">
            <template #default="scope">
              {{ formatNumber(scope?.row?.trainingVolume) }} kg
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope?.row?.status === 'completed' ? 'success' : 'warning'" size="small">
                {{ scope?.row?.status === 'completed' ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="text" size="small" @click.stop="editRecord(scope?.row)">
                编辑
              </el-button>
              <el-button type="text" size="small" @click.stop="deleteRecord(scope?.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fitnessApi } from '../api/fitness'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const recentTrainingRecords = ref([])

// 方法
const loadRecentRecords = async () => {
  loading.value = true
  try {
    // 从API获取最近的训练记录
    const response = await fitnessApi.getRecentTrainingRecords()
    
    if (response.data && Array.isArray(response.data)) {
      recentTrainingRecords.value = response.data
    } else {
      throw new Error('API返回数据格式错误')
    }
  } catch (error) {
    console.error('加载训练记录失败:', error)
    ElMessage.error('加载训练记录失败')
    
    // 如果API调用失败，使用空数组
    recentTrainingRecords.value = []
  } finally {
    loading.value = false
  }
}

const goToTrainingData = () => {
  router.push('/training-data')
}

const navigateTo = (route) => {
  router.push(`/${route}`)
}

const handleRowClick = (row) => {
  // 可以添加行点击事件处理
  console.log('点击了训练记录:', row)
}

const editRecord = (record) => {
  ElMessage.info(`编辑记录: ${record.exerciseName}`)
  // 这里可以跳转到编辑页面或打开编辑对话框
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${record.exerciseName}" 这条训练记录吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 从列表中移除记录
    const index = recentTrainingRecords.value.findIndex(r => r.id === record.id)
    if (index > -1) {
      recentTrainingRecords.value.splice(index, 1)
      ElMessage.success('记录已删除')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num)
}

// 生命周期
onMounted(() => {
  loadRecentRecords()
})
</script>

<style scoped>
.recent-activity {
  width: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 1.3rem;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.activity-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
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

/* 表格样式优化 */
:deep(.el-table) {
  background: transparent;
}

:deep(.el-table__header) {
  background: rgba(255, 255, 255, 0.8);
}

:deep(.el-table__body) {
  background: transparent;
}

:deep(.el-table__row) {
  background: rgba(255, 255, 255, 0.6);
  transition: all 0.2s ease;
}

:deep(.el-table__row:hover) {
  background: rgba(255, 255, 255, 0.9);
}

:deep(.el-table__row.striped) {
  background: rgba(255, 255, 255, 0.4);
}

:deep(.el-table__row.striped:hover) {
  background: rgba(255, 255, 255, 0.8);
}

:deep(.training-record-row) {
  cursor: pointer;
}

:deep(.el-table th) {
  background: rgba(59, 130, 246, 0.1);
  color: #1e293b;
  font-weight: 600;
  border-bottom: 2px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 12px 16px;
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .activity-content {
    padding: 20px 16px;
  }
  
  .empty-activity {
    padding: 40px 16px;
  }
  
  :deep(.el-table) {
    font-size: 14px;
  }
  
  :deep(.el-table td) {
    padding: 8px 12px;
  }
}

@media (max-width: 480px) {
  :deep(.el-table-column) {
    min-width: 80px;
  }
  
  .section-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .section-actions .el-button {
    width: 100%;
  }
}
</style>