<template>
  <div class="member-settings">
    <div class="settings-container">
      <div class="page-header">
        <h1 class="page-title">
          <div class="title-icon">
            <el-icon><Setting /></el-icon>
          </div>
          账户设置
        </h1>
        <p class="page-description">管理您的账户信息和偏好设置</p>
      </div>

      <el-row :gutter="24">
        <el-col :span="6" :xs="24">
          <!-- 设置菜单 -->
          <div class="settings-menu">
            <el-card class="menu-card" shadow="never">
              <div class="menu-item"
                   v-for="item in menuItems"
                   :key="item.key"
                   :class="{ active: activeTab === item.key }"
                   @click="activeTab = item.key">
                <div class="menu-icon">
                  <el-icon><component :is="item.icon" /></el-icon>
                </div>
                <span class="menu-label">{{ item.label }}</span>
                <el-icon class="menu-arrow" v-if="activeTab === item.key"><ArrowRight /></el-icon>
              </div>
            </el-card>
          </div>
        </el-col>

        <el-col :span="18" :xs="24">
          <!-- 设置内容 -->
          <div class="settings-content">
            <!-- 个人信息设置 -->
            <transition name="fade-slide" mode="out-in">
              <div v-if="activeTab === 'profile'" key="profile" class="settings-section">
                <el-card class="content-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <div class="header-icon">
                        <el-icon><User /></el-icon>
                      </div>
                      <span>个人信息</span>
                    </div>
                  </template>

                  <el-form :model="profileForm" label-position="top" class="settings-form">
                    <div class="form-row">
                      <el-form-item label="头像" class="avatar-item">
                        <div class="avatar-upload">
                          <el-avatar :size="80" :src="profileForm.avatar" class="user-avatar">
                            <el-icon><UserFilled /></el-icon>
                          </el-avatar>
                          <div class="avatar-actions">
                            <el-button size="small" type="primary" round>更换头像</el-button>
                            <span class="avatar-tip">支持 JPG, PNG 格式，最大 2MB</span>
                          </div>
                        </div>
                      </el-form-item>
                    </div>

                    <div class="form-grid">
                      <el-form-item label="姓名">
                        <el-input v-model="profileForm.name" placeholder="请输入姓名" />
                      </el-form-item>

                      <el-form-item label="手机号">
                        <el-input v-model="profileForm.phone" disabled>
                          <template #append>
                            <el-button link>修改</el-button>
                          </template>
                        </el-input>
                      </el-form-item>

                      <el-form-item label="邮箱">
                        <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
                      </el-form-item>

                      <el-form-item label="生日">
                        <el-date-picker
                          v-model="profileForm.birthday"
                          type="date"
                          placeholder="选择生日"
                          format="YYYY-MM-DD"
                          value-format="YYYY-MM-DD"
                          style="width: 100%"
                        />
                      </el-form-item>
                    </div>

                    <el-form-item label="性别">
                      <el-radio-group v-model="profileForm.gender">
                        <el-radio-button label="MALE">男</el-radio-button>
                        <el-radio-button label="FEMALE">女</el-radio-button>
                        <el-radio-button label="OTHER">保密</el-radio-button>
                      </el-radio-group>
                    </el-form-item>

                    <div class="form-actions">
                      <el-button type="primary" @click="saveProfile" round size="large">保存修改</el-button>
                    </div>
                  </el-form>
                </el-card>
              </div>

              <!-- 安全设置 -->
              <div v-else-if="activeTab === 'security'" key="security" class="settings-section">
                <el-card class="content-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <div class="header-icon">
                        <el-icon><Lock /></el-icon>
                      </div>
                      <span>安全设置</span>
                    </div>
                  </template>

                  <div class="security-items">
                    <div class="security-item">
                      <div class="item-icon">
                        <el-icon><Key /></el-icon>
                      </div>
                      <div class="item-info">
                        <h4>登录密码</h4>
                        <p>定期更换密码，保护账户安全</p>
                      </div>
                      <el-button @click="changePassword" round>修改密码</el-button>
                    </div>

                    <div class="security-item">
                      <div class="item-icon">
                        <el-icon><Iphone /></el-icon>
                      </div>
                      <div class="item-info">
                        <h4>手机验证</h4>
                        <p>已绑定: {{ userStore.user?.phone || '未绑定' }}</p>
                      </div>
                      <el-button link type="primary">更换手机</el-button>
                    </div>

                    <div class="security-item">
                      <div class="item-icon">
                        <el-icon><Monitor /></el-icon>
                      </div>
                      <div class="item-info">
                        <h4>登录设备管理</h4>
                        <p>管理已登录的设备和会话</p>
                      </div>
                      <el-button link type="primary">查看设备</el-button>
                    </div>
                  </div>
                </el-card>
              </div>

              <!-- 通知设置 -->
              <div v-else-if="activeTab === 'notification'" key="notification" class="settings-section">
                <el-card class="content-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <div class="header-icon">
                        <el-icon><Bell /></el-icon>
                      </div>
                      <span>通知设置</span>
                    </div>
                  </template>

                  <div class="notification-items">
                    <div class="notification-item">
                      <div class="item-info">
                        <h4>消费通知</h4>
                        <p>接收消费记录和积分变动通知</p>
                      </div>
                      <el-switch v-model="notificationSettings.consumption" />
                    </div>

                    <div class="notification-item">
                      <div class="item-info">
                        <h4>优惠券通知</h4>
                        <p>接收优惠券到期和新券发放通知</p>
                      </div>
                      <el-switch v-model="notificationSettings.coupon" />
                    </div>

                    <div class="notification-item">
                      <div class="item-info">
                        <h4>促销活动</h4>
                        <p>接收门店促销和活动信息</p>
                      </div>
                      <el-switch v-model="notificationSettings.promotion" />
                    </div>

                    <div class="notification-item">
                      <div class="item-info">
                        <h4>系统消息</h4>
                        <p>接收系统维护和重要公告</p>
                      </div>
                      <el-switch v-model="notificationSettings.system" />
                    </div>
                  </div>

                  <div class="form-actions">
                    <el-button type="primary" @click="saveNotificationSettings" round size="large">保存设置</el-button>
                  </div>
                </el-card>
              </div>

              <!-- 隐私设置 -->
              <div v-else-if="activeTab === 'privacy'" key="privacy" class="settings-section">
                <el-card class="content-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <div class="header-icon">
                        <el-icon><Hide /></el-icon>
                      </div>
                      <span>隐私设置</span>
                    </div>
                  </template>

                  <div class="privacy-items">
                    <div class="privacy-item">
                      <div class="item-info">
                        <h4>消费记录可见性</h4>
                        <p>控制消费记录的展示范围</p>
                      </div>
                      <el-select v-model="privacySettings.consumptionVisibility" style="width: 140px">
                        <el-option label="仅自己可见" value="private" />
                        <el-option label="门店可见" value="store" />
                      </el-select>
                    </div>

                    <div class="privacy-item">
                      <div class="item-info">
                        <h4>个人资料展示</h4>
                        <p>控制个人信息的公开程度</p>
                      </div>
                      <el-switch v-model="privacySettings.showProfile" />
                    </div>

                    <div class="privacy-item">
                      <div class="item-info">
                        <h4>数据导出</h4>
                        <p>导出您的个人数据副本</p>
                      </div>
                      <el-button link type="primary">申请导出</el-button>
                    </div>

                    <div class="privacy-item danger-zone">
                      <div class="item-info">
                        <h4 class="text-danger">账户注销</h4>
                        <p>永久删除您的账户和所有相关数据</p>
                      </div>
                      <el-button type="danger" plain @click="deleteAccount" round>申请注销</el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </transition>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      align-center
      class="custom-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="submitPasswordChange" :loading="passwordLoading" round>
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Setting,
  User,
  UserFilled,
  Lock,
  Bell,
  ArrowRight,
  Key,
  Iphone,
  Monitor,
  Hide
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 当前激活的设置标签
const activeTab = ref('profile')

// 设置菜单项
const menuItems = [
  { key: 'profile', label: '个人信息', icon: 'User' },
  { key: 'security', label: '安全设置', icon: 'Lock' },
  { key: 'notification', label: '通知设置', icon: 'Bell' },
  { key: 'privacy', label: '隐私设置', icon: 'Hide' }
]

// 个人信息表单
const profileForm = reactive({
  name: '',
  phone: '',
  email: '',
  birthday: '',
  gender: 'MALE',
  avatar: ''
})

// 通知设置
const notificationSettings = reactive({
  consumption: true,
  coupon: true,
  promotion: false,
  system: true
})

// 隐私设置
const privacySettings = reactive({
  consumptionVisibility: 'private',
  showProfile: true
})

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
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

// 保存个人信息
const saveProfile = async () => {
  try {
    const { updateCurrentMemberProfile } = await import('@/api/member-profile')
    await updateCurrentMemberProfile(profileForm)
    ElMessage.success('个人信息保存成功')
  } catch (error) {
    // 错误已由全局拦截器处理
  }
}

// 修改密码
const changePassword = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    const { changePassword: changePasswordApi } = await import('@/api/member-profile')
    await changePasswordApi({
      oldPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })

    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error) {
    // 错误已由全局拦截器处理
  } finally {
    passwordLoading.value = false
  }
}

// 保存通知设置
const saveNotificationSettings = async () => {
  try {
    ElMessage.success('通知设置保存成功')
  } catch (error) {
    // 错误已由全局拦截器处理
  }
}

// 申请注销账户
const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '注销账户将永久删除您的所有数据，此操作不可恢复。确定要继续吗？',
      '申请注销账户',
      {
        confirmButtonText: '确定注销',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    ElMessage.success('注销申请已提交，我们将在3个工作日内处理')
  } catch (error) {
    // 用户取消
  }
}

// 初始化表单数据
onMounted(() => {
  if (userStore.user) {
    profileForm.name = userStore.user.name || ''
    profileForm.phone = userStore.user.phone || ''
    profileForm.email = userStore.user.email || ''
  }
})
</script>

<style scoped>
.member-settings {
  padding: 32px 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.settings-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
  padding-left: 52px;
}

/* 设置菜单 */
.settings-menu {
  position: sticky;
  top: 24px;
}

.menu-card {
  border-radius: 16px;
  border: none;
  background: #fff;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #606266;
  font-weight: 500;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.menu-item.active {
  background: #f0f9ff;
  color: #409eff;
  border-left-color: #409eff;
}

.menu-icon {
  font-size: 18px;
  display: flex;
  align-items: center;
}

.menu-label {
  flex: 1;
}

.menu-arrow {
  font-size: 14px;
  opacity: 0.5;
}

/* 设置内容 */
.settings-content {
  min-height: 500px;
}

.content-card {
  border-radius: 16px;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.header-icon {
  width: 32px;
  height: 32px;
  background: #ecf5ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 16px;
}

/* 表单样式 */
.settings-form {
  padding: 10px 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}

/* 安全设置 */
.security-items,
.notification-items,
.privacy-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item,
.notification-item,
.privacy-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.security-item:hover,
.notification-item:hover,
.privacy-item:hover {
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.item-icon {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #606266;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.item-info {
  flex: 1;
}

.item-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.item-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.text-danger {
  color: #f56c6c !important;
}

.danger-zone {
  background: #fef0f0;
}

.danger-zone:hover {
  background: #fde2e2;
}

.danger-zone .item-icon {
  color: #f56c6c;
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .member-settings {
    padding: 16px;
  }

  .settings-menu {
    position: static;
    margin-bottom: 24px;
    overflow-x: auto;
  }
  
  .menu-card {
    display: flex;
    padding: 4px;
    border-radius: 12px;
  }
  
  .menu-item {
    flex: 1;
    justify-content: center;
    padding: 12px;
    border-left: none;
    border-bottom: 3px solid transparent;
    border-radius: 8px;
    white-space: nowrap;
  }
  
  .menu-item.active {
    border-bottom-color: #409eff;
    background: #f0f9ff;
  }
  
  .menu-arrow {
    display: none;
  }
  
  .menu-label {
    display: none;
  }
  
  .menu-icon {
    font-size: 20px;
    margin: 0;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .security-item,
  .notification-item,
  .privacy-item {
    padding: 16px;
  }
  
  .item-icon {
    display: none;
  }
}
</style>