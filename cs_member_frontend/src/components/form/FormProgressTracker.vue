<template>
  <div class="form-progress-tracker" :class="trackerClasses">
    <!-- 进度头部 -->
    <div class="progress-header">
      <div class="progress-title">
        <el-icon><TrendCharts /></el-icon>
        <span>{{ title }}</span>
      </div>
      <div class="progress-percentage" :class="progressStatusClass">
        {{ Math.round(progressPercentage) }}%
      </div>
    </div>

    <!-- 总体进度条 -->
    <div class="overall-progress">
      <div class="progress-bar">
        <div
          class="progress-fill"
          :style="{ width: progressPercentage + '%' }"
          :class="progressStatusClass"
        >
          <div class="progress-glow"></div>
        </div>
      </div>
      <div class="progress-steps">
        <div
          v-for="(step, index) in steps"
          :key="index"
          class="progress-step"
          :class="{
            'completed': step.completed,
            'current': step.current,
            'disabled': !step.enabled
          }"
          :style="{ left: (index / (steps.length - 1)) * 100 + '%' }"
          @click="onStepClick(index)"
        >
          <div class="step-dot">
            <el-icon v-if="step.completed"><Check /></el-icon>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="step-tooltip">
            {{ step.label }}
            <div v-if="step.description" class="step-description">{{ step.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细进度信息 -->
    <div class="progress-details">
      <!-- 完成统计 -->
      <div class="completion-stats">
        <div class="stat-item">
          <div class="stat-value">{{ completedSteps }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ totalSteps }}</div>
          <div class="stat-label">总步骤</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ estimatedTime }}</div>
          <div class="stat-label">预计剩余</div>
        </div>
      </div>

      <!-- 字段完成详情 -->
      <div class="field-progress" v-if="showFieldDetails">
        <div class="field-progress-header">
          <span>字段完成情况</span>
          <el-button
            text
            size="small"
            @click="toggleFieldDetails"
            class="toggle-btn"
          >
            {{ showFieldList ? '收起' : '展开' }}
            <el-icon :class="{ 'rotated': showFieldList }">
              <ArrowDown />
            </el-icon>
          </el-button>
        </div>

        <transition name="field-list">
          <div v-if="showFieldList" class="field-list">
            <div
              v-for="(field, index) in fieldProgress"
              :key="index"
              class="field-item"
              :class="{
                'completed': field.completed,
                'current': field.current,
                'error': field.hasError
              }"
            >
              <div class="field-icon">
                <el-icon v-if="field.completed"><CircleCheck /></el-icon>
                <el-icon v-else-if="field.hasError"><CircleClose /></el-icon>
                <el-icon v-else-if="field.current"><Loading /></el-icon>
                <el-icon v-else><Clock /></el-icon>
              </div>
              <div class="field-info">
                <div class="field-name">{{ field.label }}</div>
                <div class="field-status">{{ field.status }}</div>
              </div>
              <div class="field-progress-bar">
                <div
                  class="field-progress-fill"
                  :style="{ width: field.progress + '%' }"
                  :class="{
                    'success': field.completed,
                    'error': field.hasError,
                    'current': field.current
                  }"
                ></div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 用户行为分析 -->
      <div class="behavior-analytics" v-if="showAnalytics">
        <div class="analytics-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>使用分析</span>
        </div>
        <div class="analytics-grid">
          <div class="analytics-item">
            <div class="analytics-value">{{ formatTime(analytics.totalTime) }}</div>
            <div class="analytics-label">总用时</div>
          </div>
          <div class="analytics-item">
            <div class="analytics-value">{{ analytics.corrections }}</div>
            <div class="analytics-label">修正次数</div>
          </div>
          <div class="analytics-item">
            <div class="analytics-value">{{ analytics.hintsUsed }}</div>
            <div class="analytics-label">提示使用</div>
          </div>
          <div class="analytics-item">
            <div class="analytics-value">{{ analytics.efficiency }}%</div>
            <div class="analytics-label">完成效率</div>
          </div>
        </div>

        <!-- 建议卡片 -->
        <div v-if="suggestions.length > 0" class="suggestions-card">
          <div class="suggestions-header">
            <el-icon><Lightbulb /></el-icon>
            <span>智能建议</span>
          </div>
          <div class="suggestions-list">
            <div
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-item"
              :class="suggestion.type"
            >
              <el-icon>
                <InfoFilled v-if="suggestion.type === 'info'" />
                <Warning v-else-if="suggestion.type === 'warning'" />
                <SuccessFilled v-else />
              </el-icon>
              <span>{{ suggestion.message }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-button
        v-for="action in quickActions"
        :key="action.key"
        :type="action.type"
        :icon="action.icon"
        size="small"
        @click="action.handler"
        :disabled="action.disabled"
      >
        {{ action.label }}
      </el-button>
    </div>

    <!-- 完成庆祝动画 -->
    <transition name="celebration">
      <div v-if="showCelebration" class="celebration-overlay">
        <div class="celebration-content">
          <el-icon class="celebration-icon"><Trophy /></el-icon>
          <div class="celebration-title">表单完成！</div>
          <div class="celebration-message">恭喜您完成所有必填项</div>
          <div class="celebration-stats">
            <div class="celebration-stat">
              <span class="stat-number">{{ formatTime(analytics.totalTime) }}</span>
              <span class="stat-text">总用时</span>
            </div>
            <div class="celebration-stat">
              <span class="stat-number">{{ analytics.efficiency }}%</span>
              <span class="stat-text">效率评分</span>
            </div>
          </div>
          <el-button type="primary" @click="hideCelebration">继续</el-button>
        </div>
        <div class="confetti"></div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import {
  TrendCharts,
  Check,
  ArrowDown,
  CircleCheck,
  CircleClose,
  Loading,
  Clock,
  DataAnalysis,
  Lightbulb,
  InfoFilled,
  Warning,
  SuccessFilled,
  Trophy
} from '@element-plus/icons-vue'

// Props接口
interface StepConfig {
  label: string
  description?: string
  completed: boolean
  current: boolean
  enabled: boolean
  fields?: string[]
}

interface FieldProgress {
  name: string
  label: string
  completed: boolean
  current: boolean
  hasError: boolean
  progress: number
  status: string
}

interface QuickAction {
  key: string
  label: string
  type: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  icon: any
  handler: () => void
  disabled?: boolean
}

interface AnalyticsData {
  totalTime: number
  corrections: number
  hintsUsed: number
  efficiency: number
  fieldTimes: Record<string, number>
  errorCounts: Record<string, number>
}

interface Suggestion {
  type: 'info' | 'warning' | 'success'
  message: string
}

interface Props {
  title: string
  steps: StepConfig[]
  fieldProgress: FieldProgress[]
  analytics: AnalyticsData
  suggestions: Suggestion[]
  quickActions: QuickAction[]
  showFieldDetails?: boolean
  showAnalytics?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showFieldDetails: true,
  showAnalytics: true
})

// Emits
const emit = defineEmits<{
  stepClick: [stepIndex: number]
  quickAction: [action: QuickAction]
  celebrationComplete: []
}>()

// 响应式数据
const showFieldList = ref(false)
const showCelebration = ref(false)
const startTime = ref(Date.now())
const progressHistory = ref<Array<{ timestamp: number; percentage: number }>>([])

// 计算属性
const trackerClasses = computed(() => ({
  'tracker-completed': isCompleted.value,
  'tracker-in-progress': isInProgress.value,
  'show-celebration': showCelebration.value
}))

const totalSteps = computed(() => props.steps.length)
const completedSteps = computed(() => props.steps.filter(step => step.completed).length)
const progressPercentage = computed(() => {
  if (totalSteps.value === 0) return 0
  return (completedSteps.value / totalSteps.value) * 100
})

const progressStatusClass = computed(() => {
  const percentage = progressPercentage.value
  if (percentage === 100) return 'progress-complete'
  if (percentage >= 70) return 'progress-good'
  if (percentage >= 40) return 'progress-medium'
  return 'progress-low'
})

const isCompleted = computed(() => progressPercentage.value === 100)
const isInProgress = computed(() => progressPercentage.value > 0 && progressPercentage.value < 100)

const estimatedTime = computed(() => {
  if (!isInProgress.value) return '0分'

  const avgTimePerStep = props.analytics.totalTime / Math.max(completedSteps.value, 1)
  const remainingSteps = totalSteps.value - completedSteps.value
  const estimatedMs = avgTimePerStep * remainingSteps

  return formatTime(estimatedMs)
})

// 方法
const onStepClick = (stepIndex: number) => {
  const step = props.steps[stepIndex]
  if (step.enabled && !step.completed) {
    emit('stepClick', stepIndex)
  }
}

const toggleFieldDetails = () => {
  showFieldList.value = !showFieldList.value
}

const formatTime = (ms: number): string => {
  if (ms < 1000) return '0分'

  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)

  if (hours > 0) {
    return `${hours}小时${minutes % 60}分`
  } else if (minutes > 0) {
    return `${minutes}分${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

const showCelebrationAnimation = () => {
  if (isCompleted.value && !showCelebration.value) {
    setTimeout(() => {
      showCelebration.value = true
      createConfetti()
    }, 500)
  }
}

const hideCelebration = () => {
  showCelebration.value = false
  emit('celebrationComplete')
}

const createConfetti = () => {
  // 这里可以实现更复杂的彩纸动画效果
  const confettiContainer = document.querySelector('.confetti')
  if (confettiContainer) {
    for (let i = 0; i < 50; i++) {
      setTimeout(() => {
        const particle = document.createElement('div')
        particle.className = 'confetti-piece'
        particle.style.left = Math.random() * 100 + '%'
        particle.style.backgroundColor = ['#ff6b6b', '#4ecdc4', '#45b7d1', '#f9ca24', '#6c5ce7'][Math.floor(Math.random() * 5)]
        particle.style.animationDelay = Math.random() * 2 + 's'
        confettiContainer.appendChild(particle)

        setTimeout(() => {
          if (particle.parentNode) {
            particle.parentNode.removeChild(particle)
          }
        }, 3000)
      }, Math.random() * 1000)
    }
  }
}

const recordProgress = () => {
  progressHistory.value.push({
    timestamp: Date.now(),
    percentage: progressPercentage.value
  })

  // 保持最近100个记录
  if (progressHistory.value.length > 100) {
    progressHistory.value = progressHistory.value.slice(-100)
  }
}

// 监听进度变化
watch(() => progressPercentage.value, (newPercentage, oldPercentage) => {
  recordProgress()

  // 完成时显示庆祝动画
  if (newPercentage === 100 && oldPercentage < 100) {
    showCelebrationAnimation()
  }
}, { immediate: true })

// 监听快捷操作
const handleQuickAction = (action: QuickAction) => {
  action.handler()
  emit('quickAction', action)
}

// 定期更新进度
let progressTimer: NodeJS.Timeout
onMounted(() => {
  recordProgress()

  // 每5秒记录一次进度
  progressTimer = setInterval(() => {
    recordProgress()
  }, 5000)
})

onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
})

// 暴露方法
defineExpose({
  showCelebration: showCelebrationAnimation,
  hideCelebration,
  getProgressHistory: () => progressHistory.value,
  getCurrentProgress: () => progressPercentage.value
})
</script>

<style scoped>
.form-progress-tracker {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.tracker-completed {
  border-color: #10b981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.1);
}

/* 进度头部 */
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.progress-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.progress-title .el-icon {
  color: #3b82f6;
  font-size: 18px;
}

.progress-percentage {
  font-size: 20px;
  font-weight: 700;
  transition: color 0.3s ease;
}

.progress-percentage.progress-complete {
  color: #10b981;
}

.progress-percentage.progress-good {
  color: #059669;
}

.progress-percentage.progress-medium {
  color: #f59e0b;
}

.progress-percentage.progress-low {
  color: #ef4444;
}

/* 总体进度条 */
.overall-progress {
  position: relative;
  margin-bottom: 20px;
}

.progress-bar {
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-radius: 4px;
}

.progress-fill.progress-complete {
  background: linear-gradient(90deg, #10b981, #059669);
}

.progress-fill.progress-good {
  background: linear-gradient(90deg, #059669, #047857);
}

.progress-fill.progress-medium {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.progress-fill.progress-low {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

.progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progressGlow 2s ease-in-out infinite;
}

@keyframes progressGlow {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

/* 进度步骤 */
.progress-steps {
  position: relative;
  margin-top: 8px;
}

.progress-step {
  position: absolute;
  transform: translateX(-50%);
  cursor: pointer;
  transition: all 0.3s ease;
}

.progress-step.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.step-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #e5e7eb;
  border: 2px solid #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.progress-step.completed .step-dot {
  background-color: #10b981;
  color: white;
  transform: scale(1.1);
}

.progress-step.current .step-dot {
  background-color: #3b82f6;
  color: white;
  animation: currentStepPulse 2s ease-in-out infinite;
}

@keyframes currentStepPulse {
  0%, 100% { box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }
  50% { box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4); }
}

.step-tooltip {
  position: absolute;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #1f2937;
  color: white;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
  z-index: 10;
}

.progress-step:hover .step-tooltip {
  opacity: 1;
}

.step-description {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 2px;
}

/* 完成统计 */
.completion-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background-color: #e5e7eb;
}

/* 字段完成详情 */
.field-progress {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.field-progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.field-progress-header span {
  font-weight: 500;
  color: #374151;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
}

.toggle-btn .el-icon {
  transition: transform 0.3s ease;
}

.toggle-btn .el-icon.rotated {
  transform: rotate(180deg);
}

.field-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background-color: #ffffff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

.field-item.completed {
  border-color: #10b981;
  background-color: #f0fdf4;
}

.field-item.current {
  border-color: #3b82f6;
  background-color: #eff6ff;
}

.field-item.error {
  border-color: #ef4444;
  background-color: #fef2f2;
}

.field-icon {
  color: #6b7280;
  font-size: 16px;
  flex-shrink: 0;
}

.field-item.completed .field-icon {
  color: #10b981;
}

.field-item.current .field-icon {
  color: #3b82f6;
}

.field-item.error .field-icon {
  color: #ef4444;
}

.field-info {
  flex: 1;
}

.field-name {
  font-weight: 500;
  color: #374151;
  font-size: 13px;
}

.field-status {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
}

.field-progress-bar {
  width: 60px;
  height: 4px;
  background-color: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.field-progress-fill {
  height: 100%;
  transition: width 0.3s ease;
  background-color: #6b7280;
}

.field-progress-fill.success {
  background-color: #10b981;
}

.field-progress-fill.error {
  background-color: #ef4444;
}

.field-progress-fill.current {
  background-color: #3b82f6;
  animation: fieldProgressPulse 2s ease-in-out infinite;
}

@keyframes fieldProgressPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* 用户行为分析 */
.behavior-analytics {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.analytics-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 500;
  color: #374151;
}

.analytics-header .el-icon {
  color: #8b5cf6;
  font-size: 16px;
}

.analytics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.analytics-item {
  text-align: center;
  padding: 8px;
  background-color: #ffffff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.analytics-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.analytics-label {
  font-size: 11px;
  color: #6b7280;
  margin-top: 4px;
}

/* 建议卡片 */
.suggestions-card {
  background: linear-gradient(135deg, #fef3c7 0%, #fef9c3 100%);
  border: 1px solid #fcd34d;
  border-radius: 8px;
  padding: 12px;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-weight: 500;
  color: #92400e;
  font-size: 12px;
}

.suggestions-header .el-icon {
  color: #f59e0b;
  font-size: 14px;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 11px;
  line-height: 1.4;
}

.suggestion-item.info {
  color: #1e40af;
}

.suggestion-item.warning {
  color: #92400e;
}

.suggestion-item.success {
  color: #166534;
}

.suggestion-item .el-icon {
  font-size: 12px;
  flex-shrink: 0;
  margin-top: 1px;
}

.suggestion-item.info .el-icon {
  color: #3b82f6;
}

.suggestion-item.warning .el-icon {
  color: #f59e0b;
}

.suggestion-item.success .el-icon {
  color: #10b981;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

/* 完成庆祝动画 */
.celebration-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.celebration-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  max-width: 400px;
  position: relative;
  z-index: 2;
  animation: celebrationScale 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes celebrationScale {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.celebration-icon {
  font-size: 64px;
  color: #f59e0b;
  margin-bottom: 16px;
  animation: celebrationBounce 1s ease-in-out infinite;
}

@keyframes celebrationBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.celebration-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.celebration-message {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 20px;
}

.celebration-stats {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 24px;
}

.celebration-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #3b82f6;
}

.stat-text {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.confetti {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.confetti-piece {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: confettiFall 3s ease-in-out infinite;
}

@keyframes confettiFall {
  0% {
    transform: translateY(-100vh) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(720deg);
    opacity: 0;
  }
}

/* 动画效果 */
.celebration-enter-active,
.celebration-leave-active {
  transition: all 0.3s ease;
}

.celebration-enter-from {
  opacity: 0;
}

.celebration-leave-to {
  opacity: 0;
}

.field-list-enter-active,
.field-list-leave-active {
  transition: all 0.3s ease;
}

.field-list-enter-from,
.field-list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-progress-tracker {
    padding: 16px;
  }

  .progress-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .progress-title {
    font-size: 14px;
  }

  .progress-percentage {
    font-size: 18px;
  }

  .completion-stats {
    flex-direction: column;
    gap: 12px;
  }

  .stat-divider {
    width: 60%;
    height: 1px;
  }

  .analytics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .celebration-content {
    padding: 24px;
    margin: 16px;
  }

  .celebration-stats {
    flex-direction: column;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .analytics-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    flex-direction: column;
  }

  .step-tooltip {
    display: none;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .form-progress-tracker {
    background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
    border-color: #374151;
  }

  .progress-title {
    color: #e5e7eb;
  }

  .progress-bar {
    background-color: #374151;
  }

  .step-dot {
    background-color: #4b5563;
    border-color: #1f2937;
    color: #9ca3af;
  }

  .completion-stats,
  .field-progress,
  .behavior-analytics {
    background: linear-gradient(135deg, #111827 0%, #1f2937 100%);
    border-color: #374151;
  }

  .stat-value,
  .analytics-value {
    color: #e5e7eb;
  }

  .field-item {
    background-color: #1f2937;
    border-color: #374151;
  }

  .field-name {
    color: #e5e7eb;
  }

  .analytics-item {
    background-color: #1f2937;
    border-color: #374151;
  }

  .celebration-content {
    background: #1f2937;
    color: #e5e7eb;
  }

  .celebration-title {
    color: #e5e7eb;
  }

  .celebration-message {
    color: #9ca3af;
  }
}

/* 高对比度模式适配 */
@media (prefers-contrast: high) {
  .progress-bar {
    border: 2px solid currentColor;
  }

  .completion-stats,
  .field-progress,
  .behavior-analytics {
    border-width: 2px;
  }
}

/* 减少动画模式适配 */
@media (prefers-reduced-motion: reduce) {
  .progress-fill,
  .step-dot,
  .field-progress-fill,
  .celebration-icon,
  .confetti-piece {
    animation: none;
  }

  .celebration-enter-active,
  .celebration-leave-active,
  .field-list-enter-active,
  .field-list-leave-active {
    transition: none;
  }
}
</style>