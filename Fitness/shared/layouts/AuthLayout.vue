<template>
  <div class="auth-layout">
    <div class="auth-background">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
    </div>
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <slot name="logo">
            <div class="logo-wrapper">
              <span class="logo-icon">🏋️</span>
              <h1 class="logo-text">AFitness</h1>
            </div>
          </slot>
          <slot name="title">
            <h2 class="auth-title">{{ title }}</h2>
          </slot>
          <slot name="subtitle">
            <p class="auth-subtitle" v-if="subtitle">{{ subtitle }}</p>
          </slot>
        </div>
        <div class="auth-content">
          <slot></slot>
        </div>
        <div class="auth-footer" v-if="$slots.footer">
          <slot name="footer"></slot>
        </div>
      </div>
      <div class="auth-extra" v-if="$slots.extra">
        <slot name="extra"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '欢迎回来'
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.auth-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.auth-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 48px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.logo-text {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.auth-content {
  margin-bottom: 24px;
}

.auth-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid var(--border-default, #e2e8f0);
}

.auth-extra {
  margin-top: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 480px) {
  .auth-container {
    padding: 16px;
  }
  
  .auth-card {
    padding: 24px;
    border-radius: 16px;
  }
  
  .logo-icon {
    font-size: 36px;
  }
  
  .logo-text {
    font-size: 24px;
  }
  
  .auth-title {
    font-size: 20px;
  }
}

/* 深色主题适配 - 使用 data-theme 属性 */
[data-theme="dark"] .auth-card {
  background: rgba(30, 41, 59, 0.95);
}

[data-theme="dark"] .auth-subtitle {
  color: #94a3b8;
}

[data-theme="dark"] .auth-footer {
  border-top-color: #334155;
}
</style>
