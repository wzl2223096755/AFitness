<template>
  <div 
    class="lazy-image-container" 
    :class="containerClass"
    :style="containerStyle"
  >
    <!-- 占位符 -->
    <div 
      v-if="!loaded && placeholder" 
      class="lazy-image__placeholder"
      :style="{ backgroundImage: `url(${placeholder})` }"
    />
    
    <!-- 实际图片 -->
    <img
      ref="imageRef"
      :src="currentSrc"
      :alt="alt"
      :class="['lazy-image__img', { 'lazy-loaded': loaded, 'lazy-error': error }]"
      :style="imageStyle"
      @load="onLoad"
      @error="onError"
    />
    
    <!-- 加载中指示器 -->
    <div v-if="loading && showLoading" class="lazy-image__loading">
      <span class="loading-spinner"></span>
    </div>
    
    <!-- 错误状态 -->
    <div v-if="error && showError" class="lazy-image__error">
      <span>{{ errorText }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { lazyLoadConfig } from '@/utils/resourceOptimization'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: lazyLoadConfig.placeholder
  },
  aspectRatio: {
    type: String,
    default: null,
    validator: (value) => ['16-9', '4-3', '1-1', null].includes(value)
  },
  objectFit: {
    type: String,
    default: 'cover'
  },
  width: {
    type: [String, Number],
    default: null
  },
  height: {
    type: [String, Number],
    default: null
  },
  lazy: {
    type: Boolean,
    default: true
  },
  showLoading: {
    type: Boolean,
    default: false
  },
  showError: {
    type: Boolean,
    default: true
  },
  errorText: {
    type: String,
    default: '加载失败'
  },
  rootMargin: {
    type: String,
    default: '50px 0px'
  }
})

const emit = defineEmits(['load', 'error'])

const imageRef = ref(null)
const loaded = ref(false)
const loading = ref(true)
const error = ref(false)
const isIntersecting = ref(false)

// 当前显示的图片源
const currentSrc = computed(() => {
  if (!props.lazy) return props.src
  return isIntersecting.value ? props.src : props.placeholder
})

// 容器类名
const containerClass = computed(() => {
  const classes = []
  if (props.aspectRatio) {
    classes.push(`lazy-image-container--${props.aspectRatio}`)
  }
  return classes
})

// 容器样式
const containerStyle = computed(() => {
  const style = {}
  if (props.width) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  if (props.height && !props.aspectRatio) {
    style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  return style
})

// 图片样式
const imageStyle = computed(() => ({
  objectFit: props.objectFit
}))

// 创建观察器
let observer = null

const createObserver = () => {
  if (!props.lazy || !('IntersectionObserver' in window)) {
    isIntersecting.value = true
    return
  }
  
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          isIntersecting.value = true
          observer?.unobserve(entry.target)
        }
      })
    },
    {
      rootMargin: props.rootMargin,
      threshold: 0.01
    }
  )
  
  if (imageRef.value) {
    observer.observe(imageRef.value)
  }
}

const onLoad = () => {
  loaded.value = true
  loading.value = false
  error.value = false
  emit('load')
}

const onError = () => {
  loaded.value = false
  loading.value = false
  error.value = true
  emit('error')
}

// 监听src变化
watch(() => props.src, () => {
  loaded.value = false
  loading.value = true
  error.value = false
})

onMounted(() => {
  createObserver()
})

onUnmounted(() => {
  if (observer && imageRef.value) {
    observer.unobserve(imageRef.value)
    observer.disconnect()
  }
})
</script>

<style scoped lang="scss">
.lazy-image-container {
  position: relative;
  overflow: hidden;
  background-color: rgba(26, 26, 46, 0.5);
}

.lazy-image__placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  filter: blur(10px);
  transform: scale(1.1);
  transition: opacity 0.3s ease;
}

.lazy-image__img {
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  &.lazy-loaded {
    opacity: 1;
  }
  
  &.lazy-error {
    opacity: 0;
  }
}

.lazy-image__loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  
  .loading-spinner {
    display: block;
    width: 24px;
    height: 24px;
    border: 2px solid rgba(99, 102, 241, 0.3);
    border-top-color: #6366f1;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

.lazy-image__error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(239, 68, 68, 0.8);
  font-size: 12px;
  text-align: center;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 宽高比容器 */
.lazy-image-container--16-9 {
  padding-bottom: 56.25%;
  
  .lazy-image__img,
  .lazy-image__placeholder {
    position: absolute;
    top: 0;
    left: 0;
  }
}

.lazy-image-container--4-3 {
  padding-bottom: 75%;
  
  .lazy-image__img,
  .lazy-image__placeholder {
    position: absolute;
    top: 0;
    left: 0;
  }
}

.lazy-image-container--1-1 {
  padding-bottom: 100%;
  
  .lazy-image__img,
  .lazy-image__placeholder {
    position: absolute;
    top: 0;
    left: 0;
  }
}
</style>
