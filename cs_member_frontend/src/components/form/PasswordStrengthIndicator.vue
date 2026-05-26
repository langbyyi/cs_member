<template>
  <div class="password-strength-indicator" :class="{ 'has-value': password }">
    <!-- 密码强度条 -->
    <div class="strength-bars">
      <div
        v-for="(level, index) in strengthLevels"
        :key="index"
        class="strength-bar"
        :class="{
          'active': currentStrengthScore > index,
          [`strength-${getStrengthLevel(currentStrengthScore)}`]: true
        }"
      ></div>
    </div>

    <!-- 强度文字 -->
    <div class="strength-text" :class="`strength-${getStrengthLevel(currentStrengthScore)}`">
      <span v-if="password" class="strength-label">
        密码强度：<strong>{{ getStrengthText(currentStrengthScore) }}</strong>
      </span>
      <span v-else class="strength-placeholder">
        请输入密码以检测强度
      </span>
    </div>

    <!-- 强度详情 -->
    <transition name="slide-fade">
      <div v-if="showDetails && password" class="strength-details">
        <div class="detail-item" :class="{ 'met': criteria.lowercase }">
          <el-icon><Check v-if="criteria.lowercase" /><Close v-else /></el-icon>
          <span>包含小写字母 (a-z)</span>
        </div>
        <div class="detail-item" :class="{ 'met': criteria.uppercase }">
          <el-icon><Check v-if="criteria.uppercase" /><Close v-else /></el-icon>
          <span>包含大写字母 (A-Z)</span>
        </div>
        <div class="detail-item" :class="{ 'met': criteria.number }">
          <el-icon><Check v-if="criteria.number" /><Close v-else /></el-icon>
          <span>包含数字 (0-9)</span>
        </div>
        <div class="detail-item" :class="{ 'met': criteria.special }">
          <el-icon><Check v-if="criteria.special" /><Close v-else /></el-icon>
          <span>包含特殊字符 (!@#$%^&*)</span>
        </div>
        <div class="detail-item" :class="{ 'met': criteria.length }">
          <el-icon><Check v-if="criteria.length" /><Close v-else /></el-icon>
          <span>长度不少于8位</span>
        </div>
      </div>
    </transition>

    <!-- 改进建议 -->
    <transition name="slide-fade">
      <div v-if="suggestions && suggestions.length > 0" class="strength-suggestions">
        <div class="suggestions-header">
          <el-icon><InfoFilled /></el-icon>
          <span>改进建议：</span>
        </div>
        <ul class="suggestions-list">
          <li v-for="(suggestion, index) in suggestions" :key="index">
            {{ suggestion }}
          </li>
        </ul>
      </div>
    </transition>

    <!-- 安全提示 -->
    <transition name="slide-fade">
      <div v-if="showSecurityTip && currentStrengthScore <= 2" class="security-tip">
        <el-icon class="tip-icon"><Warning /></el-icon>
        <div class="tip-content">
          <strong>安全提示：</strong>
          为了账户安全，建议使用强密码并定期更新。避免使用生日、姓名等个人信息作为密码。
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Check, Close, InfoFilled, Warning } from '@element-plus/icons-vue'
import { formValidator, type ValidationResult } from '@/utils/formValidation'

// Props
interface Props {
  password: string
  showDetails?: boolean
  showSecurityTip?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showDetails: true,
  showSecurityTip: true
})

// 强度级别
const strengthLevels = [1, 2, 3, 4, 5]

// 密码验证结果
const validationResult = ref<ValidationResult>({ isValid: false, message: '' })
const currentStrengthScore = ref(0)

// 密码标准
const criteria = computed(() => ({
  length: props.password.length >= 8,
  lowercase: /[a-z]/.test(props.password),
  uppercase: /[A-Z]/.test(props.password),
  number: /\d/.test(props.password),
  special: /[^a-zA-Z0-9]/.test(props.password)
}))

// 改进建议
const suggestions = computed(() => {
  const sugs: string[] = []

  if (!criteria.value.length) {
    sugs.push('增加密码长度到8位以上')
  }
  if (!criteria.value.lowercase) {
    sugs.push('添加小写字母')
  }
  if (!criteria.value.uppercase) {
    sugs.push('添加大写字母')
  }
  if (!criteria.value.number) {
    sugs.push('添加数字')
  }
  if (!criteria.value.special) {
    sugs.push('添加特殊字符（如 !@#$%^&*）')
  }

  // 避免常见密码
  if (props.password.length > 0 && isCommonPassword(props.password)) {
    sugs.push('避免使用常见密码')
  }

  return sugs.slice(0, 3) // 最多显示3个建议
})

// 监听密码变化
watch(() => props.password, (newPassword) => {
  if (newPassword) {
    const result = formValidator.validateField('newPassword', newPassword)
    validationResult.value = result
    currentStrengthScore.value = result.strength || 0
  } else {
    validationResult.value = { isValid: false, message: '' }
    currentStrengthScore.value = 0
  }
}, { immediate: true })

// 获取强度级别
const getStrengthLevel = (score: number): 'weak' | 'medium' | 'strong' => {
  if (score <= 2) return 'weak'
  if (score <= 4) return 'medium'
  return 'strong'
}

// 获取强度文本
const getStrengthText = (score: number): string => {
  if (score <= 2) return '弱'
  if (score <= 4) return '中等'
  return '强'
}

// 检测常见密码
const isCommonPassword = (password: string): boolean => {
  const commonPasswords = [
    '123456', '123456789', 'password', '12345678', 'qwerty',
    '123123', '111111', '1234567890', '1234567', 'sunshine',
    'qwerty123', 'iloveyou', 'princess', 'admin', 'welcome',
    '666666', 'abc123', 'football', '123123123', 'monkey',
    '654321', '!@#$%^&*', '888888', '999999', '000000'
  ]

  return commonPasswords.includes(password.toLowerCase())
}
</script>

<style scoped>
.password-strength-indicator {
  margin-top: 8px;
  font-size: 13px;
}

/* 强度条样式 */
.strength-bars {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background-color: #e5e7eb;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-bar.active {
  background-color: #6b7280;
}

.strength-bar.active.strength-weak {
  background-color: #ef4444;
  box-shadow: 0 0 6px rgba(239, 68, 68, 0.3);
}

.strength-bar.active.strength-medium {
  background-color: #f59e0b;
  box-shadow: 0 0 6px rgba(245, 158, 11, 0.3);
}

.strength-bar.active.strength-strong {
  background-color: #10b981;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.3);
}

/* 强度文字样式 */
.strength-text {
  font-size: 12px;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.strength-text.strength-weak {
  color: #ef4444;
}

.strength-text.strength-medium {
  color: #f59e0b;
}

.strength-text.strength-strong {
  color: #10b981;
}

.strength-placeholder {
  color: #9ca3af;
  font-style: italic;
}

/* 详情样式 */
.strength-details {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  color: #6b7280;
  font-size: 12px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item.met {
  color: #059669;
}

.detail-item .el-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.detail-item.met .el-icon {
  color: #10b981;
}

.detail-item:not(.met) .el-icon {
  color: #ef4444;
}

/* 建议样式 */
.strength-suggestions {
  background-color: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
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
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions-list li {
  color: #92400e;
  font-size: 12px;
  margin-bottom: 4px;
  padding-left: 16px;
  position: relative;
  line-height: 1.4;
}

.suggestions-list li:last-child {
  margin-bottom: 0;
}

.suggestions-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #f59e0b;
}

/* 安全提示样式 */
.security-tip {
  display: flex;
  gap: 10px;
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 12px;
  color: #991b1b;
  font-size: 12px;
  line-height: 1.5;
}

.tip-icon {
  color: #ef4444;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.tip-content {
  flex: 1;
}

.tip-content strong {
  font-weight: 600;
  color: #7f1d1d;
}

/* 动画效果 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .strength-bars {
    gap: 3px;
  }

  .strength-bar {
    height: 3px;
  }

  .strength-text {
    font-size: 11px;
  }

  .detail-item,
  .suggestions-list li,
  .security-tip {
    font-size: 11px;
  }

  .strength-details,
  .strength-suggestions,
  .security-tip {
    padding: 10px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .strength-bar {
    background-color: #374151;
  }

  .strength-details {
    background-color: #1f2937;
    border-color: #374151;
  }

  .detail-item {
    color: #9ca3af;
  }

  .strength-suggestions {
    background-color: #451a03;
    border-color: #92400e;
  }

  .suggestions-header,
  .suggestions-list li {
    color: #fef3c7;
  }

  .security-tip {
    background-color: #450a0a;
    border-color: #991b1b;
    color: #fecaca;
  }

  .tip-content {
    color: #fecaca;
  }

  .tip-content strong {
    color: #fca5a5;
  }
}

/* 高对比度模式适配 */
@media (prefers-contrast: high) {
  .strength-bar {
    border: 1px solid currentColor;
  }

  .strength-details,
  .strength-suggestions,
  .security-tip {
    border-width: 2px;
  }
}

/* 减少动画模式适配 */
@media (prefers-reduced-motion: reduce) {
  .strength-bar,
  .strength-text,
  .slide-fade-enter-active,
  .slide-fade-leave-active {
    transition: none;
  }

  .slide-fade-enter-from,
  .slide-fade-leave-to {
    transform: none;
  }
}
</style>