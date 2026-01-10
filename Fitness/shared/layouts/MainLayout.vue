<template>
  <div class="main-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <aside class="main-sidebar" v-if="showSidebar">
      <div class="sidebar-header">
        <slot name="logo">
          <h1 class="logo">🏋️ AFitness</h1>
        </slot>
        <button class="collapse-btn" @click="toggleSidebar" v-if="collapsible">
          <span>{{ sidebarCollapsed ? '→' : '←' }}</span>
        </button>
      </div>
      <nav class="sidebar-nav">
        <slot name="navigation"></slot>
      </nav>
      <div class="sidebar-footer">
        <slot name="sidebar-footer"></slot>
      </div>
    </aside>
    <main class="main-content">
      <header class="main-header" v-if="showHeader">
        <div class="header-left">
          <slot name="header-left"></slot>
        </div>
        <div class="header-center">
          <slot name="header-title">
            <h2>{{ title }}</h2>
          </slot>
        </div>
        <div class="header-right">
          <slot name="header-right"></slot>
        </div>
      </header>
      <div class="content-wrapper">
        <slot></slot>
      </div>
      <footer class="main-footer" v-if="showFooter">
        <slot name="footer">
          <p>&copy; {{ currentYear }} AFitness. All rights reserved.</p>
        </slot>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showSidebar: {
    type: Boolean,
    default: true
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  showFooter: {
    type: Boolean,
    default: false
  },
  collapsible: {
    type: Boolean,
    default: true
  },
  defaultCollapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['sidebar-toggle'])

const sidebarCollapsed = ref(props.defaultCollapsed)

const currentYear = computed(() => new Date().getFullYear())

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  emit('sidebar-toggle', sidebarCollapsed.value)
}

defineExpose({
  sidebarCollapsed,
  toggleSidebar
})
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

.main-sidebar {
  width: 260px;
  background: var(--bg-elevated);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  flex-shrink: 0;
  border-right: 1px solid var(--border-default);
}

.sidebar-collapsed .main-sidebar {
  width: 64px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-default);
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-collapsed .logo {
  display: none;
}

.collapse-btn {
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.collapse-btn:hover {
  background: var(--hover-bg);
  color: var(--text-primary);
}

.sidebar-nav {
  flex: 1;
  padding: 16px 8px;
  overflow-y: auto;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-default);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border-default);
  flex-shrink: 0;
}

.main-header h2 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.main-footer {
  padding: 16px 24px;
  background: var(--bg-elevated);
  border-top: 1px solid var(--border-default);
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
}

.main-footer p {
  margin: 0;
}

@media (max-width: 768px) {
  .main-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
  }
  
  .main-layout:not(.sidebar-collapsed) .main-sidebar {
    transform: translateX(0);
  }
  
  .content-wrapper {
    padding: 16px;
  }
}
</style>
