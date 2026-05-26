<template>
  <div class="smart-input-helper" :class="helperClasses">
    <!-- 帮助提示区域 -->
    <transition name="helper-fade">
      <div v-if="showHelper" class="helper-content">
        <!-- 动态占位符提示 -->
        <div v-if="animatedPlaceholder" class="animated-placeholder">
          <div class="placeholder-text">{{ currentPlaceholder }}</div>
          <div class="placeholder-dots">
            <span class="dot" v-for="i in 3" :key="i"></span>
          </div>
        </div>

        <!-- 输入建议 -->
        <div v-if="suggestions.length > 0" class="input-suggestions">
          <div class="suggestions-header">
            <el-icon><Lightbulb /></el-icon>
            <span>输入建议：</span>
          </div>
          <div class="suggestions-list">
            <div
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-item"
              @click="applySuggestion(suggestion)"
            >
              <el-icon><Pointer /></el-icon>
              <span>{{ suggestion.text }}</span>
            </div>
          </div>
        </div>

        <!-- 格式提示 -->
        <div v-if="formatHint" class="format-hint">
          <div class="hint-icon">
            <el-icon><InfoFilled /></el-icon>
          </div>
          <div class="hint-content">
            <div class="hint-title">{{ formatHint.title }}</div>
            <div class="hint-description">{{ formatHint.description }}</div>
            <div v-if="formatHint.example" class="hint-example">
              示例：<code>{{ formatHint.example }}</code>
            </div>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="error-message" role="alert">
          <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
          <div class="error-content">
            <strong>{{ errorMessage }}</strong>
            <div v-if="errorSuggestion" class="error-suggestion">
              {{ errorSuggestion }}
            </div>
          </div>
        </div>

        <!-- 成功提示 -->
        <div v-if="successMessage" class="success-message">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
          <span>{{ successMessage }}</span>
        </div>

        <!-- 快捷操作 -->
        <div v-if="quickActions.length > 0" class="quick-actions">
          <div class="actions-header">
            <el-icon><Operation /></el-icon>
            <span>快捷操作：</span>
          </div>
          <div class="actions-list">
            <el-button
              v-for="(action, index) in quickActions"
              :key="index"
              size="small"
              text
              :type="action.type"
              :icon="action.icon"
              @click="action.handler"
              class="quick-action-btn"
            >
              {{ action.label }}
            </el-button>
          </div>
        </div>

        <!-- 输入进度指示器 -->
        <div v-if="showProgress && progressConfig" class="input-progress">
          <div class="progress-header">
            <span>{{ progressConfig.title }}</span>
            <span class="progress-text">{{ progressPercentage }}%</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: progressPercentage + '%' }"
              :class="progressStatus"
            ></div>
          </div>
          <div v-if="progressConfig.description" class="progress-description">
            {{ progressConfig.description }}
          </div>
        </div>
      </div>
    </transition>

    <!-- 键盘提示 -->
    <transition name="keyboard-hint-fade">
      <div v-if="showKeyboardHint" class="keyboard-hint">
        <el-icon><Keyboard /></el-icon>
        <span>{{ keyboardHintText }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import {
  Lightbulb,
  Pointer,
  InfoFilled,
  CircleCloseFilled,
  CircleCheckFilled,
  Operation,
  Keyboard
} from '@element-plus/icons-vue'

// Props接口
interface Props {
  // 基础配置
  fieldType: 'phone' | 'email' | 'password' | 'verifyCode'
  inputValue: string
  isVisible?: boolean

  // 动态占位符
  animatedPlaceholder?: boolean
  placeholderTexts?: string[]

  // 格式提示
  formatHint?: {
    title: string
    description: string
    example?: string
  }

  // 消息提示
  errorMessage?: string
  errorSuggestion?: string
  successMessage?: string

  // 输入建议
  suggestions?: Array<{
    text: string
    value: string
    apply?: (value: string) => void
  }>

  // 进度指示
  showProgress?: boolean
  progressConfig?: {
    title: string
    description?: string
    calculateProgress: (value: string) => number
  }

  // 键盘提示
  showKeyboardHint?: boolean
  keyboardHintText?: string

  // 快捷操作
  quickActions?: Array<{
    label: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    icon?: any
    handler: () => void
  }>
}

const props = withDefaults(defineProps<Props>(), {
  isVisible: true,
  animatedPlaceholder: false,
  placeholderTexts: () => [],
  suggestions: () => [],
  showProgress: false,
  showKeyboardHint: false,
  quickActions: () => []
})

// Emits
const emit = defineEmits<{
  applySuggestion: [suggestion: any]
  quickAction: [action: any]
}>()

// 响应式数据
const currentPlaceholderIndex = ref(0)
const animationTimer = ref<NodeJS.Timeout>()

// 计算属性
const helperClasses = computed(() => ({
  'helper-visible': props.isVisible && props.inputValue.length > 0,
  'helper-with-error': !!props.errorMessage,
  'helper-with-success': !!props.successMessage,
  [`field-type-${props.fieldType}`]: true
}))

const showHelper = computed(() => {
  return props.isVisible && (
    props.errorMessage ||
    props.successMessage ||
    props.suggestions.length > 0 ||
    props.formatHint ||
    (props.showProgress && props.progressConfig) ||
    props.quickActions.length > 0 ||
    (props.animatedPlaceholder && props.placeholderTexts.length > 0)
  )
})

const currentPlaceholder = computed(() => {
  if (props.placeholderTexts.length === 0) return ''
  return props.placeholderTexts[currentPlaceholderIndex.value]
})

const progressPercentage = computed(() => {
  if (!props.progressConfig) return 0
  return Math.min(100, Math.max(0, props.progressConfig.calculateProgress(props.inputValue)))
})

const progressStatus = computed(() => {
  const percentage = progressPercentage.value
  if (percentage < 30) return 'progress-weak'
  if (percentage < 70) return 'progress-medium'
  return 'progress-strong'
})

// 动态占位符轮播
const startPlaceholderAnimation = () => {
  if (!props.animatedPlaceholder || props.placeholderTexts.length <= 1) return

  animationTimer.value = setInterval(() => {
    currentPlaceholderIndex.value = (currentPlaceholderIndex.value + 1) % props.placeholderTexts.length
  }, 3000)
}

const stopPlaceholderAnimation = () => {
  if (animationTimer.value) {
    clearInterval(animationTimer.value)
    animationTimer.value = undefined
  }
}

// 应用建议
const applySuggestion = (suggestion: any) => {
  if (suggestion.apply) {
    suggestion.apply(suggestion.value)
  }
  emit('applySuggestion', suggestion)
}

// 快捷操作处理
const handleQuickAction = (action: any) => {
  action.handler()
  emit('quickAction', action)
}

// 监听输入值变化
watch(() => props.inputValue, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    // 输入变化时可以触发一些逻辑
  }
})

// 监听可见性变化
watch(() => props.isVisible, (isVisible) => {
  if (isVisible) {
    startPlaceholderAnimation()
  } else {
    stopPlaceholderAnimation()
  }
})

// 生命周期
onMounted(() => {
  if (props.isVisible) {
    startPlaceholderAnimation()
  }
})

onUnmounted(() => {
  stopPlaceholderAnimation()
})

// 暴露方法
defineExpose({
  applySuggestion,
  handleQuickAction
})
</script>

<style scoped>
.smart-input-helper {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.5;
  transition: all 0.3s ease;
}

.helper-visible {
  opacity: 1;
  transform: translateY(0);
}

.helper-with-error {
  animation: shake 0.5s ease-in-out;
}

/* 帮助内容区域 */
.helper-content {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 动态占位符 */
.animated-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-style: italic;
  margin-bottom: 8px;
}

.placeholder-dots {
  display: flex;
  gap: 2px;
}

.dot {
  width: 4px;
  height: 4px;
  background-color: #94a3b8;
  border-radius: 50%;
  animation: dotPulse 1.5s ease-in-out infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.3s;
}

.dot:nth-child(3) {
  animation-delay: 0.6s;
}

/* 输入建议 */
.input-suggestions {
  margin-bottom: 10px;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #475569;
  font-weight: 500;
  font-size: 12px;
}

.suggestions-header .el-icon {
  color: #f59e0b;
  font-size: 14px;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 12px;
  color: #475569;
}

.suggestion-item:hover {
  background-color: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateX(2px);
}

.suggestion-item .el-icon {
  color: #64748b;
  font-size: 12px;
  flex-shrink: 0;
}

/* 格式提示 */
.format-hint {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  padding: 8px;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
}

.hint-icon {
  color: #3b82f6;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.hint-content {
  flex: 1;
}

.hint-title {
  font-weight: 500;
  color: #1e40af;
  margin-bottom: 2px;
  font-size: 12px;
}

.hint-description {
  color: #3730a3;
  font-size: 12px;
  margin-bottom: 4px;
}

.hint-example {
  font-size: 11px;
  color: #6366f1;
}

.hint-example code {
  background-color: #dbeafe;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

/* 错误提示 */
.error-message {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  padding: 8px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  color: #991b1b;
}

.error-icon {
  color: #ef4444;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.error-content strong {
  font-weight: 600;
  color: #7f1d1d;
  display: block;
  margin-bottom: 2px;
  font-size: 12px;
}

.error-suggestion {
  font-size: 12px;
  color: #b91c1c;
  font-style: italic;
}

/* 成功提示 */
.success-message {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  color: #166534;
  font-size: 12px;
}

.success-icon {
  color: #10b981;
  font-size: 16px;
  flex-shrink: 0;
}

/* 快捷操作 */
.quick-actions {
  margin-bottom: 10px;
}

.actions-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #475569;
  font-weight: 500;
  font-size: 12px;
}

.actions-header .el-icon {
  color: #8b5cf6;
  font-size: 14px;
}

.actions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-action-btn {
  font-size: 11px;
  height: 24px;
  padding: 0 8px;
  border-radius: 12px;
}

/* 输入进度指示器 */
.input-progress {
  margin-top: 8px;
  padding: 8px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #374151;
}

.progress-text {
  font-weight: 600;
  color: #1f2937;
}

.progress-bar {
  height: 6px;
  background-color: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 3px;
}

.progress-fill.progress-weak {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

.progress-fill.progress-medium {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.progress-fill.progress-strong {
  background: linear-gradient(90deg, #10b981, #059669);
}

.progress-description {
  font-size: 11px;
  color: #6b7280;
  font-style: italic;
}

/* 键盘提示 */
.keyboard-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 11px;
  color: #4b5563;
  margin-top: 4px;
}

.keyboard-hint .el-icon {
  font-size: 12px;
  color: #6b7280;
}

/* 动画效果 */
.helper-fade-enter-active,
.helper-fade-leave-active {
  transition: all 0.3s ease;
}

.helper-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.helper-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.keyboard-hint-fade-enter-active,
.keyboard-hint-fade-leave-active {
  transition: all 0.2s ease;
}

.keyboard-hint-fade-enter-from,
.keyboard-hint-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-2px); }
  20%, 40%, 60%, 80% { transform: translateX(2px); }
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* 字段类型特定样式 */
.field-type-phone .suggestions-header .el-icon,
.field-type-phone .hint-icon {
  color: #10b981;
}

.field-type-email .suggestions-header .el-icon,
.field-type-email .hint-icon {
  color: #3b82f6;
}

.field-type-password .suggestions-header .el-icon,
.field-type-password .hint-icon {
  color: #f59e0b;
}

.field-type-verifyCode .suggestions-header .el-icon,
.field-type-verifyCode .hint-icon {
  color: #8b5cf6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .helper-content {
    padding: 10px;
  }

  .suggestions-header,
  .actions-header,
  .progress-header {
    font-size: 11px;
  }

  .suggestion-item,
  .quick-action-btn,
  .progress-description,
  .keyboard-hint {
    font-size: 11px;
  }

  .format-hint,
  .error-message,
  .success-message {
    padding: 6px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .helper-content {
    background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
    border-color: #374151;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  .animated-placeholder {
    color: #9ca3af;
  }

  .suggestions-header,
  .actions-header,
  .progress-header {
    color: #d1d5db;
  }

  .suggestion-item {
    background-color: #374151;
    border-color: #4b5563;
    color: #d1d5db;
  }

  .suggestion-item:hover {
    background-color: #4b5563;
    border-color: #6b7280;
  }

  .format-hint {
    background-color: #1e3a8a;
    border-color: #3730a3;
  }

  .hint-title {
    color: #dbeafe;
  }

  .hint-description {
    color: #bfdbfe;
  }

  .hint-example {
    color: #93c5fd;
  }

  .hint-example code {
    background-color: #1e40af;
  }

  .error-message {
    background-color: #450a0a;
    border-color: #991b1b;
  }

  .error-content strong {
    color: #fca5a5;
  }

  .error-suggestion {
    color: #fca5a5;
  }

  .success-message {
    background-color: #052e16;
    border-color: #166534;
  }

  .input-progress {
    background-color: #374151;
    border-color: #4b5563;
  }

  .progress-header,
  .keyboard-hint {
    color: #d1d5db;
  }

  .keyboard-hint {
    background-color: #374151;
    border-color: #4b5563;
  }
}

/* 高对比度模式适配 */
@media (prefers-contrast: high) {
  .helper-content,
  .format-hint,
  .error-message,
  .success-message,
  .input-progress,
  .keyboard-hint {
    border-width: 2px;
  }

  .suggestion-item {
    border-width: 2px;
  }

  .progress-bar {
    border: 1px solid currentColor;
  }
}

/* 减少动画模式适配 */
@media (prefers-reduced-motion: reduce) {
  .helper-fade-enter-active,
  .helper-fade-leave-active,
  .keyboard-hint-fade-enter-active,
  .keyboard-hint-fade-leave-active {
    transition: none;
  }

  .helper-fade-enter-from,
  .helper-fade-leave-to,
  .keyboard-hint-fade-enter-from,
  .keyboard-hint-leave-to {
    transform: none;
  }

  .helper-with-error {
    animation: none;
  }

  .dot {
    animation: none;
  }
}
</style>