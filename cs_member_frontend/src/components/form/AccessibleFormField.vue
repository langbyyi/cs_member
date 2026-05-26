<template>
  <div class="accessible-form-field" :class="fieldClasses">
    <!-- 字段标签 -->
    <label
      v-if="label"
      :for="fieldId"
      class="field-label"
      :class="{ 'required': required }"
    >
      <span class="label-text">{{ label }}</span>
      <span v-if="required" class="required-indicator" aria-label="必填项">*</span>
      <span v-if="helpText" class="help-text">{{ helpText }}</span>
    </label>

    <!-- 输入容器 -->
    <div class="input-container" :class="inputContainerClasses">
      <!-- 前缀插槽 -->
      <div v-if="$slots.prefix" class="field-prefix">
        <slot name="prefix"></slot>
      </div>

      <!-- 输入框 -->
      <component
        :is="inputComponent"
        :id="fieldId"
        ref="inputRef"
        v-model="inputValue"
        v-bind="inputProps"
        :class="inputClasses"
        :aria-label="ariaLabel"
        :aria-describedby="ariaDescribedBy"
        :aria-invalid="hasError"
        :aria-required="required"
        :aria-expanded="isExpanded"
        :aria-autocomplete="ariaAutocomplete"
        :role="inputRole"
        :tabindex="tabIndex"
        @keydown="handleKeydown"
        @keyup="handleKeyup"
        @focus="handleFocus"
        @blur="handleBlur"
        @input="handleInput"
        @change="handleChange"
      />

      <!-- 后缀插槽 -->
      <div v-if="$slots.suffix" class="field-suffix">
        <slot name="suffix"></slot>
      </div>

      <!-- 验证状态图标 -->
      <div v-if="showValidationIcon" class="validation-icon" :class="validationIconClass">
        <el-icon>
          <Loading v-if="validating" />
          <CircleCheckFilled v-else-if="isValid && inputValue" />
          <CircleCloseFilled v-else-if="hasError && inputValue" />
        </el-icon>
      </div>
    </div>

    <!-- 字段描述 -->
    <div
      v-if="description"
      :id="`${fieldId}-description`"
      class="field-description"
      role="note"
    >
      {{ description }}
    </div>

    <!-- 错误消息 -->
    <div
      v-if="hasError && errorMessage"
      :id="`${fieldId}-error`"
      class="field-error"
      role="alert"
      aria-live="polite"
    >
      <el-icon class="error-icon"><Warning /></el-icon>
      <span class="error-text">{{ errorMessage }}</span>
      <span v-if="errorSuggestion" class="error-suggestion">{{ errorSuggestion }}</span>
    </div>

    <!-- 成功消息 -->
    <div
      v-if="showSuccess && successMessage"
      :id="`${fieldId}-success`"
      class="field-success"
      role="status"
      aria-live="polite"
    >
      <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
      <span>{{ successMessage }}</span>
    </div>

    <!-- 辅助功能提示 -->
    <div
      v-if="showKeyboardHelp"
      :id="`${fieldId}-keyboard-help`"
      class="keyboard-help"
      role="tooltip"
      aria-hidden="true"
    >
      <div class="help-title">键盘快捷键：</div>
      <div class="help-list">
        <div v-for="(help, index) in keyboardHelp" :key="index" class="help-item">
          <kbd>{{ help.key }}</kbd>
          <span>{{ help.description }}</span>
        </div>
      </div>
    </div>

    <!-- 屏幕阅读器专用提示 -->
    <div class="sr-only" aria-live="polite" aria-atomic="true">
      {{ screenReaderAnnouncement }}
    </div>

    <!-- 进度指示器（适用于验证码等场景） -->
    <div
      v-if="showProgress"
      :id="`${fieldId}-progress`"
      class="field-progress"
      role="progressbar"
      :aria-valuenow="progressValue"
      :aria-valuemin="0"
      :aria-valuemax="progressMax"
      :aria-label="progressLabel"
    >
      <div class="progress-bar">
        <div
          class="progress-fill"
          :style="{ width: progressPercentage + '%' }"
          :class="progressStatus"
        ></div>
      </div>
      <div class="progress-text">{{ progressText }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import {
  Loading,
  CircleCheckFilled,
  CircleCloseFilled,
  Warning
} from '@element-plus/icons-vue'
import { generateRandomId } from '@/utils/common'

// Props接口
interface Props {
  // 基础属性
  modelValue: string | number
  fieldType: 'text' | 'email' | 'tel' | 'password' | 'number' | 'textarea'
  label?: string
  description?: string
  helpText?: string
  required?: boolean
  disabled?: boolean
  readonly?: boolean
  placeholder?: string
  maxlength?: number
  minlength?: number

  // 验证属性
  validating?: boolean
  isValid?: boolean
  errorMessage?: string
  errorSuggestion?: string
  successMessage?: string
  showValidationIcon?: boolean
  showSuccess?: boolean

  // 无障碍属性
  ariaLabel?: string
  ariaAutocomplete?: 'none' | 'inline' | 'list' | 'both'
  inputRole?: string
  tabIndex?: number

  // 特殊功能
  showKeyboardHelp?: boolean
  showProgress?: boolean
  progressValue?: number
  progressMax?: number
  progressLabel?: string

  // 样式配置
  size?: 'large' | 'default' | 'small'
  variant?: 'default' | 'filled' | 'outlined'
}

const props = withDefaults(defineProps<Props>(), {
  fieldType: 'text',
  required: false,
  disabled: false,
  readonly: false,
  validating: false,
  isValid: true,
  showValidationIcon: true,
  showSuccess: true,
  showKeyboardHelp: false,
  showProgress: false,
  progressMax: 100,
  size: 'default',
  variant: 'default'
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'focus': [event: FocusEvent]
  'blur': [event: FocusEvent]
  'input': [event: Event]
  'change': [event: Event]
  'keydown': [event: KeyboardEvent]
  'keyup': [event: KeyboardEvent]
  'validate': [value: string | number]
}>()

// 响应式数据
const inputRef = ref()
const fieldId = ref(generateRandomId('field'))
const isFocused = ref(false)
const inputValue = ref(props.modelValue)
const screenReaderAnnouncement = ref('')

// 计算属性
const fieldClasses = computed(() => ({
  'field-focused': isFocused.value,
  'field-disabled': props.disabled,
  'field-readonly': props.readonly,
  'field-invalid': hasError.value,
  'field-valid': props.isValid && inputValue.value,
  'field-required': props.required,
  [`field-size-${props.size}`]: true,
  [`field-variant-${props.variant}`]: true,
  [`field-type-${props.fieldType}`]: true
}))

const inputContainerClasses = computed(() => ({
  'input-focused': isFocused.value,
  'input-disabled': props.disabled,
  'input-readonly': props.readonly,
  'input-valid': props.isValid && inputValue.value,
  'input-invalid': hasError.value,
  'input-with-prefix': !!$slots.prefix,
  'input-with-suffix': !!$slots.suffix,
  'input-with-validation': props.showValidationIcon
}))

const inputClasses = computed(() => ({
  'input-field': true,
  'input-focused': isFocused.value,
  'input-disabled': props.disabled,
  'input-readonly': props.readonly,
  [`input-size-${props.size}`]: true
}))

const inputComponent = computed(() => {
  switch (props.fieldType) {
    case 'textarea':
      return 'textarea'
    default:
      return 'input'
  }
})

const inputProps = computed(() => {
  const baseProps: any = {
    type: props.fieldType === 'tel' ? 'tel' : props.fieldType,
    placeholder: props.placeholder,
    maxlength: props.maxlength,
    minlength: props.minlength,
    disabled: props.disabled,
    readonly: props.readonly,
    value: inputValue.value
  }

  if (props.fieldType === 'number') {
    baseProps.type = 'number'
    baseProps.inputmode = 'numeric'
    baseProps.pattern = '[0-9]*'
  }

  return baseProps
})

const hasError = computed(() => !props.isValid && !!props.errorMessage)

const validationIconClass = computed(() => ({
  'icon-validating': props.validating,
  'icon-valid': props.isValid && inputValue.value,
  'icon-invalid': hasError.value
}))

const isExpanded = computed(() => {
  // 根据字段类型返回展开状态
  return false
})

const ariaDescribedBy = computed(() => {
  const ids = []
  if (props.description) ids.push(`${fieldId.value}-description`)
  if (hasError.value && props.errorMessage) ids.push(`${fieldId.value}-error`)
  if (props.showSuccess && props.successMessage) ids.push(`${fieldId.value}-success`)
  if (props.showKeyboardHelp) ids.push(`${fieldId.value}-keyboard-help`)
  if (props.showProgress) ids.push(`${fieldId.value}-progress`)
  return ids.join(' ') || undefined
})

const keyboardHelp = computed(() => {
  switch (props.fieldType) {
    case 'tel':
      return [
        { key: 'Space', description: '格式化手机号' },
        { key: '↑↓', description: '增加/减少数字' }
      ]
    case 'email':
      return [
        { key: '@', description: '快速输入@符号' },
        { key: 'Ctrl+.', description: '常用邮箱后缀' }
      ]
    case 'password':
      return [
        { key: 'Ctrl+Shift+P', description: '显示/隐藏密码' },
        { key: 'Ctrl+G', description: '生成强密码' }
      ]
    default:
      return [
        { key: 'Enter', description: '确认输入' },
        { key: 'Escape', description: '清空内容' }
      ]
  }
})

const progressPercentage = computed(() => {
  if (!props.showProgress || props.progressMax === 0) return 0
  return Math.min(100, Math.max(0, (props.progressValue || 0) / props.progressMax * 100))
})

const progressStatus = computed(() => {
  const percentage = progressPercentage.value
  if (percentage < 30) return 'progress-weak'
  if (percentage < 70) return 'progress-medium'
  return 'progress-strong'
})

const progressText = computed(() => {
  if (!props.showProgress) return ''
  return `${props.progressValue || 0}/${props.progressMax} ${props.progressLabel || ''}`
})

// 方法
const handleKeydown = (event: KeyboardEvent) => {
  // 处理键盘快捷键
  switch (event.key) {
    case 'Escape':
      if (!props.readonly && !props.disabled && inputValue.value) {
        event.preventDefault()
        clearInput()
      }
      break
    case 'Enter':
      if (props.fieldType !== 'textarea') {
        event.preventDefault()
        validateInput()
      }
      break
    case ' ':
      if (props.fieldType === 'tel') {
        event.preventDefault()
        formatPhoneNumber()
      }
      break
  }

  emit('keydown', event)
}

const handleKeyup = (event: KeyboardEvent) => {
  // 处理特殊键盘事件
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case '.':
        if (props.fieldType === 'email') {
          event.preventDefault()
          insertEmailDomain()
        }
        break
      case 'g':
        if (event.shiftKey && props.fieldType === 'password') {
          event.preventDefault()
          generateStrongPassword()
        }
        break
    }
  }

  emit('keyup', event)
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  announceToScreenReader(`聚焦到${props.label || '输入框'}，${getInputDescription()}`)
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  validateInput()
  emit('blur', event)
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  inputValue.value = target.value
  emit('update:modelValue', target.value)
  emit('input', event)

  // 实时验证反馈
  if (inputValue.value && !props.validating) {
    delayedValidate()
  }
}

const handleChange = (event: Event) => {
  emit('change', event)
}

const clearInput = () => {
  inputValue.value = ''
  emit('update:modelValue', '')
  announceToScreenReader('已清空输入内容')
}

const formatPhoneNumber = () => {
  if (props.fieldType !== 'tel') return

  let value = inputValue.value.toString().replace(/\D/g, '')
  if (value.length > 11) value = value.slice(0, 11)

  // 格式化为 138-0013-8000
  if (value.length >= 7) {
    value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7)
  } else if (value.length >= 3) {
    value = value.slice(0, 3) + '-' + value.slice(3)
  }

  inputValue.value = value
  emit('update:modelValue', value)
  announceToScreenReader('手机号已格式化')
}

const insertEmailDomain = () => {
  if (props.fieldType !== 'email') return

  const commonDomains = ['gmail.com', '163.com', 'qq.com', '126.com']
  const currentDomain = commonDomains[Math.floor(Math.random() * commonDomains.length)]

  const currentValue = inputValue.value.toString()
  if (!currentValue.includes('@')) {
    inputValue.value = currentValue + '@' + currentDomain
    emit('update:modelValue', inputValue.value)
    announceToScreenReader(`已插入邮箱后缀：${currentDomain}`)
  }
}

const generateStrongPassword = () => {
  const length = 12
  const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*'
  let password = ''

  for (let i = 0; i < length; i++) {
    password += charset.charAt(Math.floor(Math.random() * charset.length))
  }

  inputValue.value = password
  emit('update:modelValue', password)
  announceToScreenReader('已生成强密码')
}

const validateInput = () => {
  if (!inputValue.value) return

  emit('validate', inputValue.value)
  announceToScreenReader('已验证输入内容')
}

// 延迟验证
let validateTimeout: NodeJS.Timeout
const delayedValidate = () => {
  clearTimeout(validateTimeout)
  validateTimeout = setTimeout(() => {
    validateInput()
  }, 500)
}

// 屏幕阅读器公告
const announceToScreenReader = (message: string) => {
  screenReaderAnnouncement.value = message
  setTimeout(() => {
    screenReaderAnnouncement.value = ''
  }, 1000)
}

const getInputDescription = () => {
  const descriptions = []
  if (props.required) descriptions.push('必填')
  if (props.placeholder) descriptions.push(`提示：${props.placeholder}`)
  if (props.helpText) descriptions.push(props.helpText)
  if (props.description) descriptions.push(props.description)

  return descriptions.join('，')
}

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue
})

// 监听验证状态变化
watch([() => props.isValid, () => props.errorMessage], ([, newError]) => {
  if (hasError.value) {
    announceToScreenReader(`输入有误：${newError}`)
  } else if (props.isValid && inputValue.value) {
    announceToScreenReader('输入格式正确')
  }
})

// 生命周期
onMounted(() => {
  // 初始化无障碍设置
  if (inputRef.value) {
    inputRef.value.setAttribute('autocomplete', props.ariaAutocomplete || 'off')
  }
})

onUnmounted(() => {
  clearTimeout(validateTimeout)
})

// 暴露方法
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  clear: clearInput,
  validate: validateInput,
  getValue: () => inputValue.value,
  getInputElement: () => inputRef.value
})
</script>

<style scoped>
.accessible-form-field {
  position: relative;
  width: 100%;
}

/* 字段标签 */
.field-label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
}

.label-text {
  margin-right: 4px;
}

.required-indicator {
  color: #ef4444;
  font-weight: bold;
  margin-left: 2px;
}

.help-text {
  color: #6b7280;
  font-weight: normal;
  font-size: 12px;
  margin-left: 8px;
}

/* 输入容器 */
.input-container {
  position: relative;
  display: flex;
  align-items: center;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  background-color: #ffffff;
  transition: all 0.2s ease;
  overflow: hidden;
}

.input-container:hover {
  border-color: #9ca3af;
}

.input-container:focus-within,
.input-focused {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-disabled {
  background-color: #f9fafb;
  border-color: #e5e7eb;
  cursor: not-allowed;
  opacity: 0.6;
}

.input-readonly {
  background-color: #f9fafb;
  border-color: #e5e7eb;
}

.input-valid {
  border-color: #10b981;
}

.input-invalid {
  border-color: #ef4444;
}

/* 前缀和后缀 */
.field-prefix,
.field-suffix {
  display: flex;
  align-items: center;
  padding: 0 12px;
  color: #6b7280;
  background-color: #f9fafb;
  border: none;
}

.input-with-prefix .field-prefix {
  border-right: 1px solid #e5e7eb;
}

.input-with-suffix .field-suffix {
  border-left: 1px solid #e5e7eb;
}

/* 输入框 */
.input-field {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: #111827;
  font-size: 14px;
  line-height: 1.5;
}

.input-field::placeholder {
  color: #9ca3af;
}

.input-field:disabled {
  cursor: not-allowed;
  color: #6b7280;
}

.input-size-large .input-field {
  padding: 12px 16px;
  font-size: 16px;
}

.input-size-default .input-field {
  padding: 8px 12px;
  font-size: 14px;
}

.input-size-small .input-field {
  padding: 6px 8px;
  font-size: 12px;
}

.input-field[type="password"] {
  letter-spacing: 2px;
}

/* 验证图标 */
.validation-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  margin-right: 8px;
  color: #6b7280;
  transition: color 0.2s ease;
}

.icon-validating {
  color: #3b82f6;
  animation: spin 1s linear infinite;
}

.icon-valid {
  color: #10b981;
}

.icon-invalid {
  color: #ef4444;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 字段描述和消息 */
.field-description {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.field-error {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 4px;
  padding: 8px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  color: #991b1b;
  font-size: 12px;
  line-height: 1.4;
}

.error-icon {
  color: #ef4444;
  font-size: 14px;
  margin-top: 1px;
  flex-shrink: 0;
}

.error-text {
  font-weight: 500;
  flex: 1;
}

.error-suggestion {
  color: #b91c1c;
  font-style: italic;
  display: block;
  margin-top: 2px;
}

.field-success {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  padding: 8px;
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  color: #166534;
  font-size: 12px;
  line-height: 1.4;
}

.success-icon {
  color: #10b981;
  font-size: 14px;
  flex-shrink: 0;
}

/* 键盘帮助 */
.keyboard-help {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  padding: 12px;
  background-color: #1f2937;
  color: #f3f4f6;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 50;
  font-size: 12px;
  opacity: 0;
  transform: translateY(-10px);
  transition: all 0.2s ease;
  pointer-events: none;
}

.input-container:hover + .field-description + .field-error + .field-success + .keyboard-help,
.input-container:hover + .field-description + .field-success + .keyboard-help,
.input-container:hover + .field-description + .keyboard-help,
.input-container:hover + .keyboard-help {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.help-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: #e5e7eb;
}

.help-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.help-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.help-item kbd {
  background-color: #374151;
  color: #e5e7eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 11px;
  border: 1px solid #4b5563;
  min-width: 40px;
  text-align: center;
  display: inline-block;
}

/* 进度条 */
.field-progress {
  margin-top: 6px;
}

.progress-bar {
  height: 4px;
  background-color: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 2px;
}

.progress-fill.progress-weak {
  background-color: #ef4444;
}

.progress-fill.progress-medium {
  background-color: #f59e0b;
}

.progress-fill.progress-strong {
  background-color: #10b981;
}

.progress-text {
  font-size: 11px;
  color: #6b7280;
  text-align: right;
}

/* 屏幕阅读器专用 */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* 变体样式 */
.field-variant-outlined .input-container {
  background-color: transparent;
  border-width: 2px;
}

.field-variant-filled .input-container {
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
}

.field-variant-filled.input-focused .input-container {
  background-color: #ffffff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .field-label {
    font-size: 13px;
  }

  .input-field {
    font-size: 13px;
  }

  .field-error,
  .field-success {
    font-size: 11px;
    padding: 6px;
  }

  .keyboard-help {
    font-size: 11px;
    padding: 8px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .field-label {
    color: #e5e7eb;
  }

  .input-container {
    background-color: #1f2937;
    border-color: #374151;
  }

  .input-container:hover {
    border-color: #4b5563;
  }

  .input-disabled {
    background-color: #111827;
    border-color: #374151;
  }

  .input-readonly {
    background-color: #111827;
  }

  .input-field {
    color: #f9fafb;
  }

  .input-field::placeholder {
    color: #6b7280;
  }

  .field-prefix,
  .field-suffix {
    background-color: #111827;
    color: #9ca3af;
  }

  .field-description {
    color: #9ca3af;
  }

  .progress-text {
    color: #9ca3af;
  }

  .field-variant-filled .input-container {
    background-color: #374151;
    border-color: #4b5563;
  }

  .field-variant-filled.input-focused .input-container {
    background-color: #1f2937;
  }
}

/* 高对比度模式适配 */
@media (prefers-contrast: high) {
  .input-container {
    border-width: 3px;
  }

  .field-error,
  .field-success {
    border-width: 2px;
  }
}

/* 减少动画模式适配 */
@media (prefers-reduced-motion: reduce) {
  .input-container,
  .validation-icon,
  .progress-fill,
  .keyboard-help {
    transition: none;
  }

  .icon-validating {
    animation: none;
  }
}
</style>