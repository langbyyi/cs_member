<template>
  <div class="login-container" :class="{ 'page-entered': pageEntered }">
    <!-- 背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="login-content">
      <div class="login-box">
        <div class="login-header">
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
          <h2 class="welcome-text">欢迎回来</h2>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <div class="form-item-group">
            <el-form-item prop="phone">
              <el-input
                v-model="loginForm.phone"
                placeholder="手机号码"
                size="large"
                prefix-icon="Iphone"
                clearable
                maxlength="11"
                @keyup.enter="handleEnterKey"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="登录密码"
                size="large"
                prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleEnterKey"
              />
            </el-form-item>
          </div>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-button link class="forgot-btn" @click.stop="handleForgotPassword">忘记密码？</el-button>
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="!canSubmit"
            class="submit-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </el-form>

        <div class="login-footer">
          <span class="no-account">还没有账号？</span>
          <router-link to="/member/register" class="register-link">
            立即注册
          </router-link>
        </div>
      </div>
      
      <!-- 底部管理员入口 -->
      <div class="admin-entry">
        <router-link to="/admin/login" class="admin-link">
          管理员登录 <el-icon><Right /></el-icon>
        </router-link>
      </div>
    </div>
  </div>

  <!-- 忘记密码对话框 -->
  <ForgotPasswordDialog
    v-model="forgotPasswordVisible"
    user-type="会员"
    @success="handlePasswordResetSuccess"
    @close="handleCloseForgotPassword"
  />
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Iphone, Lock, ArrowRight, Right } from '@element-plus/icons-vue'
import type { LoginRequest } from '@/types'
import ForgotPasswordDialog from '@/components/common/ForgotPasswordDialog.vue'

const userStore = useUserStore()

// 响应式变量
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const forgotPasswordVisible = ref(false)
const pageEntered = ref(false)

// 登录表单
const loginForm = reactive<LoginRequest>({
  phone: '',
  password: ''
})

const loginRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 计算属性：是否可以提交表单
const canSubmit = computed(() => {
  return loginForm.phone.length === 11 &&
         loginForm.password.length >= 6 &&
         /^1[3-9]\d{9}$/.test(loginForm.phone) &&
         !loading.value
})

// 页面初始化
onMounted(() => {
  const rememberedPhone = localStorage.getItem('rememberedPhone')
  if (rememberedPhone) {
    loginForm.phone = rememberedPhone
    rememberMe.value = true
  }

  setTimeout(() => {
    pageEntered.value = true
  }, 100)
})

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true
    await userStore.loginAction(loginForm)

    if (rememberMe.value) {
      localStorage.setItem('rememberedPhone', loginForm.phone)
    } else {
      localStorage.removeItem('rememberedPhone')
    }
  } catch (error: any) {
  } finally {
    loading.value = false
  }
}

// 处理回车键登录
const handleEnterKey = async () => {
  if (loading.value) return
  await handleLogin()
}

// 忘记密码
const handleForgotPassword = () => {
  forgotPasswordVisible.value = true
}

// 忘记密码成功处理
const handlePasswordResetSuccess = () => {
  ElMessage.success('密码重置成功，请使用新密码登录')
  forgotPasswordVisible.value = false
}

// 关闭忘记密码对话框
const handleCloseForgotPassword = () => {
  forgotPasswordVisible.value = false
}
</script>

<style scoped>
.login-container {
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

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-box {
  width: 100%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.page-entered .login-box {
  opacity: 1;
  transform: translateY(0);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
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

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid transparent;
  box-shadow: none;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 48px;
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: rgba(64, 158, 255, 0.3);
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
  color: #2c3e50;
  height: 48px;
}

.form-item-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.form-options :deep(.el-checkbox__label) {
  color: #606266;
}

.forgot-btn {
  font-size: 14px;
  color: #409EFF;
}

.submit-button {
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

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #53a8ff 0%, #409EFF 100%);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
  position: relative;
}

.register-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #409EFF;
  transition: width 0.3s ease;
}

.register-link:hover::after {
  width: 100%;
}

.admin-entry {
  opacity: 0;
  transform: translateY(10px);
  animation: fadeIn Up 0.8s ease forwards 0.5s;
}

.admin-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  text-decoration: none;
  font-size: 13px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.admin-link:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #606266;
  transform: translateY(-1px);
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-box {
    padding: 32px 24px;
  }
  
  .welcome-text {
    font-size: 24px;
  }
}
</style>