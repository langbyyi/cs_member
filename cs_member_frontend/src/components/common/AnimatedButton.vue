<!--
  高级动画按钮组件
  演示如何使用增强动画效果库
-->
<template>
  <button
    :class="[
      'animated-button',
      animationClass,
      {
        'loading': loading,
        'disabled': disabled,
        'success': status === 'success',
        'error': status === 'error',
        'warning': status === 'warning'
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-spinner">
      <div class="spinner-dot"></div>
      <div class="spinner-dot"></div>
      <div class="spinner-dot"></div>
    </div>

    <!-- 按钮内容 -->
    <span class="button-content" :class="{ 'loading-content': loading }">
      <slot>
        {{ text }}
      </slot>
    </span>

    <!-- 波纹效果 -->
    <div class="ripple-container"></div>

    <!-- 状态图标 -->
    <div v-if="status" class="status-icon">
      <el-icon v-if="status === 'success'"><CircleCheck /></el-icon>
      <el-icon v-else-if="status === 'error'"><CircleClose /></el-icon>
      <el-icon v-else-if="status === 'warning'"><Warning /></el-icon>
    </div>
  </button>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CircleCheck, CircleClose, Warning } from '@element-plus/icons-vue'

// Props 定义
interface Props {
  text?: string
  loading?: boolean
  disabled?: boolean
  status?: 'success' | 'error' | 'warning' | null
  animationType?: 'ripple' | 'magnetic' | 'glow' | 'liquid' | 'bounce'
}

const props = withDefaults(defineProps<Props>(), {
  text: '按钮',
  loading: false,
  disabled: false,
  status: null,
  animationType: 'ripple'
})

// Emits 定义
const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

// 动画类计算
const animationClass = computed(() => {
  const classes = []

  // 基础动画类
  classes.push('ripple-button-enhanced')
  classes.push('haptic-feedback-enhanced')
  classes.push('hover-enhanced')

  // 特定动画类型
  switch (props.animationType) {
    case 'magnetic':
      classes.push('magnetic-hover')
      break
    case 'glow':
      classes.push('glow-effect')
      break
    case 'liquid':
      classes.push('liquid-fill')
      break
    case 'bounce':
      classes.push('loading-bounce')
      break
  }

  return classes
})

// 点击处理
const handleClick = (event: MouseEvent) => {
  if (props.disabled || props.loading) return

  // 创建波纹效果
  createRipple(event)

  // 触发点击事件
  emit('click', event)
}

// 创建波纹效果
const createRipple = (event: MouseEvent) => {
  const button = event.currentTarget as HTMLElement
  const rippleContainer = button.querySelector('.ripple-container')

  if (!rippleContainer) return

  const ripple = document.createElement('span')
  ripple.className = 'ripple-effect'

  // 计算波纹位置和大小
  const rect = button.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  const x = event.clientX - rect.left - size / 2
  const y = event.clientY - rect.top - size / 2

  ripple.style.width = ripple.style.height = size + 'px'
  ripple.style.left = x + 'px'
  ripple.style.top = y + 'px'

  rippleContainer.appendChild(ripple)

  // 动画完成后移除波纹
  setTimeout(() => {
    ripple.remove()
  }, 600)
}
</script>

<style scoped>
/* 基础按钮样式 */
.animated-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  isolation: isolate;
  transform-style: preserve-3d;
}

.animated-button:hover:not(.disabled) {
  transform: translateY(-2px) translateZ(10px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.4);
}

.animated-button:active:not(.disabled) {
  transform: translateY(0) translateZ(5px);
}

.animated-button.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
}

/* 状态样式 */
.animated-button.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.3);
}

.animated-button.error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.3);
}

.animated-button.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.3);
}

/* 加载样式 */
.animated-button.loading {
  pointer-events: none;
}

.loading-spinner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 4px;
}

.spinner-dot {
  width: 4px;
  height: 4px;
  background: white;
  border-radius: 50%;
  animation: spinnerBounce 1.4s ease-in-out infinite;
}

.spinner-dot:nth-child(1) { animation-delay: -0.32s; }
.spinner-dot:nth-child(2) { animation-delay: -0.16s; }
.spinner-dot:nth-child(3) { animation-delay: 0s; }

@keyframes spinnerBounce {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.button-content {
  transition: opacity 0.3s ease;
}

.loading-content {
  opacity: 0;
}

/* 波纹容器 */
.ripple-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
  border-radius: inherit;
}

.ripple-effect {
  position: absolute;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  transform: scale(0);
  animation: rippleExpand 0.6s ease-out;
  pointer-events: none;
}

@keyframes rippleExpand {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

/* 状态图标 */
.status-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
  animation: statusIconAppear 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes statusIconAppear {
  0% {
    transform: translateY(-50%) scale(0) rotate(-45deg);
    opacity: 0;
  }
  50% {
    transform: translateY(-50%) scale(1.2) rotate(-45deg);
    opacity: 1;
  }
  100% {
    transform: translateY(-50%) scale(1) rotate(0deg);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .animated-button {
    padding: 10px 20px;
    font-size: 14px;
  }
}
</style>

<!-- 使用示例 -->
<!--
<template>
  <AnimatedButton
    text="提交"
    :loading="submitting"
    :status="submitStatus"
    animation-type="ripple"
    @click="handleSubmit"
  />
</template>
-->