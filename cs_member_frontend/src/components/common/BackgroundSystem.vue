<template>
  <div
    ref="backgroundRef"
    :class="backgroundClasses"
    :style="backgroundStyles"
    class="background-system"
  >
    <!-- 背景装饰元素 -->
    <div class="bg-decoration" v-if="showDecorations">
      <!-- 现代简约风格装饰 -->
      <template v-if="currentTheme === 'modern'">
        <div class="modern-shape modern-shape-1"></div>
        <div class="modern-shape modern-shape-2"></div>
        <div class="modern-shape modern-shape-3"></div>
      </template>

      <!-- 便利店主题装饰 -->
      <template v-else-if="currentTheme === 'convenience'">
        <div class="store-icon-1"></div>
        <div class="store-icon-2"></div>
        <div class="store-icon-3"></div>
        <div class="store-warm-block store-warm-block-1"></div>
        <div class="store-warm-block store-warm-block-2"></div>
        <div class="store-warm-block store-warm-block-3"></div>
        <div class="store-glow-effect"></div>
      </template>

      <!-- 科技智能风格装饰 -->
      <template v-else-if="currentTheme === 'tech'">
        <div class="tech-shape tech-shape-1"></div>
        <div class="tech-shape tech-shape-2"></div>
        <div class="tech-shape tech-shape-3"></div>
        <div class="tech-digital-rain">
          <div class="tech-rain-column" v-for="i in 6" :key="i"></div>
        </div>
        <div class="tech-particles">
          <div class="tech-particle" v-for="i in 5" :key="i"></div>
        </div>
        <div class="tech-connections">
          <div class="tech-connection-line" v-for="i in 3" :key="i"></div>
        </div>
      </template>
    </div>

    <!-- 性能监控面板 (开发环境) -->
    <div v-if="showPerformanceMonitor && isDevelopment" class="performance-monitor">
      <div>性能级别: {{ performanceLevel }}</div>
      <div>主题: {{ currentTheme }}</div>
      <div>动画质量: {{ animationQuality }}</div>
      <div>设备类型: {{ deviceType }}</div>
    </div>

    <!-- 主题切换按钮 -->
    <button
      v-if="showThemeToggle"
      @click="toggleTheme"
      class="theme-toggle"
      :title="isDarkTheme ? '切换到浅色主题' : '切换到深色主题'"
    >
      <svg v-if="isDarkTheme" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58c-.39-.39-1.03-.39-1.41 0-.39.39-.39 1.03 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37c-.39-.39-1.03-.39-1.41 0-.39.39-.39 1.03 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0 .39-.39.39-1.03 0-1.41l-1.06-1.06zm1.06-10.96c.39-.39.39-1.03 0-1.41-.39-.39-1.03-.39-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36c.39-.39.39-1.03 0-1.41-.39-.39-1.03-.39-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06z"/>
      </svg>
      <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path d="M9 2c-1.05 0-2.05.16-3 .46 4.06 1.27 7 5.06 7 9.54 0 4.48-2.94 8.27-7 9.54.95.3 1.95.46 3 .46 5.52 0 10-4.48 10-10S14.52 2 9 2z"/>
      </svg>
    </button>

    <!-- 背景内容插槽 -->
    <div class="bg-content" :class="{ 'theme-switching': isThemeSwitching }">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

interface BackgroundSystemProps {
  theme?: 'modern' | 'convenience' | 'tech' | 'auto'
  showDecorations?: boolean
  showThemeToggle?: boolean
  showPerformanceMonitor?: boolean
  performanceLevel?: 'auto' | 'high' | 'medium' | 'low' | 'minimal'
  enableDarkMode?: boolean
  autoDetectTheme?: boolean
}

const props = withDefaults(defineProps<BackgroundSystemProps>(), {
  theme: 'auto',
  showDecorations: true,
  showThemeToggle: true,
  showPerformanceMonitor: false,
  performanceLevel: 'auto',
  enableDarkMode: true,
  autoDetectTheme: true
})

const emit = defineEmits<{
  themeChanged: [theme: string]
  performanceChanged: [level: string]
  darkModeChanged: [isDark: boolean]
}>()

// 响应式状态
const backgroundRef = ref<HTMLElement>()
const currentTheme = ref<string>(props.theme)
const isDarkTheme = ref<boolean>(false)
const performanceLevel = ref<string>(props.performanceLevel)
const deviceType = ref<string>('unknown')
const isThemeSwitching = ref<boolean>(false)
const isDevelopment = ref<boolean>(import.meta.env.DEV)

// 性能检测相关
const animationQuality = ref<string>('high')
const loadingState = ref<'loading' | 'loaded'>('loading')

// 计算属性
const backgroundClasses = computed(() => [
  'responsive-background',
  'performance-optimized',
  `performance-${performanceLevel.value}`,
  `theme-${currentTheme.value}`,
  {
    'dark-theme': isDarkTheme.value,
    'light-theme': !isDarkTheme.value,
    'lazy-background': loadingState.value === 'loading',
    'loaded': loadingState.value === 'loaded',
    'theme-switching': isThemeSwitching.value
  }
])

const backgroundStyles = computed(() => ({
  '--performance-level': performanceLevel.value,
  '--animation-quality': animationQuality.value,
  '--current-theme': currentTheme.value,
  '--device-type': deviceType.value
}))

// 检测设备类型
const detectDeviceType = () => {
  const width = window.innerWidth
  if (width < 576) {
    deviceType.value = 'mobile'
  } else if (width < 768) {
    deviceType.value = 'tablet'
  } else if (width < 1200) {
    deviceType.value = 'desktop'
  } else {
    deviceType.value = 'large-desktop'
  }
}

// 检测性能级别
const detectPerformanceLevel = () => {
  if (props.performanceLevel !== 'auto') {
    performanceLevel.value = props.performanceLevel
    return
  }

  // 检测设备性能
  const memory = (navigator as any).deviceMemory || 4
  const connection = (navigator as any).connection
  const cores = navigator.hardwareConcurrency || 4

  // 检测用户偏好
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const prefersReducedData = window.matchMedia('(prefers-reduced-data: reduce)').matches

  // 综合评估性能级别
  if (prefersReducedMotion || prefersReducedData || memory < 2 || cores < 4) {
    performanceLevel.value = 'low'
    animationQuality.value = 'low'
  } else if (memory < 4 || cores < 8 || (connection && connection.effectiveType === 'slow-2g')) {
    performanceLevel.value = 'medium'
    animationQuality.value = 'medium'
  } else if (memory >= 8 && cores >= 8) {
    performanceLevel.value = 'high'
    animationQuality.value = 'high'
  } else {
    performanceLevel.value = 'medium'
    animationQuality.value = 'medium'
  }

  emit('performanceChanged', performanceLevel.value)
}

// 自动选择主题
const autoSelectTheme = () => {
  if (props.theme === 'auto') {
    // 根据用户角色或路径自动选择主题
    const path = window.location.pathname

    if (path.includes('/admin')) {
      currentTheme.value = 'tech' // 管理端使用科技风格
    } else if (path.includes('/member') || path.includes('/login')) {
      currentTheme.value = 'convenience' // 会员端使用便利店主题
    } else {
      currentTheme.value = 'modern' // 默认使用现代简约风格
    }
  } else {
    currentTheme.value = props.theme
  }

  emit('themeChanged', currentTheme.value)
}

// 检测系统主题
const detectSystemTheme = () => {
  if (!props.autoDetectTheme) return

  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  isDarkTheme.value = prefersDark
  emit('darkModeChanged', prefersDark)
}

// 切换主题
const toggleTheme = async () => {
  if (!props.enableDarkMode) return

  isThemeSwitching.value = true

  // 添加切换动画
  await nextTick()

  setTimeout(() => {
    isDarkTheme.value = !isDarkTheme.value
    emit('darkModeChanged', isDarkTheme.value)

    // 保存用户偏好
    localStorage.setItem('preferred-theme', isDarkTheme.value ? 'dark' : 'light')

    setTimeout(() => {
      isThemeSwitching.value = false
    }, 600)
  }, 50)
}

// 加载用户偏好设置
const loadUserPreferences = () => {
  // 加载主题偏好
  const savedTheme = localStorage.getItem('preferred-theme')
  if (savedTheme && props.enableDarkMode) {
    isDarkTheme.value = savedTheme === 'dark'
  } else {
    detectSystemTheme()
  }

  // 加载主题风格偏好
  const savedThemeStyle = localStorage.getItem('preferred-theme-style')
  if (savedThemeStyle && props.theme === 'auto') {
    currentTheme.value = savedThemeStyle
  }
}

// 渐进式加载
const progressiveLoad = async () => {
  const element = backgroundRef.value
  if (!element) return

  // 第一阶段：基础背景
  loadingState.value = 'loading'
  element.classList.add('lazy-background')

  // 第二阶段：简单纹理 (200ms后)
  setTimeout(() => {
    element.classList.add('texture-loaded')
  }, 200)

  // 第三阶段：完整效果 (500ms后)
  setTimeout(() => {
    element.classList.add('loaded')
    element.classList.remove('lazy-background')
    loadingState.value = 'loaded'
  }, 500)
}

// 监听窗口变化
const handleResize = () => {
  detectDeviceType()
  detectPerformanceLevel()
}

// 监听主题偏好变化
const handleThemeChange = (e: MediaQueryListEvent) => {
  if (props.autoDetectTheme) {
    isDarkTheme.value = e.matches
    emit('darkModeChanged', e.matches)
  }
}

// 监听网络状态变化
const handleNetworkChange = () => {
  detectPerformanceLevel()
}

// 生命周期
onMounted(() => {
  // 加载用户偏好
  loadUserPreferences()

  // 自动选择主题
  autoSelectTheme()

  // 检测设备和性能
  detectDeviceType()
  detectPerformanceLevel()

  // 开始渐进式加载
  progressiveLoad()

  // 添加事件监听器
  window.addEventListener('resize', handleResize)
  window.addEventListener('online', handleNetworkChange)
  window.addEventListener('offline', handleNetworkChange)

  // 监听系统主题变化
  const themeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  themeMediaQuery.addEventListener('change', handleThemeChange)

  // 监听网络状态
  if ('connection' in navigator) {
    const connection = (navigator as any).connection
    connection.addEventListener('change', handleNetworkChange)
  }
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('online', handleNetworkChange)
  window.removeEventListener('offline', handleNetworkChange)

  const themeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  themeMediaQuery.removeEventListener('change', handleThemeChange)

  if ('connection' in navigator) {
    const connection = (navigator as any).connection
    connection.removeEventListener('change', handleNetworkChange)
  }
})

// 监听props变化
watch(() => props.theme, (newTheme) => {
  if (newTheme !== 'auto') {
    currentTheme.value = newTheme
    localStorage.setItem('preferred-theme-style', newTheme)
  } else {
    autoSelectTheme()
  }
})

watch(() => props.performanceLevel, (newLevel) => {
  if (newLevel !== 'auto') {
    performanceLevel.value = newLevel
  } else {
    detectPerformanceLevel()
  }
})

// 暴露方法给父组件
defineExpose({
  toggleTheme,
  detectPerformanceLevel,
  autoSelectTheme,
  currentTheme: readonly(currentTheme),
  isDarkTheme: readonly(isDarkTheme),
  performanceLevel: readonly(performanceLevel),
  deviceType: readonly(deviceType)
})
</script>

<style scoped>
/* 引入所有背景样式 */
@import '@/styles/backgrounds/modern-minimal.css';
@import '@/styles/backgrounds/convenience-store.css';
@import '@/styles/backgrounds/tech-smart.css';
@import '@/styles/backgrounds/responsive-system.css';
@import '@/styles/backgrounds/performance-optimized.css';
@import '@/styles/backgrounds/theme-system.css';

/* 背景系统基础样式 */
.background-system {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  contain: layout style paint;
}

.bg-content {
  position: relative;
  z-index: 10;
  min-height: 100vh;
}

/* 性能监控面板样式 */
.performance-monitor {
  position: fixed;
  top: 60px;
  right: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 12px;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  line-height: 1.6;
  z-index: 1000;
  backdrop-filter: blur(10px);
  min-width: 200px;
}

.performance-monitor div {
  margin-bottom: 4px;
}

.performance-monitor div:last-child {
  margin-bottom: 0;
}

/* 主题切换按钮动画 */
.theme-toggle {
  z-index: 1001;
}

/* 装饰元素默认隐藏，根据主题显示 */
.bg-decoration > * {
  display: none;
}

.theme-modern .bg-decoration .modern-shape {
  display: block;
}

.theme-convenience .bg-decoration .store-icon-1,
.theme-convenience .bg-decoration .store-icon-2,
.theme-convenience .bg-decoration .store-icon-3,
.theme-convenience .bg-decoration .store-warm-block,
.theme-convenience .bg-decoration .store-glow-effect {
  display: block;
}

.theme-tech .bg-decoration .tech-shape,
.theme-tech .bg-decoration .tech-digital-rain,
.theme-tech .bg-decoration .tech-particles,
.theme-tech .bg-decoration .tech-connections {
  display: block;
}

/* 低性能模式下隐藏复杂装饰 */
.performance-low .bg-decoration .store-warm-block,
.performance-low .bg-decoration .tech-digital-rain,
.performance-low .bg-decoration .tech-particles,
.performance-low .bg-decoration .tech-connections {
  display: none;
}

.performance-minimal .bg-decoration {
  display: none;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .performance-monitor {
    font-size: 10px;
    padding: 8px;
    min-width: 160px;
  }

  .bg-decoration .tech-digital-rain,
  .bg-decoration .tech-particles,
  .bg-decoration .tech-connections {
    display: none;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .bg-decoration * {
    animation: none !important;
  }

  .theme-switching::before {
    animation: none !important;
  }
}
</style>