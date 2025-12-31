<template>
  <div class="particle-background" ref="particleContainer">
    <canvas ref="canvas" class="particle-canvas"></canvas>
    <div class="gradient-overlay"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

const canvas = ref(null)
const particleContainer = ref(null)
let ctx = null
let particles = []
let animationId = null
let mousePosition = { x: 0, y: 0 }
let resizeTimeout = null

// 粒子类
class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.reset()
    this.y = Math.random() * canvas.height
  }

  reset() {
    this.x = Math.random() * this.canvas.width
    this.y = Math.random() * this.canvas.height
    this.size = Math.random() * 2 + 0.5
    this.speedX = (Math.random() - 0.5) * 0.3
    this.speedY = (Math.random() - 0.5) * 0.3
    this.opacity = Math.random() * 0.5 + 0.3
    this.pulse = Math.random() * 0.05 + 0.02
    this.pulsePhase = Math.random() * Math.PI * 2
    // 离子色调：紫色/粉色/青色
    const colors = ['#ff00ff', '#7000ff', '#00f2fe']
    this.color = colors[Math.floor(Math.random() * colors.length)]
  }

  update(mouseX, mouseY) {
    this.x += this.speedX
    this.y += this.speedY

    this.pulsePhase += this.pulse
    const currentSize = this.size + Math.sin(this.pulsePhase) * 0.8

    if (mouseX && mouseY) {
      const dx = mouseX - this.x
      const dy = mouseY - this.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 150) {
        const force = (150 - distance) / 150
        this.x -= (dx / distance) * force * 3
        this.y -= (dy / distance) * force * 3
      }
    }

    if (this.x < -100) this.x = this.canvas.width + 100
    if (this.x > this.canvas.width + 100) this.x = -100
    if (this.y < -100) this.y = this.canvas.height + 100
    if (this.y > this.canvas.height + 100) this.y = -100

    return currentSize
  }

  draw(ctx, currentSize) {
    ctx.save()
    ctx.shadowBlur = 15
    ctx.shadowColor = this.color
    ctx.globalAlpha = this.opacity
    
    ctx.beginPath()
    ctx.arc(this.x, this.y, currentSize, 0, Math.PI * 2)
    ctx.fillStyle = this.color
    ctx.fill()
    
    // 核心亮点
    ctx.beginPath()
    ctx.arc(this.x, this.y, currentSize * 0.4, 0, Math.PI * 2)
    ctx.fillStyle = '#fff'
    ctx.fill()
    
    ctx.restore()
  }
}

// 初始化粒子
const initParticles = () => {
  if (!canvas.value) return
  
  const particleCount = Math.min(60, window.innerWidth / 30)
  particles = []
  
  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(canvas.value))
  }
}

// 连接临近粒子 - 离子电弧效果
const connectParticles = (ctx) => {
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 150) {
        const opacity = (1 - distance / 150) * 0.3
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        
        // 添加电弧抖动效果
        if (Math.random() > 0.95) {
          const midX = (particles[i].x + particles[j].x) / 2 + (Math.random() - 0.5) * 10
          const midY = (particles[i].y + particles[j].y) / 2 + (Math.random() - 0.5) * 10
          ctx.quadraticCurveTo(midX, midY, particles[j].x, particles[j].y)
        } else {
          ctx.lineTo(particles[j].x, particles[j].y)
        }
        
        ctx.strokeStyle = `rgba(112, 0, 255, ${opacity})`
        ctx.lineWidth = 0.8
        ctx.stroke()
      }
    }
  }
}

// 动画循环
const animate = () => {
  if (!ctx || !canvas.value) return
  
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  // 更新和绘制粒子
  particles.forEach(particle => {
    const currentSize = particle.update(mousePosition.x, mousePosition.y)
    particle.draw(ctx, currentSize)
  })
  
  // 连接粒子
  connectParticles(ctx)
  
  animationId = requestAnimationFrame(animate)
}

// 设置画布大小
const resizeCanvas = () => {
  if (!canvas.value || !particleContainer.value) return
  
  const rect = particleContainer.value.getBoundingClientRect()
  canvas.value.width = rect.width
  canvas.value.height = rect.height
  
  // 重新初始化粒子
  initParticles()
}

// 鼠标移动事件
const handleMouseMove = (e) => {
  if (!canvas.value) return
  
  const rect = canvas.value.getBoundingClientRect()
  mousePosition.x = e.clientX - rect.left
  mousePosition.y = e.clientY - rect.top
}

// 触摸事件（移动端支持）
const handleTouchMove = (e) => {
  if (!canvas.value) return
  
  const rect = canvas.value.getBoundingClientRect()
  const touch = e.touches[0]
  mousePosition.x = touch.clientX - rect.left
  mousePosition.y = touch.clientY - rect.top
}

// 防抖调整大小
const debouncedResize = () => {
  clearTimeout(resizeTimeout)
  resizeTimeout = setTimeout(() => {
    resizeCanvas()
  }, 250)
}

onMounted(async () => {
  await nextTick()
  
  if (!canvas.value) return
  
  ctx = canvas.value.getContext('2d')
  
  // 设置初始大小
  resizeCanvas()
  
  // 初始化粒子
  initParticles()
  
  // 添加事件监听器
  window.addEventListener('resize', debouncedResize)
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('touchmove', handleTouchMove)
  
  // 开始动画
  animate()
})

onUnmounted(() => {
  // 清理
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  
  window.removeEventListener('resize', debouncedResize)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('touchmove', handleTouchMove)
  
  clearTimeout(resizeTimeout)
})
</script>

<style scoped>
.particle-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
  background: #0a0a14;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  filter: blur(0.5px);
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 10% 10%, rgba(112, 0, 255, 0.15) 0%, transparent 40%),
    radial-gradient(circle at 90% 90%, rgba(255, 0, 255, 0.15) 0%, transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(0, 242, 254, 0.05) 0%, transparent 60%);
  pointer-events: none;
}

/* 离子化大光斑 */
.particle-background::before,
.particle-background::after {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
  z-index: -1;
  animation: float 20s infinite alternate;
}

.particle-background::before {
  background: radial-gradient(circle, #7000ff, transparent 70%);
  top: -200px;
  left: -200px;
}

.particle-background::after {
  background: radial-gradient(circle, #ff00ff, transparent 70%);
  bottom: -200px;
  right: -200px;
  animation-delay: -10s;
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(100px, 50px) scale(1.2); }
}

/* 深色主题适配 */
[data-theme="dark"] .particle-background {
  background: #05050a;
}

[data-theme="dark"] .gradient-overlay {
  opacity: 0.8;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .particle-background::before,
  .particle-background::after {
    width: 300px;
    height: 300px;
  }
}
</style>