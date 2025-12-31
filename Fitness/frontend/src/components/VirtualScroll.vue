<template>
  <div 
    class="virtual-scroll-container"
    :style="{ height: containerHeight + 'px' }"
    @scroll="handleScroll"
    ref="containerRef"
  >
    <div 
      class="virtual-scroll-content"
      :style="{ height: totalHeight + 'px', position: 'relative' }"
    >
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item)"
        class="virtual-scroll-item"
        :style="{
          position: 'absolute',
          top: getItemTop(item) + 'px',
          left: '0',
          right: '0',
          height: itemHeight + 'px'
        }"
      >
        <slot :item="item" :index="getItemIndex(item)"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { PerformanceUtils } from '@/utils/performance'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  itemHeight: {
    type: Number,
    default: 50
  },
  containerHeight: {
    type: Number,
    required: true
  },
  keyField: {
    type: String,
    default: 'id'
  },
  bufferSize: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits(['scroll'])

const containerRef = ref(null)
const scrollTop = ref(0)

// 计算总高度
const totalHeight = computed(() => {
  return props.items.length * props.itemHeight
})

// 计算可见项目
const visibleItems = computed(() => {
  const result = PerformanceUtils.calculateVisibleItems(
    props.items,
    scrollTop.value,
    props.containerHeight,
    props.itemHeight
  )
  
  // 添加缓冲区项目
  const startIdx = Math.max(0, result.startIndex - props.bufferSize)
  const endIdx = Math.min(
    props.items.length - 1,
    result.endIndex + props.bufferSize
  )
  
  return props.items.slice(startIdx, endIdx + 1)
})

// 获取项目键值
const getItemKey = (item) => {
  return item[props.keyField] || Math.random().toString(36)
}

// 获取项目索引
const getItemIndex = (item) => {
  return props.items.indexOf(item)
}

// 获取项目顶部位置
const getItemTop = (item) => {
  return getItemIndex(item) * props.itemHeight
}

// 节流滚动处理
const handleScroll = PerformanceUtils.throttle((event) => {
  scrollTop.value = event.target.scrollTop
  emit('scroll', {
    scrollTop: scrollTop.value,
    scrollLeft: event.target.scrollLeft,
    scrollHeight: event.target.scrollHeight,
    clientHeight: event.target.clientHeight
  })
}, 16) // 约60fps

// 滚动到指定项目
const scrollToItem = (index) => {
  if (containerRef.value) {
    const targetScrollTop = index * props.itemHeight
    containerRef.value.scrollTop = targetScrollTop
  }
}

// 滚动到顶部
const scrollToTop = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = totalHeight.value - props.containerHeight
  }
}

// 监听items变化，重置滚动位置
watch(() => props.items.length, () => {
  scrollTop.value = 0
})

// 暴露方法
defineExpose({
  scrollToItem,
  scrollToTop,
  scrollToBottom
})
</script>

<style scoped>
.virtual-scroll-container {
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

.virtual-scroll-content {
  position: relative;
}

.virtual-scroll-item {
  box-sizing: border-box;
}

/* 滚动条样式 */
.virtual-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.virtual-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>