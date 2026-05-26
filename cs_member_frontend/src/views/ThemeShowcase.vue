<template>
  <BackgroundSystem
    theme="auto"
    :showDecorations="true"
    :showThemeToggle="true"
    :showPerformanceMonitor="isDevelopment"
    :performanceLevel="'auto'"
    :enableDarkMode="true"
    :autoDetectTheme="true"
  >
    <div class="theme-showcase">
      <!-- 顶部导航 -->
      <nav class="showcase-nav">
        <div class="nav-container">
          <h1 class="nav-title">背景主题展示</h1>
          <div class="nav-controls">
            <button
              @click="togglePerformanceMonitor"
              class="control-btn"
              :class="{ active: showMonitor }"
            >
              性能监控
            </button>
            <button
              @click="switchThemeMode"
              class="control-btn"
            >
              切换主题: {{ currentMode }}
            </button>
          </div>
        </div>
      </nav>

      <!-- 主要内容区域 -->
      <main class="showcase-main">
        <!-- 主题选择器 -->
        <section class="theme-selector">
          <h2 class="section-title">选择主题风格</h2>
          <div class="theme-grid">
            <div
              v-for="theme in availableThemes"
              :key="theme.id"
              class="theme-card"
              :class="{ active: selectedTheme === theme.id }"
              @click="selectTheme(theme.id)"
            >
              <div class="theme-preview" :class="`preview-${theme.id}`">
                <div class="preview-elements">
                  <div class="preview-shape"></div>
                  <div class="preview-shape"></div>
                  <div class="preview-shape"></div>
                </div>
              </div>
              <div class="theme-info">
                <h3 class="theme-name">{{ theme.name }}</h3>
                <p class="theme-description">{{ theme.description }}</p>
                <div class="theme-tags">
                  <span
                    v-for="tag in theme.tags"
                    :key="tag"
                    class="theme-tag"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 演示区域 -->
        <section class="demo-section">
          <h2 class="section-title">效果演示</h2>
          <div class="demo-container">
            <!-- 登录框演示 -->
            <div class="demo-item">
              <h3 class="demo-title">登录框效果</h3>
              <div class="login-demo">
                <div class="demo-login-box theme-login-box">
                  <div class="demo-login-header">
                    <div class="demo-logo">
                      <svg width="48" height="48" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                        <rect x="10" y="20" width="80" height="60" rx="5" fill="#409EFF" />
                        <rect x="20" y="30" width="25" height="40" fill="#ffffff" />
                        <rect x="55" y="30" width="25" height="40" fill="#ffffff" />
                        <rect x="15" y="10" width="70" height="10" rx="2" fill="#409EFF" />
                        <circle cx="25" cy="75" r="3" fill="#ffffff" />
                        <circle cx="75" cy="75" r="3" fill="#ffffff" />
                      </svg>
                    </div>
                    <h4 class="demo-title-text">系统登录</h4>
                  </div>
                  <div class="demo-form">
                    <div class="demo-input-group">
                      <input type="text" placeholder="请输入手机号码" class="demo-input theme-input" />
                    </div>
                    <div class="demo-input-group">
                      <input type="password" placeholder="请输入密码" class="demo-input theme-input" />
                    </div>
                    <button class="demo-button theme-button-primary">登录</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 卡片演示 -->
            <div class="demo-item">
              <h3 class="demo-title">卡片效果</h3>
              <div class="cards-demo">
                <div class="demo-card theme-card">
                  <div class="card-icon">📊</div>
                  <h4 class="card-title">数据分析</h4>
                  <p class="card-description">实时查看会员数据统计和分析报告</p>
                  <div class="card-stats">
                    <div class="stat-item">
                      <span class="stat-number">1,234</span>
                      <span class="stat-label">总会员数</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-number">89%</span>
                      <span class="stat-label">活跃率</span>
                    </div>
                  </div>
                </div>

                <div class="demo-card theme-card">
                  <div class="card-icon">🎯</div>
                  <h4 class="card-title">营销活动</h4>
                  <p class="card-description">创建和管理会员营销活动</p>
                  <div class="card-actions">
                    <button class="card-btn theme-button">创建活动</button>
                    <button class="card-btn theme-button">查看历史</button>
                  </div>
                </div>

                <div class="demo-card theme-card">
                  <div class="card-icon">💎</div>
                  <h4 class="card-title">会员等级</h4>
                  <p class="card-description">管理会员等级和权益设置</p>
                  <div class="progress-bar">
                    <div class="progress-fill" style="width: 65%"></div>
                  </div>
                  <span class="progress-text">当前进度: 65%</span>
                </div>
              </div>
            </div>

            <!-- 按钮演示 -->
            <div class="demo-item">
              <h3 class="demo-title">按钮效果</h3>
              <div class="buttons-demo">
                <button class="demo-btn theme-button-primary">主要按钮</button>
                <button class="demo-btn theme-button">次要按钮</button>
                <button class="demo-btn theme-button" disabled>禁用按钮</button>
                <button class="demo-btn theme-button-primary loading">
                  <span class="loading-spinner"></span>
                  加载中...
                </button>
              </div>
            </div>

            <!-- 表单演示 -->
            <div class="demo-item">
              <h3 class="demo-title">表单效果</h3>
              <div class="form-demo">
                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label theme-text-secondary">姓名</label>
                    <input type="text" class="form-input theme-input" placeholder="请输入姓名" />
                  </div>
                  <div class="form-group">
                    <label class="form-label theme-text-secondary">邮箱</label>
                    <input type="email" class="form-input theme-input" placeholder="请输入邮箱" />
                  </div>
                </div>
                <div class="form-group">
                  <label class="form-label theme-text-secondary">备注</label>
                  <textarea class="form-textarea theme-input" rows="3" placeholder="请输入备注信息"></textarea>
                </div>
                <div class="form-actions">
                  <button class="form-btn theme-button">取消</button>
                  <button class="form-btn theme-button-primary">保存</button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 响应式演示 -->
        <section class="responsive-demo">
          <h2 class="section-title">响应式适配</h2>
          <div class="responsive-info">
            <div class="info-item">
              <span class="info-label">当前设备类型:</span>
              <span class="info-value">{{ deviceType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">屏幕尺寸:</span>
              <span class="info-value">{{ screenSize }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性能级别:</span>
              <span class="info-value">{{ performanceLevel }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">主题模式:</span>
              <span class="info-value">{{ isDarkTheme ? '深色' : '浅色' }}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </BackgroundSystem>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import BackgroundSystem from '@/components/common/BackgroundSystem.vue'

interface Theme {
  id: string
  name: string
  description: string
  tags: string[]
}

const availableThemes: Theme[] = [
  {
    id: 'modern',
    name: '现代简约',
    description: '清爽专业的商务风格',
    tags: ['专业', '简洁', '商务']
  },
  {
    id: 'convenience',
    name: '便利店主题',
    description: '温暖亲切的便利店风格',
    tags: ['温暖', '亲切', '便利店']
  },
  {
    id: 'tech',
    name: '科技智能',
    description: '现代数字化科技风格',
    tags: ['科技', '数字', '智能']
  }
]

// 响应式状态
const selectedTheme = ref<string>('auto')
const currentMode = ref<string>('自动')
const showMonitor = ref<boolean>(false)
const isDevelopment = ref<boolean>(import.meta.env.DEV)

// 设备信息
const deviceType = ref<string>('unknown')
const screenSize = ref<string>('unknown')
const performanceLevel = ref<string>('unknown')
const isDarkTheme = ref<boolean>(false)

// 计算属性
const windowWidth = ref(window.innerWidth)
const windowHeight = ref(window.innerHeight)

// 方法
const selectTheme = (themeId: string) => {
  selectedTheme.value = themeId
  // 这里可以调用BackgroundSystem的方法切换主题
}

const switchThemeMode = () => {
  const modes = ['自动', '现代简约', '便利店主题', '科技智能']
  const currentIndex = modes.indexOf(currentMode.value)
  currentMode.value = modes[(currentIndex + 1) % modes.length]

  // 根据模式设置主题
  switch (currentMode.value) {
    case '现代简约':
      selectedTheme.value = 'modern'
      break
    case '便利店主题':
      selectedTheme.value = 'convenience'
      break
    case '科技智能':
      selectedTheme.value = 'tech'
      break
    default:
      selectedTheme.value = 'auto'
  }
}

const togglePerformanceMonitor = () => {
  showMonitor.value = !showMonitor.value
}

const updateDeviceInfo = () => {
  // 更新窗口尺寸
  windowWidth.value = window.innerWidth
  windowHeight.value = window.innerHeight
  screenSize.value = `${windowWidth.value} x ${windowHeight.value}`

  // 更新设备类型
  if (windowWidth.value < 576) {
    deviceType.value = '手机'
  } else if (windowWidth.value < 768) {
    deviceType.value = '平板竖屏'
  } else if (windowWidth.value < 992) {
    deviceType.value = '平板横屏'
  } else if (windowWidth.value < 1200) {
    deviceType.value = '桌面'
  } else {
    deviceType.value = '大屏桌面'
  }

  // 更新性能级别（模拟）
  const memory = (navigator as any).deviceMemory || 4
  const cores = navigator.hardwareConcurrency || 4

  if (memory < 2 || cores < 4) {
    performanceLevel.value = '低'
  } else if (memory < 4 || cores < 8) {
    performanceLevel.value = '中'
  } else {
    performanceLevel.value = '高'
  }

  // 更新主题模式
  isDarkTheme.value = document.documentElement.classList.contains('dark-theme')
}

// 生命周期
onMounted(() => {
  updateDeviceInfo()
  window.addEventListener('resize', updateDeviceInfo)

  // 检测系统主题
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  isDarkTheme.value = prefersDark

  // 监听主题变化
  const themeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  themeMediaQuery.addEventListener('change', (e) => {
    isDarkTheme.value = e.matches
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', updateDeviceInfo)
})
</script>

<style scoped>
/* 主题展示页面样式 */
.theme-showcase {
  min-height: 100vh;
  position: relative;
  z-index: 10;
  padding: 20px;
}

/* 导航样式 */
.showcase-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-primary);
  backdrop-filter: blur(10px);
  margin: -20px -20px 20px -20px;
  padding: 16px 20px;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.nav-controls {
  display: flex;
  gap: 12px;
}

.control-btn {
  padding: 8px 16px;
  border: 1px solid var(--border-primary);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.control-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--border-secondary);
}

.control-btn.active {
  background: var(--light-primary-500, #3b82f6);
  color: white;
  border-color: var(--light-primary-500, #3b82f6);
}

/* 主要内容区域 */
.showcase-main {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
  text-align: center;
}

/* 主题选择器 */
.theme-selector {
  margin-bottom: 48px;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.theme-card {
  background: var(--bg-primary);
  border: 2px solid var(--border-primary);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.theme-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--light-primary-500, #3b82f6);
}

.theme-card.active {
  border-color: var(--light-primary-500, #3b82f6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.theme-preview {
  height: 120px;
  position: relative;
  overflow: hidden;
  background: var(--bg-secondary);
}

.preview-modern {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.preview-convenience {
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 100%);
}

.preview-tech {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.preview-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.preview-shape {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.3);
  animation: float 3s ease-in-out infinite;
}

.preview-shape:nth-child(2) {
  animation-delay: 1s;
  background: rgba(245, 158, 11, 0.3);
}

.preview-shape:nth-child(3) {
  animation-delay: 2s;
  background: rgba(139, 92, 246, 0.3);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.theme-info {
  padding: 20px;
}

.theme-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.theme-description {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.theme-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.theme-tag {
  padding: 4px 8px;
  background: var(--bg-accent);
  color: var(--text-secondary);
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 演示区域 */
.demo-section {
  margin-bottom: 48px;
}

.demo-container {
  display: grid;
  gap: 32px;
}

.demo-item {
  background: var(--bg-primary);
  border: 1px solid var(--border-primary);
  border-radius: 16px;
  padding: 24px;
}

.demo-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
}

/* 登录框演示 */
.login-demo {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.demo-login-box {
  width: 100%;
  max-width: 360px;
  padding: 32px 24px;
  text-align: center;
}

.demo-login-header {
  margin-bottom: 24px;
}

.demo-logo {
  margin-bottom: 16px;
}

.demo-title-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.demo-form {
  text-align: left;
}

.demo-input-group {
  margin-bottom: 16px;
}

.demo-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  font-size: 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.demo-input:focus {
  outline: none;
  border-color: var(--light-primary-500, #3b82f6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.demo-button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

/* 卡片演示 */
.cards-demo {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.demo-card {
  padding: 24px;
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  background: var(--bg-primary);
  transition: all 0.3s ease;
}

.demo-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.card-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.card-description {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 16px 0;
}

.card-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.card-actions {
  display: flex;
  gap: 12px;
}

.card-btn {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid var(--border-primary);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.progress-bar {
  height: 4px;
  background: var(--bg-accent);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: var(--light-primary-500, #3b82f6);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 按钮演示 */
.buttons-demo {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.demo-btn {
  padding: 12px 24px;
  border: 1px solid var(--border-primary);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.demo-btn:hover:not(:disabled) {
  background: var(--bg-secondary);
  border-color: var(--border-secondary);
}

.demo-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.demo-btn.loading {
  position: relative;
  color: transparent;
}

.loading-spinner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

/* 表单演示 */
.form-demo {
  max-width: 600px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  font-size: 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  font-size: 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
  resize: vertical;
  min-height: 80px;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--light-primary-500, #3b82f6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.form-btn {
  padding: 10px 20px;
  border: 1px solid var(--border-primary);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 响应式演示 */
.responsive-demo {
  background: var(--bg-primary);
  border: 1px solid var(--border-primary);
  border-radius: 16px;
  padding: 24px;
}

.responsive-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 600;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .theme-showcase {
    padding: 16px;
  }

  .nav-container {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .theme-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .cards-demo {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .buttons-demo {
    flex-direction: column;
    align-items: stretch;
  }

  .responsive-info {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 576px) {
  .demo-login-box {
    padding: 24px 16px;
  }

  .demo-card {
    padding: 16px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-btn {
    width: 100%;
  }
}
</style>