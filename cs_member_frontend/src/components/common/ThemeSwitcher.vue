<template>
  <div class="theme-switcher">
    <button
      class="theme-toggle"
      @click="toggleTheme"
      :title="themeInfo.description"
    >
      <div class="theme-toggle-track">
        <div class="theme-toggle-thumb">
          <transition name="icon" mode="out-in">
            <svg
              v-if="isDark"
              key="moon"
              class="theme-icon moon-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"
                fill="currentColor"
              />
            </svg>
            <svg
              v-else
              key="sun"
              class="theme-icon sun-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle cx="12" cy="12" r="5" fill="currentColor"/>
              <path
                d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </transition>
        </div>
      </div>
    </button>

    <!-- 主题提示 -->
    <transition name="tooltip">
      <div v-if="showTooltip" class="theme-tooltip">
        {{ themeInfo.name }}
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const showTooltip = ref(false)
const tooltipTimer = ref<NodeJS.Timeout>()

const isDark = computed(() => themeStore.isDark)
const themeInfo = computed(() => themeStore.themeInfo)

const toggleTheme = () => {
  themeStore.toggleTheme()
  showTooltip.value = true

  // 清除之前的定时器
  if (tooltipTimer.value) {
    clearTimeout(tooltipTimer.value)
  }

  // 2秒后隐藏提示
  tooltipTimer.value = setTimeout(() => {
    showTooltip.value = false
  }, 2000)
}

// 清理定时器
onUnmounted(() => {
  if (tooltipTimer.value) {
    clearTimeout(tooltipTimer.value)
  }
})
</script>

<style scoped>
.theme-switcher {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 1000;
}

.theme-toggle {
  position: relative;
  width: 68px;
  height: 36px;
  border: none;
  border-radius: 50px;
  background: var(--border-secondary);
  cursor: pointer;
  padding: 3px;
  transition: all 0.3s var(--ease-out-back);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(10px);
}

.theme-toggle:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-lg);
}

.theme-toggle:active {
  transform: scale(0.95);
}

.theme-toggle-track {
  position: relative;
  width: 100%;
  height: 100%;
  background: var(--bg-tertiary);
  border-radius: 50px;
  overflow: hidden;
}

.theme-toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 28px;
  height: 28px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s var(--ease-spring);
  box-shadow: var(--shadow-glow);
}

/* 深色主题时滑块位置 */
.theme-dark .theme-toggle-thumb {
  left: 34px;
}

.theme-icon {
  width: 16px;
  height: 16px;
  color: var(--text-inverse);
  transition: all 0.3s ease;
}

.sun-icon {
  animation: rotate-sun 20s linear infinite;
}

.moon-icon {
  animation: float-moon 3s ease-in-out infinite;
}

/* 图标切换动画 */
.icon-enter-active,
.icon-leave-active {
  transition: all 0.3s var(--ease-out-back);
}

.icon-enter-from {
  opacity: 0;
  transform: rotate(-180deg) scale(0);
}

.icon-leave-to {
  opacity: 0;
  transform: rotate(180deg) scale(0);
}

/* 主题提示 */
.theme-tooltip {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 12px;
  padding: 8px 16px;
  background: var(--card-bg);
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(10px);
  pointer-events: none;
}

.theme-tooltip::before {
  content: '';
  position: absolute;
  top: -6px;
  right: 20px;
  width: 12px;
  height: 12px;
  background: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-bottom: none;
  border-right: none;
  transform: rotate(45deg);
}

/* 提示动画 */
.tooltip-enter-active,
.tooltip-leave-active {
  transition: all 0.3s var(--ease-out-back);
}

.tooltip-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.9);
}

.tooltip-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.9);
}

/* 动画定义 */
@keyframes rotate-sun {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes float-moon {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-2px); }
}

/* 主题过渡时的特殊效果 */
.theme-transitioning .theme-toggle {
  animation: theme-switch-pulse 0.8s var(--ease-out-back);
}

@keyframes theme-switch-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-switcher {
    top: 16px;
    right: 16px;
  }

  .theme-toggle {
    width: 60px;
    height: 32px;
  }

  .theme-toggle-thumb {
    width: 24px;
    height: 24px;
  }

  .theme-dark .theme-toggle-thumb {
    left: 30px;
  }

  .theme-icon {
    width: 14px;
    height: 14px;
  }

  .theme-tooltip {
    font-size: 12px;
    padding: 6px 12px;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .theme-toggle {
    border: 2px solid var(--text-primary);
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .sun-icon {
    animation: none;
  }

  .moon-icon {
    animation: none;
  }

  .theme-transitioning .theme-toggle {
    animation: none;
  }

  .theme-toggle,
  .theme-toggle-thumb {
    transition: none;
  }
}
</style>