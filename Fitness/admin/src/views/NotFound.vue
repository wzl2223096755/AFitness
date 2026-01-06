<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-message">抱歉，您访问的页面不存在或已被移除。</p>
      <div class="actions">
        <button class="btn-primary" @click="goHome">返回首页</button>
        <button class="btn-secondary" @click="goBack">返回上一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.not-found-content {
  max-width: 400px;
  padding: 40px;
}

.error-code {
  font-size: 8rem;
  font-weight: 800;
  background: linear-gradient(135deg, #e94560, #f39c12);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
}

.error-title {
  font-size: 1.5rem;
  color: #f8fafc;
  margin: 0 0 12px 0;
}

.error-message {
  color: #94a3b8;
  font-size: 1rem;
  margin: 0 0 32px 0;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(135deg, #e94560, #f39c12);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(233, 69, 96, 0.3);
}

.btn-secondary {
  background: rgba(233, 69, 96, 0.1);
  color: #e94560;
  border: 1px solid rgba(233, 69, 96, 0.3);
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: rgba(233, 69, 96, 0.2);
}

@media (max-width: 480px) {
  .error-code {
    font-size: 5rem;
  }
  
  .actions {
    flex-direction: column;
  }
}
</style>
