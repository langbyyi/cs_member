<template>
  <admin-layout>
    <div class="change-password-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">
            <el-icon><Lock /></el-icon>
            修改密码
          </h1>
          <p class="page-description">为了您的账户安全，请定期更新密码</p>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="password-content">
        <el-row :gutter="24">
          <el-col :lg="12" :md="16" :sm="24" :offset-md="6" :offset-lg="6">
            <el-card class="password-card">
              <template #header>
                <div class="card-header">
                  <span>修改登录密码</span>
                </div>
              </template>

              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="120px"
                class="password-form"
              >
                <el-form-item label="当前密码" prop="currentPassword">
                  <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                    clearable
                  />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                    clearable
                  />
                  <div class="password-strength">
                    <div class="strength-label">密码强度：</div>
                    <div class="strength-bar">
                      <div
                        class="strength-fill"
                        :class="passwordStrengthClass"
                        :style="{ width: passwordStrengthWidth }"
                      ></div>
                    </div>
                    <span class="strength-text" :class="passwordStrengthClass">
                      {{ passwordStrengthText }}
                    </span>
                  </div>
                </el-form-item>

                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                    clearable
                  />
                </el-form-item>

                <el-form-item>
                  <div class="password-requirements">
                    <h4>密码要求：</h4>
                    <ul>
                      <li :class="{ 'requirement-met': meetsLength }">
                        <el-icon><Check v-if="meetsLength" /><Close v-else /></el-icon>
                        至少8个字符
                      </li>
                      <li :class="{ 'requirement-met': meetsUppercase }">
                        <el-icon><Check v-if="meetsUppercase" /><Close v-else /></el-icon>
                        至少1个大写字母
                      </li>
                      <li :class="{ 'requirement-met': meetsLowercase }">
                        <el-icon><Check v-if="meetsLowercase" /><Close v-else /></el-icon>
                        至少1个小写字母
                      </li>
                      <li :class="{ 'requirement-met': meetsNumber }">
                        <el-icon><Check v-if="meetsNumber" /><Close v-else /></el-icon>
                        至少1个数字
                      </li>
                      <li :class="{ 'requirement-met': meetsSpecial }">
                        <el-icon><Check v-if="meetsSpecial" /><Close v-else /></el-icon>
                        至少1个特殊字符 (!@#$%^&*)
                      </li>
                    </ul>
                  </div>
                </el-form-item>

                <el-form-item>
                  <div class="form-actions">
                    <el-button @click="handleCancel">取消</el-button>
                    <el-button
                      type="primary"
                      @click="handleSubmit"
                      :loading="submitting"
                      :disabled="!canSubmit"
                    >
                      修改密码
                    </el-button>
                  </div>
                </el-form-item>
              </el-form>
            </el-card>

            <!-- 安全提示卡片 -->
            <el-card class="security-tips">
              <template #header>
                <div class="card-header">
                  <el-icon><InfoFilled /></el-icon>
                  <span>安全提示</span>
                </div>
              </template>

              <div class="tips-content">
                <div class="tip-item">
                  <el-icon class="tip-icon"><InfoFilled /></el-icon>
                  <div class="tip-text">
                    <strong>定期更换密码：</strong>建议每3个月更换一次密码，确保账户安全。
                  </div>
                </div>
                <div class="tip-item">
                  <el-icon class="tip-icon"><InfoFilled /></el-icon>
                  <div class="tip-text">
                    <strong>使用强密码：</strong>包含大小写字母、数字和特殊字符的组合。
                  </div>
                </div>
                <div class="tip-item">
                  <el-icon class="tip-icon"><Warning /></el-icon>
                  <div class="tip-text">
                    <strong>不要重复使用密码：</strong>避免使用与其他网站相同的密码。
                  </div>
                </div>
                <div class="tip-item">
                  <el-icon class="tip-icon"><CircleCheck /></el-icon>
                  <div class="tip-text">
                    <strong>启用双因素认证：</strong>如有可能，请启用额外的安全验证。
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import {
  Lock,
  Check,
  Close,
  InfoFilled,
  Warning,
  CircleCheck
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const passwordFormRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度计算
const passwordStrength = computed(() => {
  const password = passwordForm.newPassword
  let strength = 0

  if (password.length >= 8) strength++
  if (/[a-z]/.test(password)) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^A-Za-z0-9]/.test(password)) strength++

  return strength
})

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 2) return 'weak'
  if (strength <= 3) return 'medium'
  return 'strong'
})

const passwordStrengthWidth = computed(() => {
  const strength = passwordStrength.value
  return `${(strength / 5) * 100}%`
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 2) return '弱'
  if (strength <= 3) return '中等'
  return '强'
})

// 密码要求检查
const meetsLength = computed(() => passwordForm.newPassword.length >= 8)
const meetsUppercase = computed(() => /[A-Z]/.test(passwordForm.newPassword))
const meetsLowercase = computed(() => /[a-z]/.test(passwordForm.newPassword))
const meetsNumber = computed(() => /[0-9]/.test(passwordForm.newPassword))
const meetsSpecial = computed(() => /[^A-Za-z0-9]/.test(passwordForm.newPassword))

const canSubmit = computed(() => {
  return meetsLength.value &&
         meetsUppercase.value &&
         meetsLowercase.value &&
         meetsNumber.value &&
         meetsSpecial.value &&
         passwordForm.currentPassword &&
         passwordForm.newPassword === passwordForm.confirmPassword
})

// 表单验证规则
const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }

        if (!/[a-zA-Z]/.test(value)) {
          callback(new Error('密码必须包含至少1个字母'))
          return
        }

        if (!/[0-9]/.test(value)) {
          callback(new Error('密码必须包含至少1个数字'))
          return
        }

        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const handleSubmit = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    submitting.value = true

    // 调用修改密码接口
    await userStore.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功')

    // 清空表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    // 跳转回个人中心
    router.push('/admin/profile')

  } catch (error) {
    ElMessage.error('密码修改失败，请检查当前密码是否正确')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('确定要取消修改密码吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '继续编辑',
    type: 'warning'
  }).then(() => {
    router.push('/admin/profile')
  })
}
</script>

<style scoped>
.change-password-container {
  padding: 24px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-description {
  margin: 8px 0 0 44px;
  color: #64748b;
  font-size: 14px;
}

.password-content {
  margin-top: 24px;
}

.password-card {
  margin-bottom: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.password-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

.password-form {
  margin-top: 24px;
  padding: 0 24px 24px;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.strength-label {
  font-size: 14px;
  color: #64748b;
  min-width: 80px;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 3px;
  transition: all 0.3s ease;
}

.strength-fill.weak {
  background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
}

.strength-fill.medium {
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
}

.strength-fill.strong {
  background: linear-gradient(90deg, #22c55e 0%, #16a34a 100%);
}

.strength-text {
  font-size: 14px;
  font-weight: 600;
  min-width: 40px;
}

.strength-text.weak {
  color: #ef4444;
}

.strength-text.medium {
  color: #f59e0b;
}

.strength-text.strong {
  color: #22c55e;
}

.password-requirements {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.password-requirements h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.password-requirements ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.password-requirements li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
  transition: color 0.2s ease;
}

.password-requirements li.requirement-met {
  color: #22c55e;
}

.password-requirements .el-icon {
  font-size: 16px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.security-tips {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.tip-item:hover {
  background: #f8fafc;
  transform: translateX(4px);
}

.tip-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.tip-text {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
}

.tip-text strong {
  color: #1e293b;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .change-password-container {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .page-description {
    margin-left: 0;
  }

  .password-strength {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .strength-label {
    min-width: auto;
  }

  .form-actions {
    flex-direction: column;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .password-card,
  .tip-item,
  .strength-fill {
    animation: none !important;
    transition: none !important;
  }
}
</style>