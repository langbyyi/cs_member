<template>
  <div class="register-container" :class="{ 'page-entered': pageEntered }">
    <!-- 背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="register-box">
      <div class="register-header">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg width="40" height="40" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#ffffff" />
                  <stop offset="100%" stop-color="#e6f7ff" />
                </linearGradient>
              </defs>
              <rect x="10" y="20" width="80" height="60" rx="12" fill="url(#logoGradient)" />
              <rect x="15" y="10" width="70" height="10" rx="4" fill="rgba(255,255,255,0.8)" />
              <circle cx="25" cy="75" r="4" fill="#409EFF" />
              <circle cx="75" cy="75" r="4" fill="#409EFF" />
              <path d="M35 45 L50 60 L65 45" stroke="#409EFF" stroke-width="6" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="logo-text">
            <h1 class="brand-name">24H Store</h1>
            <p class="brand-slogan">您的专属便利生活</p>
          </div>
        </div>
        <h2 class="welcome-text">会员注册</h2>
      </div>

  
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <!-- 注册信息区域 -->
        <div class="form-section">
          <el-form-item prop="name">
            <el-input
              v-model="registerForm.name"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              clearable
              maxlength="50"
            />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入11位手机号码"
              size="large"
              prefix-icon="Iphone"
              clearable
              maxlength="11"
            />
          </el-form-item>

          <el-form-item prop="email">
            <div class="email-input-group">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入常用邮箱地址"
                size="large"
                prefix-icon="Message"
                clearable
                type="email"
              />
              <el-button
                size="large"
                type="primary"
                :disabled="!canSendEmailCode || emailCodeSending"
                :loading="emailCodeSending"
                @click="handleSendEmailCode"
                class="email-code-btn"
                :class="{ 'btn-success': emailCodeSent }"
              >
                <el-icon v-if="emailCodeSent"><Check /></el-icon>
                {{ emailCodeButtonText }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="emailCode" v-if="emailCodeSent">
            <el-input
              v-model="registerForm.emailCode"
              placeholder="请输入6位邮箱验证码"
              size="large"
              prefix-icon="Key"
              clearable
              maxlength="6"
            />
            <div class="verification-hint" v-if="emailCodeSent">
              <el-icon><InfoFilled /></el-icon>
              验证码已发送至 {{ registerForm.email }}
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请设置6-20位登录密码"
              size="large"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="gender">
            <div class="gender-selection">
              <span class="field-label">性别</span>
              <el-radio-group v-model="registerForm.gender" size="large">
                <el-radio :value="1" class="gender-radio">
                  <el-icon><Male /></el-icon>
                  男
                </el-radio>
                <el-radio :value="2" class="gender-radio">
                  <el-icon><Female /></el-icon>
                  女
                </el-radio>
              </el-radio-group>
            </div>
          </el-form-item>
        </div>

  
        <!-- 协议同意区域 -->
        <div class="form-section agreement-section">
          <div class="agreement-header">
            <div class="agreement-icon">
              <el-icon size="24" color="#1e88e5"><Document /></el-icon>
            </div>
            <div class="agreement-title">
              <h3>服务协议确认</h3>
              <p>为了保障您的权益，请在注册前仔细阅读以下协议</p>
            </div>
          </div>

          <div class="agreement-content">
            <div class="agreement-checkbox-wrapper">
              <el-checkbox
                v-model="agreeTerms"
                size="large"
                class="agreement-checkbox"
                @change="handleTermsChange"
              >
                <span class="checkbox-label">
                  我已仔细阅读并完全同意以上协议
                </span>
              </el-checkbox>
            </div>

            <div class="agreement-links">
              <div
                class="link-item"
                :class="{ active: termsAgreed.terms }"
                @click="showTermsDialog('terms')"
              >
                <div class="link-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="link-content">
                  <div class="link-title">《用户服务协议》</div>
                  <div class="link-desc">了解服务使用规则和用户权利义务</div>
                </div>
                <div class="link-status">
                  <el-icon v-if="termsAgreed.terms" class="check-icon"><Check /></el-icon>
                  <el-icon v-else class="arrow-icon"><ArrowRight /></el-icon>
                </div>
              </div>

              <div
                class="link-item privacy"
                :class="{ active: termsAgreed.privacy }"
                @click="showTermsDialog('privacy')"
              >
                <div class="link-icon">
                  <el-icon><Lock /></el-icon>
                </div>
                <div class="link-content">
                  <div class="link-title">《隐私政策》</div>
                  <div class="link-desc">了解个人信息收集、使用和保护方式</div>
                </div>
                <div class="link-status">
                  <el-icon v-if="termsAgreed.privacy" class="check-icon"><Check /></el-icon>
                  <el-icon v-else class="arrow-icon"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>

            <!-- 协议提醒 -->
            <div class="agreement-reminder" v-if="!agreeTerms">
              <div class="reminder-content">
                <el-icon class="reminder-icon"><InfoFilled /></el-icon>
                <span>请阅读并同意《用户服务协议》和《隐私政策》</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮区域 -->
        <div class="form-actions">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            :disabled="!canRegister"
            @click="handleRegister"
          >
            <el-icon v-if="!loading"><UserFilled /></el-icon>
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </div>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账号？
          <router-link to="/member/login" class="link">
            立即登录
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
      </el-form>

      <div class="register-footer">
        <div class="footer-content">
          <div class="browser-tips">
            <span class="tips-label">推荐浏览器</span>
            <el-tag type="success" size="small" effect="plain">Chrome</el-tag>
            <el-tag type="warning" size="small" effect="plain">Firefox</el-tag>
            <el-tag type="info" size="small" effect="plain">Edge</el-tag>
          </div>
          <div class="security-tips">
            <el-icon><Lock /></el-icon>
            <span>您的信息将被加密保护</span>
          </div>
        </div>
      </div>
    </div>



    <!-- 协议对话框 -->
    <UserTermsDialog
      v-model:visible="termsDialogVisible"
      :type="termsDialogType"
      @agree="handleTermsAgree"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  User,
  Iphone,
  Message,
  Lock,
  Key,
  Male,
  Female,
  Check,
  InfoFilled,
  ArrowLeft,
  ArrowRight,
  UserFilled,
  Document
} from '@element-plus/icons-vue'
import { memberRegister, sendEmailCode } from '@/api/auth'
import UserTermsDialog from '@/components/common/UserTermsDialog.vue'

const router = useRouter()

// 页面进入状态
const pageEntered = ref(false)

const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)
const emailCodeSending = ref(false)
const emailCodeCountdown = ref(0)
const emailCodeSent = ref(false)
const termsDialogVisible = ref(false)
const termsDialogType = ref<'terms' | 'privacy'>('terms')
const termsAgreed = ref({
  terms: false,
  privacy: false
})

// 计算属性：是否可以发送验证码
const canSendEmailCode = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(registerForm.email) && emailCodeCountdown.value === 0
})

// 计算属性：验证码按钮文本
const emailCodeButtonText = computed(() => {
  if (emailCodeCountdown.value > 0) {
    return `${emailCodeCountdown.value}s后重新发送`
  }
  return emailCodeSending.value ? '发送中...' : (emailCodeSent.value ? '重新发送' : '获取验证码')
})

// 计算属性：是否可以注册
const canRegister = computed(() => {
  return (
    registerForm.name.trim() &&
    registerForm.phone.trim() &&
    registerForm.email.trim() &&
    registerForm.password.trim() &&
    registerForm.confirmPassword.trim() &&
    registerForm.gender &&
    agreeTerms.value &&
    emailCodeSent.value &&
    registerForm.emailCode.trim().length === 6
  )
})

// 注册表单数据
const registerForm = reactive({
  name: '',
  phone: '',
  email: '',
  emailCode: '',
  password: '',
  confirmPassword: '',
  gender: 1
})

// 表单验证规则
const registerRules: FormRules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名在2-50个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '邮箱验证码为6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '邮箱验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}


// 发送邮箱验证码
const handleSendEmailCode = async () => {
  if (!canSendEmailCode.value) return

  try {
    emailCodeSending.value = true

    // 调用后端API发送邮箱验证码
    await sendEmailCode({
      userType: 'member',
      email: registerForm.email,
      verificationType: 'register'
    })

    emailCodeSent.value = true
    ElMessage.success('验证码已发送到您的邮箱，请查收')

    // 开始倒计时
    emailCodeCountdown.value = 60
    const timer = setInterval(() => {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)

  } catch (error: any) {
    // 错误已由全局拦截器处理
  } finally {
    emailCodeSending.value = false
  }
}


// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) return

    if (!agreeTerms.value) {
      ElMessage.warning('请先同意用户服务协议和隐私政策')
      return
    }

    if (!termsAgreed.value.terms || !termsAgreed.value.privacy) {
      ElMessage.warning('请阅读并同意用户服务协议和隐私政策')
      return
    }

    loading.value = true

    // 构建注册数据
    const registerData = {
      name: registerForm.name,
      phone: registerForm.phone,
      email: registerForm.email,
      emailCode: registerForm.emailCode,
      password: registerForm.password,
      gender: registerForm.gender
    }

    // 调用注册API
    await memberRegister(registerData)

    ElMessage.success('注册成功！请使用手机号登录')
    router.push('/member/login')
  } catch (error: any) {
    // 错误已由全局拦截器处理
  } finally {
    loading.value = false
  }
}

// 显示协议对话框
const showTermsDialog = (type: 'terms' | 'privacy') => {
  termsDialogType.value = type
  termsDialogVisible.value = true
}

// 处理协议同意
const handleTermsAgree = (type: 'terms' | 'privacy') => {
  termsAgreed.value[type] = true
  // 检查是否两个协议都同意了
  if (termsAgreed.value.terms && termsAgreed.value.privacy) {
    agreeTerms.value = true
  }
}

// 处理协议复选框变化
const handleTermsChange = (checked: boolean) => {
  if (checked) {
    // 如果勾选总同意，自动将两个子协议都标记为已阅读
    termsAgreed.value = {
      terms: true,
      privacy: true
    }
  } else {
    // 如果取消勾选，重置同意状态
    termsAgreed.value = {
      terms: false,
      privacy: false
    }
  }
}

// 页面挂载时触发淡入效果
onMounted(() => {
  // 延迟一帧以确保DOM已渲染
  requestAnimationFrame(() => {
    pageEntered.value = true
  })
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 动态背景图形 */
.background-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  top: -10%;
  left: -10%;
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #a0c4ff 0%, #c2e9fb 100%);
  animation-delay: 0s;
}

.shape-2 {
  bottom: -20%;
  right: -5%;
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #ffc3a0 0%, #ffafbd 100%);
  animation-delay: -5s;
}

.shape-3 {
  top: 40%;
  left: 40%;
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%);
  animation-delay: -10s;
  opacity: 0.4;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -50px) rotate(10deg); }
  66% { transform: translate(-20px, 20px) rotate(-5deg); }
}

.register-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.page-entered .register-box {
  opacity: 1;
  transform: translateY(0);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.5);
  padding: 10px 20px;
  border-radius: 50px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409EFF, #3a8ee6);
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
}

.logo-text {
  text-align: left;
}

.brand-name {
  font-size: 18px;
  font-weight: 800;
  color: #2c3e50;
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.brand-slogan {
  font-size: 11px;
  color: #606266;
  margin: 0;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.welcome-text {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.5px;
  background: linear-gradient(120deg, #2c3e50, #3498db);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* Form Styles */
.register-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid transparent;
  box-shadow: none;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 48px;
}

.register-form :deep(.el-input__wrapper:hover),
.register-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: rgba(64, 158, 255, 0.3);
}

.register-form :deep(.el-input__inner) {
  font-size: 15px;
  color: #2c3e50;
  height: 48px;
}

.form-section {
  margin-bottom: 24px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

/* Gender Selection */
.gender-selection {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 12px;
}

.field-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* Email Code Button */
.email-input-group {
  display: flex;
  gap: 12px;
}

.email-input-group :deep(.el-input) {
  flex: 1;
}

.email-code-btn {
  height: 48px;
  border-radius: 12px;
  padding: 0 20px;
  font-weight: 500;
}

.verification-hint {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
}

/* Agreement Section */
.agreement-section {
  margin-bottom: 24px;
}

.agreement-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.agreement-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(64, 158, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409EFF;
}

.agreement-title h3 {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 2px 0;
}

.agreement-title p {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.agreement-checkbox-wrapper {
  margin-bottom: 12px;
}

.agreement-checkbox {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(64, 158, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.agreement-checkbox:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(64, 158, 255, 0.3);
}

.agreement-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.link-item:hover {
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateX(4px);
}

.link-item.active {
  background: rgba(103, 194, 58, 0.1);
  border-color: rgba(103, 194, 58, 0.2);
}

.link-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(64, 158, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409EFF;
}

.link-content {
  flex: 1;
}

.link-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.link-desc {
  font-size: 12px;
  color: #909399;
}

.agreement-reminder {
  margin-top: 16px;
}

.reminder-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(230, 162, 60, 0.1);
  border: 1px solid rgba(230, 162, 60, 0.2);
  border-radius: 8px;
  font-size: 14px;
  color: #e6a23c;
  line-height: 1.5;
}

/* Buttons */
.register-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #53a8ff 0%, #409EFF 100%);
}

.register-btn:disabled {
  background: #c0c4cc;
  box-shadow: none;
  cursor: not-allowed;
}

.login-link {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.login-link .link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
}

.register-footer {
  margin-top: 32px;
  text-align: center;
}

.footer-content {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.browser-tips, .security-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

/* Responsive */
@media (max-width: 480px) {
  .register-box {
    padding: 32px 20px;
  }
  
  .welcome-text {
    font-size: 24px;
  }
  
  .email-input-group {
    flex-direction: column;
  }
  
  .email-code-btn {
    width: 100%;
  }
}
</style>