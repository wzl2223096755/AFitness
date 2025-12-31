/**
 * Vue 图片懒加载指令
 * 使用方式: v-lazy="imageUrl" 或 v-lazy-bg="backgroundUrl"
 */

import { createLazyLoadObserver, lazyLoadConfig } from '@/utils/resourceOptimization'

// 全局观察器实例
let observer = null

/**
 * 获取或创建观察器
 */
function getObserver() {
  if (!observer) {
    observer = createLazyLoadObserver()
  }
  return observer
}

/**
 * v-lazy 指令 - 图片懒加载
 */
export const lazyDirective = {
  mounted(el, binding) {
    // 设置占位图
    if (el.tagName === 'IMG') {
      el.src = lazyLoadConfig.placeholder
      el.dataset.src = binding.value
      el.classList.add('lazy-image')
    }
    
    // 开始观察
    getObserver().observe(el)
  },
  
  updated(el, binding) {
    // 如果URL变化，重新设置
    if (binding.value !== binding.oldValue) {
      el.dataset.src = binding.value
      el.classList.remove('lazy-loaded')
      el.classList.add('lazy-image')
      getObserver().observe(el)
    }
  },
  
  unmounted(el) {
    getObserver().unobserve(el)
  }
}

/**
 * v-lazy-bg 指令 - 背景图片懒加载
 */
export const lazyBgDirective = {
  mounted(el, binding) {
    el.dataset.bgSrc = binding.value
    el.classList.add('lazy-image')
    getObserver().observe(el)
  },
  
  updated(el, binding) {
    if (binding.value !== binding.oldValue) {
      el.dataset.bgSrc = binding.value
      el.classList.remove('lazy-loaded')
      el.classList.add('lazy-image')
      getObserver().observe(el)
    }
  },
  
  unmounted(el) {
    getObserver().unobserve(el)
  }
}

/**
 * v-lazy-srcset 指令 - 响应式图片懒加载
 */
export const lazySrcsetDirective = {
  mounted(el, binding) {
    if (el.tagName === 'IMG') {
      el.src = lazyLoadConfig.placeholder
      el.dataset.src = binding.value.src
      if (binding.value.srcset) {
        el.dataset.srcset = binding.value.srcset
      }
      el.classList.add('lazy-image')
      getObserver().observe(el)
    }
  },
  
  updated(el, binding) {
    if (binding.value.src !== binding.oldValue?.src) {
      el.dataset.src = binding.value.src
      if (binding.value.srcset) {
        el.dataset.srcset = binding.value.srcset
      }
      el.classList.remove('lazy-loaded')
      el.classList.add('lazy-image')
      getObserver().observe(el)
    }
  },
  
  unmounted(el) {
    getObserver().unobserve(el)
  }
}

/**
 * 注册所有懒加载指令
 * @param {App} app - Vue应用实例
 */
export function registerLazyDirectives(app) {
  app.directive('lazy', lazyDirective)
  app.directive('lazy-bg', lazyBgDirective)
  app.directive('lazy-srcset', lazySrcsetDirective)
}

export default {
  lazyDirective,
  lazyBgDirective,
  lazySrcsetDirective,
  registerLazyDirectives
}
