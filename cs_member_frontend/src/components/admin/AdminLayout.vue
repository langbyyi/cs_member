<template>
  <div class="admin-layout">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="admin-content">
      <slot></slot>
    </div>

    <!-- 浮动操作按钮 -->
    <div class="floating-actions" v-if="showFloatingActions">
      <el-tooltip content="回到顶部" placement="left">
        <el-button
          circle
          :icon="ArrowUp"
          class="floating-btn up-button"
          @click="scrollToTop"
        />
      </el-tooltip>

      <el-tooltip content="全屏模式" placement="left">
        <el-button
          circle
          :icon="FullScreen"
          class="floating-btn fullscreen-toggle"
          @click="toggleFullscreen"
        />
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowUp, FullScreen } from '@element-plus/icons-vue'

interface Props {
  showFloatingActions?: boolean
}

withDefaults(defineProps<Props>(), {
  showFloatingActions: true
})

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}


// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

</script>

<style scoped>
/* 管理布局容器 */
.admin-layout {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰元素 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.6;
  animation: float 6s ease-in-out infinite;
}

.bg-orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  top: -200px;
  right: -100px;
  animation-delay: 0s;
}

.bg-orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1) 0%, rgba(16, 185, 129, 0.1) 100%);
  bottom: -150px;
  left: -100px;
  animation-delay: 2s;
}

.bg-orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(245, 158, 11, 0.08) 100%);
  top: 50%;
  left: 30%;
  animation-delay: 4s;
}

@keyframes float {
 0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(-20px) rotate(180deg) scale(1.1);
  }
}

/* 背景网格 */
.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

/* 主要内容区域 */
.admin-content {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

/* 浮动操作按钮 */
.floating-actions {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
}

.floating-btn {
  width: 56px;
  height: 56px;
  border-radius: 50% !important;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
  border: none !important;
  color: white !important;
  font-size: 20px;
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.floating-btn:hover::before {
  left: 100%;
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.1) !important;
  box-shadow: 0 12px 35px rgba(99, 102, 241, 0.4) !important;
}

/* 确保Element Plus按钮不影响对齐 */
.floating-actions .el-button {
  margin: 0 !important;
  padding: 0 !important;
  line-height: 1 !important;
}

/* 移除快捷键提示 */
.floating-actions .el-button::after {
  display: none !important;
}

/* 隐藏Element Plus按钮的快捷键提示 */
.floating-actions .el-button .el-button__shortcut-key {
  display: none !important;
}

/* 移除所有快捷键相关的伪元素 */
.floating-actions .el-button:not([disabled])::before {
  content: none !important;
}

/* 全局隐藏Element Plus按钮的快捷键提示 */
.floating-actions .el-button [class*="shortcut"] {
  display: none !important;
}

/* 强制隐藏可能的快捷键提示元素 */
.floating-actions .el-button .el-tooltip__trigger,
.floating-actions .el-button .el-button__text + .el-button__suffix,
.floating-actions .el-button .el-button__icon + .el-button__text {
  position: relative !important;
}

.floating-actions .el-button .shortcut-key,
.floating-actions .el-button [data-shortcut] {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
}

.up-button {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.3) !important;
}

.up-button:hover {
  box-shadow: 0 12px 35px rgba(59, 130, 246, 0.4) !important;
}

.fullscreen-toggle {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.3) !important;
}

.fullscreen-toggle:hover {
  box-shadow: 0 12px 35px rgba(239, 68, 68, 0.4) !important;
}

/* 按钮进入动画 */
.floating-btn {
  animation: slideInRight 0.5s ease-out forwards;
  opacity: 0;
}

.floating-btn:nth-child(1) {
  animation-delay: 0.1s;
}

.floating-btn:nth-child(2) {
  animation-delay: 0.2s;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-content {
    padding: 16px;
  }

  .floating-actions {
    bottom: 24px;
    right: 24px;
    gap: 10px;
  }

  .floating-btn {
    width: 48px;
    height: 48px;
    font-size: 18px;
  }

  .bg-orb-1 {
    width: 300px;
    height: 300px;
    top: -150px;
    right: -50px;
  }

  .bg-orb-2 {
    width: 200px;
    height: 200px;
    bottom: -100px;
    left: -50px;
  }

  .bg-orb-3 {
    width: 150px;
    height: 150px;
  }
}

@media (max-width: 480px) {
  .admin-content {
    padding: 12px;
  }

  .floating-actions {
    bottom: 16px;
    right: 16px;
    gap: 8px;
  }

  .floating-btn {
    width: 44px;
    height: 44px;
    font-size: 16px;
  }
}


/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .bg-orb,
  .bg-grid,
  .floating-btn {
    animation: none !important;
  }

  .floating-btn::before {
    display: none !important;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .floating-btn {
    border: 2px solid #fff !important;
  }

  .bg-grid {
    opacity: 0.5;
  }
}
</style>