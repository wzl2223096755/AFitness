<template>
  <div class="sync-status-indicator" :class="statusClass">
    <!-- 状态图标 -->
    <div class="status-icon" @click="handleClick">
      <el-icon v-if="isOffline" class="offline-icon">
        <Warning />
      </el-icon>
      <el-icon v-else-if="syncInProgress" class="syncing-icon">
        <Loading />
      </el-icon>
      <el-icon v-else-if="hasPendingSync" class="pending-icon">
        <Upload />
      </el-icon>
      <el-icon v-else class="synced-icon">
        <CircleCheck />
      </el-icon>
    </div>
    
    <!-- 状态文本 -->
    <span v-if="showText" class="status-text">{{ statusText }}</span>
    
    <!-- 待同步数量徽章 -->
    <el-badge 
      v-if="hasPendingSync && !syncInProgress" 
      :value="pendingCount" 
      class="pending-badge"
      :max="99"
    />
    
    <!-- 同步进度 -->
    <el-progress
      v-if="syncInProgress && syncProgress.total > 0"
      :percentage="progressPercentage"
      :stroke-width="3"
      :show-text="false"
      class="sync-progress"
    />
    
    <!-- 提示弹窗 -->
    <el-popover
      v-if="showPopover"
      placement="bottom"
      :width="200"
      trigger="hover"
    >
      <template #reference>
        <div class="popover-trigger"></div>
      </template>
      <div class="sync-info">
        <p><strong>网络状态:</strong> {{ online ? '在线' : '离线' }}</p>
        <p><strong>同步状态:</strong> {{ syncStatus }}</p>
        <p v-if="pendingCount > 0"><strong>待同步:</strong> {{ pendingCount }} 条</p>
        <p v-if="lastSyncTime"><strong>上次同步:</strong> {{ formatTime(lastSyncTime) }}</p>
        <el-button 
          v-if="hasPendingSync && online && !syncInProgress"
          type="primary" 
          size="small" 
          @click="handleSync"
        >
          立即同步
        </el-button>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Warning, Loading, Upload, CircleCheck } from '@element-plus/icons-vue'
import { useOfflineSync } from '@/composables/useOfflineSync'
import dayjs from 'dayjs'

const props = defineProps({
  showText: {
    type: Boolean,
    default: false
  },
  showPopover: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click', 'sync'])

const {
  online,
  isOffline,
  syncStatus,
  syncInProgress,
  lastSyncTime,
  pendingCount,
  hasPendingSync,
  statusText,
  syncProgress,
  sync
} = useOfflineSync()

// 计算属性
const statusClass = computed(() => ({
  'is-offline': isOffline.value,
  'is-syncing': syncInProgress.value,
  'has-pending': hasPendingSync.value && !syncInProgress.value,
  'is-synced': !isOffline.value && !syncInProgress.value && !hasPendingSync.value
}))

const progressPercentage = computed(() => {
  if (syncProgress.value.total === 0) return 0
  return Math.round((syncProgress.value.current / syncProgress.value.total) * 100)
})

// 方法
function formatTime(timestamp) {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('HH:mm:ss')
}

function handleClick() {
  emit('click')
}

async function handleSync() {
  emit('sync')
  await sync()
}
</script>

<style scoped lang="scss">
.sync-status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  
  &.is-offline {
    background-color: rgba(245, 108, 108, 0.1);
    color: #f56c6c;
  }
  
  &.is-syncing {
    background-color: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }
  
  &.has-pending {
    background-color: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }
  
  &.is-synced {
    background-color: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }
}

.status-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  
  .syncing-icon {
    animation: spin 1s linear infinite;
  }
}

.status-text {
  white-space: nowrap;
}

.pending-badge {
  position: absolute;
  top: -4px;
  right: -4px;
}

.sync-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.popover-trigger {
  position: absolute;
  inset: 0;
}

.sync-info {
  p {
    margin: 4px 0;
    font-size: 12px;
  }
  
  .el-button {
    margin-top: 8px;
    width: 100%;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
