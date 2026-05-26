<template>
  <el-dialog
    v-model="visible"
    :title="`${userType}忘记密码`"
    width="580px"
    :close-on-click-modal="false"
    class="forgot-password-dialog"
    align-center
  >
    <!-- 美化的步骤指示器 -->
    <div class="step-indicator-container">
      <div class="step-indicator">
        <div class="step-item" :class="{ active: resetStep >= 1, completed: resetStep > 1 }">
          <div class="step-circle">
            <el-icon v-if="resetStep > 1" class="step-icon-completed"><CircleCheck /></el-icon>
            <el-icon v-else class="step-icon-default"><User /></el-icon>
          </div>
          <div class="step-label">身份验证</div>
          <div class="step-desc">验证手机号</div>
        </div>

        <div class="step-connector" :class="{ active: resetStep >= 2 }">
          <div class="connector-line"></div>
        </div>

        <div class="step-item" :class="{ active: resetStep >= 2, completed: resetStep > 2 }">
          <div class="step-circle">
            <el-icon v-if="resetStep > 2" class="step-icon-completed"><CircleCheck /></el-icon>
            <el-icon v-else class="step-icon-default"><Message /></el-icon>
          </div>
          <div class="step-label">邮箱验证</div>
          <div class="step-desc">验证邮箱地址</div>
        </div>

        <div class="step-connector" :class="{ active: resetStep >= 3 }">
          <div class="connector-line"></div>
        </div>

        <div class="step-item" :class="{ active: resetStep >= 3, completed: resetStep > 3 }">
          <div class="step-circle">
            <el-icon v-if="resetStep > 3" class="step-icon-completed"><CircleCheck /></el-icon>
            <el-icon v-else class="step-icon-default"><Lock /></el-icon>
          </div>
          <div class="step-label">重置密码</div>
          <div class="step-desc">设置新密码</div>
        </div>
      </div>
    </div>

    <!-- 步骤内容区域 -->
    <div class="step-content-wrapper">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="forgot-form"
        @submit.prevent
      >
      <!-- 步骤1: 手机号验证 -->
      <div v-if="resetStep === 1" class="step-panel">
        <div class="step-header">
          <div class="step-icon-wrapper">
            <el-icon class="step-main-icon"><User /></el-icon>
          </div>
          <div class="step-title">身份验证</div>
          <div class="step-subtitle">请输入{{ userTypeText }}手机号码，我们将验证您的身份</div>
        </div>

        <div class="step-form-content">
          <el-form-item prop="phone" class="form-item-float">
            <el-input
              v-model="form.phone"
              :placeholder="`请输入${userTypeText}11位手机号码`"
              size="large"
              clearable
              maxlength="11"
              class="modern-input"
              @keydown.enter.prevent.stop
              @keyup.enter.prevent.stop="handleVerifyPhone"
            >
              <template #prefix>
                <el-icon class="input-icon"><Iphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </div>
      </div>

      <!-- 步骤2: 邮箱验证 -->
      <div v-if="resetStep === 2" class="step-panel">
        <div class="step-header">
          <div class="step-icon-wrapper">
            <el-icon class="step-main-icon"><Message /></el-icon>
          </div>
          <div class="step-title">邮箱验证</div>
          <div class="step-subtitle">请输入{{ userTypeText }}邮箱地址，我们将发送验证码到您的邮箱</div>
        </div>

        <!-- 验证成功提示 -->
        <div v-if="userInfo" class="success-tip">
          <el-icon class="tip-icon"><CircleCheck /></el-icon>
          <span>身份验证成功，请完成邮箱验证</span>
        </div>

        <div class="step-form-content">
          <el-form-item prop="email" class="form-item-float">
            <label class="verification-label">邮箱地址</label>
            <el-input
              v-model="form.email"
              :placeholder="`请输入${userTypeText}邮箱地址`"
              size="large"
              clearable
              type="email"
              class="modern-input"
              @keydown.enter.prevent.stop
              @keyup.enter.prevent.stop="handleSendEmailCode"
            >
              <template #prefix>
                <el-icon class="input-icon"><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="emailVerifyCode" class="form-item-float">
            <label class="verification-label">邮箱验证码</label>
            <div class="code-input-group">
              <el-input
                v-model="form.emailVerifyCode"
                placeholder="请输入邮箱验证码"
                maxlength="6"
                size="large"
                class="code-input modern-input"
                :disabled="!emailCodeSent"
                @keydown.enter.prevent.stop
                @keyup.enter.prevent.stop="handleVerifyEmail"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Key /></el-icon>
                </template>
              </el-input>
              <el-button
                size="large"
                :disabled="emailCodeCountdown > 0 || !form.email"
                :loading="sendingEmailCode"
                @click="handleSendEmailCode"
                class="code-send-btn email-code-btn"
              >
                <span v-if="!emailCodeSent">发送邮箱验证码</span>
                <span v-else-if="emailCodeCountdown > 0">{{ emailCodeCountdown }}s</span>
                <span v-else>重新发送</span>
              </el-button>
            </div>
          </el-form-item>
        </div>
      </div>

      <!-- 步骤3: 重置密码 -->
      <div v-if="resetStep === 3" class="step-panel">
        <div class="step-header">
          <div class="step-icon-wrapper">
            <el-icon class="step-main-icon"><Lock /></el-icon>
          </div>
          <div class="step-title">重置密码</div>
          <div class="step-subtitle">请设置{{ userTypeText }}的新密码，必须包含字母和数字的组合</div>
        </div>

        <div class="step-form-content">
          <el-form-item label="新密码" prop="newPassword" class="form-item-float">
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码（8-20位）"
              size="large"
              show-password
              clearable
              class="modern-input"
              @keydown.enter.prevent.stop
              @keyup.enter.prevent.stop="handleResetPassword"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword" class="form-item-float">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              size="large"
              show-password
              clearable
              class="modern-input"
              @keydown.enter.prevent.stop
              @keyup.enter.prevent.stop="handleResetPassword"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </div>
      </div>
      </el-form>
    </div>

    <!-- 美化的底部操作区 -->
    <template #footer>
      <div class="dialog-footer-modern">
        <div class="footer-left">
          <el-button
            size="large"
            @click="handleClose"
            class="btn-cancel"
          >
            取消
          </el-button>
        </div>

        <div class="footer-right">
          <el-button
            v-if="resetStep === 1"
            type="primary"
            size="large"
            :loading="loading"
            @click.prevent.stop="handleVerifyPhone"
            class="btn-primary"
          >
            下一步
          </el-button>
          <el-button
            v-if="resetStep === 2 && emailCodeSent && form.emailVerifyCode"
            type="primary"
            size="large"
            :loading="loading"
            @click.prevent.stop="handleVerifyEmail"
            class="btn-primary"
          >
            验证邮箱
          </el-button>
          <el-button
            v-if="resetStep === 3 && form.newPassword && form.confirmPassword"
            type="primary"
            size="large"
            :loading="loading"
            @click.prevent.stop="handleResetPassword"
            class="btn-primary"
          >
            完成重置
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  CircleCheck,
  CircleClose,
  User,
  Iphone,
  Message,
  Key,
  Lock
} from '@element-plus/icons-vue'
import {
  validateMemberPhoneFormat,
  validateAdminPhoneFormat,
  sendMemberResetPasswordCode,
  sendAdminResetPasswordCode,
  verifyMemberResetCodeAndGenerateToken,
  verifyAdminResetCodeAndGenerateToken,
  resetPasswordFinal,
  checkPhoneForReset,
  verifyEmailAndSendCode,
  verifyResetCodeStep3,
  resetPasswordWithCode
} from '@/api/auth'

// Props定义
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userType: {
    type: String,
    default: '会员', // '会员' 或 '管理员'
    validator: (value: string) => ['会员', '管理员'].includes(value)
  }
})

// Emits定义
const emit = defineEmits(['update:modelValue', 'success', 'close'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref<FormInstance>()
const loading = ref(false)
const sendingEmailCode = ref(false)
const emailCodeCountdown = ref(0)
const emailCodeSent = ref(false)
const resetStep = ref(1)
const resetToken = ref('')
const userInfo = ref<any>(null)
const backgroundCheckInterval = ref<NodeJS.Timeout | null>(null)

const form = reactive({
  phone: '',
  email: '',
  emailVerifyCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算属性
const userTypeText = computed(() => props.userType)

// 表单验证规则
const rules: FormRules = {
  phone: [
    { required: true, message: `请输入${userTypeText.value}手机号`, trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  emailVerifyCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '邮箱验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度在 8 到 20 个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,20}$/,
      message: '密码必须包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法定义
const startCountdown = (type: 'email' = 'email') => {
  const timer = setInterval(() => {
    if (type === 'email') {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }
  }, 1000)
}

const resetForm = () => {
  form.phone = ''
  form.email = ''
  form.emailVerifyCode = ''
  form.newPassword = ''
  form.confirmPassword = ''
  userInfo.value = null
  emailCodeCountdown.value = 0
  emailCodeSent.value = false
  resetStep.value = 1
  resetToken.value = ''
  loading.value = false
  sendingEmailCode.value = false

  // 清理临时遮罩
  const tempOverlay = document.querySelector('.forgot-password-temp-overlay')
  if (tempOverlay) {
    tempOverlay.remove()
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
  emit('close')
}

const handleTimeoutReset = () => {
  ElMessage.error('操作超时，请重新开始密码重置流程')
  resetStep.value = 1
  resetForm()
}

const handleVerifyPhone = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validateField('phone')
    loading.value = true

    // 根据用户类型选择对应的API调用
    const response = props.userType === '管理员'
      ? await validateAdminPhoneFormat({ phone: form.phone })
      : await validateMemberPhoneFormat({ phone: form.phone })

    if (response && response.code === 200) {
      userInfo.value = {
        phone: form.phone
      }

      ElMessage.success(response.message || '手机号格式正确，请输入邮箱地址')
      resetStep.value = 2
    } else {
      // 直接显示后端返回的错误消息，避免重复前缀
      const errorMessage = response?.message || '手机号格式不正确，请重试'
      ElMessage.error(errorMessage)
    }
  } catch (error: any) {
    // 验证错误不需要弹窗，axios拦截器已经处理了
    if (error.name === 'ValidationError') return
    // 其他错误也由axios拦截器处理，不需要重复弹窗
  } finally {
    loading.value = false
  }
}

// 发送邮箱验证码
const handleSendEmailCode = async () => {
  if (!formRef.value) return

  try {
    // 验证邮箱地址
    await formRef.value.validateField('email')

    if (!form.email) {
      ElMessage.error('请先输入邮箱地址')
      return
    }

    sendingEmailCode.value = true

    // 根据用户类型选择对应的API调用
    const response = props.userType === '管理员'
      ? await sendAdminResetPasswordCode({
          phone: form.phone,
          email: form.email
        })
      : await sendMemberResetPasswordCode({
          phone: form.phone,
          email: form.email
        })

    if (response && response.code === 200) {
      emailCodeSent.value = true
      ElMessage.success(response.message || `邮箱验证码已发送到 ${form.email}`)
      emailCodeCountdown.value = 60
      startCountdown('email')
    } else {
      // 直接显示后端返回的错误消息，避免重复前缀
      const errorMessage = response?.message || '验证码发送失败，请检查手机号和邮箱是否匹配'
      ElMessage.error(errorMessage)
    }
  } catch (error: any) {
    // 验证错误不需要弹窗，axios拦截器已经处理了
    if (error.name === 'ValidationError') return
    // 其他错误也由axios拦截器处理，不需要重复弹窗
  } finally {
    sendingEmailCode.value = false
  }
}

// 验证邮箱验证码
const handleVerifyEmail = async () => {
  if (!formRef.value) return

  try {
    // 验证表单字段
    await formRef.value.validateField('emailVerifyCode')

    loading.value = true

    // 根据用户类型选择对应的API调用
    const response = props.userType === '管理员'
      ? await verifyAdminResetCodeAndGenerateToken({
          phone: form.phone,
          email: form.email,
          emailCode: form.emailVerifyCode
        })
      : await verifyMemberResetCodeAndGenerateToken({
          phone: form.phone,
          email: form.email,
          emailCode: form.emailVerifyCode
        })

    if (response && response.code === 200) {
      if (response.data?.resetToken) {
        resetToken.value = response.data.resetToken
      }
      ElMessage.success(response.message || '邮箱验证码验证成功')
      resetStep.value = 3
    } else {
      // 直接显示后端返回的错误消息，避免重复前缀
      const errorMessage = response?.message || '邮箱验证码验证失败'
      ElMessage.error(errorMessage)
    }
  } catch (error: any) {
    // 验证错误不需要弹窗，axios拦截器已经处理了
    if (error.name === 'ValidationError') return
    // 其他错误也由axios拦截器处理，不需要重复弹窗
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validateField('newPassword')
    await formRef.value.validateField('confirmPassword')
    loading.value = true

    if (!resetToken.value) {
      throw new Error('重置令牌无效，请重新开始密码重置流程')
    }

    // 使用新的API接口
    const response = await resetPasswordFinal({
      resetToken: resetToken.value,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })

    if (response && response.code === 200) {
      emit('success')
      handleClose()
    } else {
      // 直接显示后端返回的错误消息，避免重复前缀
      const errorMessage = response?.message || '密码重置失败'
      ElMessage.error(errorMessage)
    }
  } catch (error: any) {
    // 验证错误不需要弹窗，axios拦截器已经处理了
    if (error.name === 'ValidationError') return
    // 其他错误也由axios拦截器处理，不需要重复弹窗
  } finally {
    loading.value = false
  }
}

// 强制设置覆盖层背景
const forceSetOverlayBackground = () => {
  // 直接针对发现的DOM结构
  const overlaySelectors = [
    '.el-overlay.forgot-password-overlay.el-modal-dialog', // 主覆盖层
    '.el-overlay.forgot-password-overlay', // 简化版本
    '.el-overlay.el-modal-dialog', // 通用版本
    '.el-overlay' // 最基本的覆盖层
  ]

  overlaySelectors.forEach(selector => {
    const overlay = document.querySelector(selector)
    if (overlay && overlay instanceof HTMLElement) {
      // 获取当前主题背景图片
      const computedStyle = getComputedStyle(document.documentElement)
      const bgImage = computedStyle.getPropertyValue('--bg-image').trim()
      const bgGradient = computedStyle.getPropertyValue('--bg-gradient').trim()

      if (bgImage && bgImage !== 'none') {
        // 强制设置背景图片
        overlay.style.setProperty('background', bgImage, 'important')
        overlay.style.setProperty('background-size', 'cover', 'important')
        overlay.style.setProperty('background-position', 'center', 'important')
        overlay.style.setProperty('background-repeat', 'no-repeat', 'important')
        overlay.style.setProperty('background-color', 'transparent', 'important')
        overlay.style.setProperty('background-image', bgImage, 'important')

        // 如果有渐变，添加渐变遮罩
        if (bgGradient && bgGradient !== 'none') {
          overlay.style.setProperty('background', `${bgImage}, ${bgGradient} !important`)
        }
      }
    }
  })

  // 也尝试覆盖整个body作为备用方案
  const body = document.body
  if (body && visible.value) {
    const computedStyle = getComputedStyle(document.documentElement)
    const bgImage = computedStyle.getPropertyValue('--bg-image').trim()

    if (bgImage && bgImage !== 'none') {
      // 创建一个临时的遮罩元素
      const tempOverlay = document.querySelector('.forgot-password-temp-overlay')
      if (!tempOverlay) {
        const div = document.createElement('div')
        div.className = 'forgot-password-temp-overlay'
        div.style.cssText = `
          position: fixed !important;
          top: 0 !important;
          left: 0 !important;
          right: 0 !important;
          bottom: 0 !important;
          background: ${bgImage} !important;
          background-size: cover !important;
          background-position: center !important;
          background-repeat: no-repeat !important;
          z-index: 1999 !important;
          pointer-events: none !important;
        `
        document.body.appendChild(div)
      }
    }
  }
}

// 开始定期检查背景
const startBackgroundCheck = () => {
  // 清除之前的定时器
  if (backgroundCheckInterval.value) {
    clearInterval(backgroundCheckInterval.value)
  }

  // 立即设置一次
  forceSetOverlayBackground()

  // 每100ms检查一次背景，持续5秒
  let checkCount = 0
  backgroundCheckInterval.value = setInterval(() => {
    forceSetOverlayBackground()
    checkCount++

    // 5秒后停止检查（50次 * 100ms）
    if (checkCount >= 50) {
      if (backgroundCheckInterval.value) {
        clearInterval(backgroundCheckInterval.value)
        backgroundCheckInterval.value = null
      }
    }
  }, 100)
}

// 监听visible变化
watch(visible, (newVal) => {
  if (newVal) {
    resetForm()

    // 多次尝试设置背景，确保DOM完全渲染
    nextTick(() => {
      forceSetOverlayBackground()
      startBackgroundCheck()

      // 额外的延迟设置
      setTimeout(forceSetOverlayBackground, 50)
      setTimeout(forceSetOverlayBackground, 200)
      setTimeout(forceSetOverlayBackground, 500)
    })
  } else {
    // 对话框关闭时清除定时器
    if (backgroundCheckInterval.value) {
      clearInterval(backgroundCheckInterval.value)
      backgroundCheckInterval.value = null
    }
  }
})

// 暴露方法给父组件
defineExpose({
  resetForm,
  handleVerifyPhone,
  handleSendEmailCode,
  handleVerifyEmail
})
</script>

<style>
/* 全局覆盖层样式 - 使用强选择器 */
.forgot-password-overlay {
  background: rgba(0, 0, 0, 0.2) !important;
  backdrop-filter: blur(8px) !important;
}

/* 对话框容器样式 */
.el-dialog.forgot-password-dialog {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px) !important;
  border-radius: 24px !important;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  padding: 0 !important;
  overflow: hidden !important;
}

.el-dialog.forgot-password-dialog .el-dialog__header {
  margin: 0 !important;
  padding: 24px 24px 0 !important;
  background: transparent !important;
}

.el-dialog.forgot-password-dialog .el-dialog__body {
  padding: 24px !important;
  background: transparent !important;
}

.el-dialog.forgot-password-dialog .el-dialog__footer {
  padding: 0 24px 24px !important;
  background: transparent !important;
  border: none !important;
}

.el-dialog.forgot-password-dialog .el-dialog__title {
  font-size: 20px !important;
  font-weight: 700 !important;
  color: #2c3e50 !important;
}

.el-dialog.forgot-password-dialog .el-dialog__headerbtn {
  top: 24px !important;
  right: 24px !important;
}
</style>

<style scoped>
/* 步骤指示器 */
.step-indicator-container {
  margin-bottom: 32px;
  padding: 0 12px;
}

.step-indicator {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
  flex: 1;
}

.step-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.step-item.active .step-circle {
  background: #ffffff;
  border-color: #409EFF;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}

.step-item.completed .step-circle {
  background: #409EFF;
  color: white;
}

.step-icon-default {
  font-size: 20px;
  color: #909399;
}

.step-item.active .step-icon-default {
  color: #409EFF;
}

.step-icon-completed {
  font-size: 20px;
  color: white;
}

.step-label {
  font-size: 14px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 4px;
}

.step-item.active .step-label {
  color: #2c3e50;
}

.step-item.completed .step-label {
  color: #409EFF;
}

.step-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.step-item.active .step-desc {
  color: #909399;
}

/* 连接线 */
.step-connector {
  position: absolute;
  top: 24px;
  left: 0;
  width: 100%;
  height: 2px;
  background: #e4e7ed;
  z-index: 0;
  transform: translateY(-50%);
}

.connector-line {
  height: 100%;
  background: #409EFF;
  transition: width 0.3s ease;
}

/* 步骤内容 */
.step-content-wrapper {
  min-height: 300px;
}

.step-header {
  text-align: center;
  margin-bottom: 32px;
}

.step-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: linear-gradient(135deg, #e6f7ff 0%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.15);
}

.step-main-icon {
  font-size: 32px;
  color: #409EFF;
}

.step-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 8px;
}

.step-subtitle {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

/* 表单样式 */
.forgot-form {
  max-width: 360px;
  margin: 0 auto;
}

.form-item-float {
  margin-bottom: 24px;
}

.modern-input :deep(.el-input__wrapper) {
  background: #f5f7fa;
  border: 1px solid transparent;
  box-shadow: none;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 48px;
}

.modern-input :deep(.el-input__wrapper:hover),
.modern-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: rgba(64, 158, 255, 0.3);
}

.modern-input :deep(.el-input__inner) {
  height: 48px;
  font-size: 15px;
  color: #2c3e50;
}

.input-icon {
  font-size: 18px;
  color: #909399;
  margin-right: 8px;
}

/* 验证码输入框 */
.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-send-btn {
  height: 48px;
  border-radius: 12px;
  padding: 0 20px;
  font-weight: 500;
  background: #e6f7ff;
  border-color: #b3e0ff;
  color: #409EFF;
}

.code-send-btn:hover:not(:disabled) {
  background: #409EFF;
  border-color: #409EFF;
  color: white;
}

/* 底部按钮 */
.dialog-footer-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
}

.btn-cancel {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  background: #f5f7fa;
  border: none;
  color: #606266;
  font-weight: 500;
}

.btn-cancel:hover {
  background: #e6e8eb;
  color: #303133;
}

.btn-primary {
  height: 44px;
  padding: 0 32px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
  font-weight: 600;
  font-size: 15px;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.4);
}

/* 成功提示 */
.success-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f0f9eb;
  border-radius: 12px;
  color: #67c23a;
  font-size: 14px;
  margin-bottom: 24px;
}

.tip-icon {
  font-size: 18px;
}

/* 响应式 */
@media (max-width: 480px) {
  .step-label {
    display: none;
  }
  
  .step-desc {
    display: none;
  }
  
  .step-circle {
    width: 40px;
    height: 40px;
  }
  
  .step-connector {
    top: 20px;
  }
  
  .code-input-group {
    flex-direction: column;
  }
  
  .code-send-btn {
    width: 100%;
  }
}
</style>