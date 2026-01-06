<template>
  <div 
    v-if="showIndicator" 
    class="connection-status-indicator"
    :class="statusClass"
  >
    <div class="status-content">
      <span class="status-icon">
        <el-icon v-if="isConnected"><CircleCheck /></el-icon>
        <el-icon v-else-if="isChecking"><Loading /></el-icon>
        <el-icon v-else><CircleClose /></el-icon>
      </span>
      <span class="status-text">{{ statusText }}</span>
      <el-button 
        v-if="isDisconnected" 
        type="primary" 
        size="small" 
        @click="retry"
        :loading="isChecking"
      >
        重试
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import { useConnectionStatus, ConnectionStatus } from '@/composables/useConnectionStatus'

const props = defineProps({
  // 是否只在断开连接时显示
  showOnlyWhenDisconnected: {
    type: Boolean,
    default: true
  }
})

const { 
  status, 
  isConnected, 
  isDisconnected, 
  isChecking, 
  checkNow 
} = useConnectionStatus()

const showIndicator = computed(() => {
  if (props.showOnlyWhenDisconnected) {
    return status.value === ConnectionStatus.DISCONNECTED
  }
  return true
})

const statusClass = computed(() => {
  switch (status.value) {
    case ConnectionStatus.CONNECTED:
      return 'status-connected'
    case ConnectionStatus.DISCONNECTED:
      return 'status-disconnected'
    case ConnectionStatus.CHECKING:
      return 'status-checking'
    default:
      return 'status-unknown'
  }
})

const statusText = computed(() => {
  switch (status.value) {
    case ConnectionStatus.CONNECTED:
      return '已连接'
    case ConnectionStatus.DISCONNECTED:
      return '服务器连接断开'
    case ConnectionStatus.CHECKING:
      return '检查连接中...'
    default:
      return '未知状态'
  }
})

const retry = () => {
  checkNow()
}
</script>

<style scoped>
.connection-status-indicator {
  position: fixed;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  padding: 8px 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-icon {
  display: flex;
  align-items: center;
}

.status-text {
  font-size: 14px;
}

.status-connected {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  color: #67c23a;
}

.status-disconnected {
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  color: #f56c6c;
}

.status-checking {
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
  color: #e6a23c;
}

.status-unknown {
  background-color: #f4f4f5;
  border: 1px solid #e9e9eb;
  color: #909399;
}
</style>
