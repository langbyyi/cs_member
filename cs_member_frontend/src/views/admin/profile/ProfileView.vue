<template>
  <admin-layout>
    <div class="profile-container">
      <!-- 页面头部 -->
      <div class="profile-header">
        <div class="header-content">
          <h1 class="page-title">
            <el-icon><User /></el-icon>
            个人中心
          </h1>
          <p class="page-description">管理您的个人信息和账户设置</p>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="profile-content">
        <el-row :gutter="24">
          <!-- 左侧个人信息卡片 -->
          <el-col :lg="8" :md="10" :sm="24">
            <el-card class="profile-card">
              <template #header>
                <div class="card-header">
                  <span>个人信息</span>
                </div>
              </template>

              <div class="profile-avatar">
                <el-avatar :size="120" :src="userInfo?.avatar" class="avatar">
                  {{ userInfo?.realName?.charAt(0) || 'A' }}
                </el-avatar>
                <div class="avatar-actions">
                  <el-button type="primary" size="small" @click="handleAvatarUpload">
                    <el-icon><Upload /></el-icon>
                    更换头像
                  </el-button>
                </div>
              </div>

              <div class="profile-info">
                <div class="info-item">
                  <label>真实姓名</label>
                  <span>{{ userInfo?.realName || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>用户名</label>
                  <span>{{ userInfo?.username || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>手机号码</label>
                  <span>{{ userInfo?.phone || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱地址</label>
                  <span>{{ userInfo?.email || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>用户角色</label>
                  <el-tag :type="getRoleTagType(userInfo?.role)">
                    {{ getRoleText(userInfo?.role) }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <label>注册时间</label>
                  <span>{{ formatDate(userInfo?.createdTime) }}</span>
                </div>
              </div>
            </el-card>

            <!-- 账户状态卡片 -->
            <el-card class="profile-card status-card">
              <template #header>
                <div class="card-header">
                  <span>账户状态</span>
                </div>
              </template>

              <div class="status-list">
                <div class="status-item">
                  <el-icon class="status-icon success"><CircleCheck /></el-icon>
                  <span>账户状态</span>
                  <el-tag type="success" size="small">正常</el-tag>
                </div>
                <div class="status-item">
                  <el-icon class="status-icon"><Lock /></el-icon>
                  <span>登录密码</span>
                  <el-button link size="small" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </div>
                <div class="status-item">
                  <el-icon class="status-icon"><Bell /></el-icon>
                  <span>消息通知</span>
                  <el-switch v-model="notificationsEnabled" />
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧编辑表单 -->
          <el-col :lg="16" :md="14" :sm="24">
            <el-card class="profile-form-card">
              <template #header>
                <div class="card-header">
                  <span>编辑个人信息</span>
                  <div class="header-actions">
                    <el-button @click="handleReset">重置</el-button>
                    <el-button type="primary" @click="handleSave" :loading="saving">
                      保存修改
                    </el-button>
                  </div>
                </div>
              </template>

              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="真实姓名" prop="realName">
                  <el-input
                    v-model="profileForm.realName"
                    placeholder="请输入真实姓名"
                    :disabled="!editing"
                  />
                </el-form-item>

                <el-form-item label="用户名" prop="username">
                  <el-input
                    v-model="profileForm.username"
                    placeholder="请输入用户名"
                    :disabled="true"
                  />
                  <div class="form-tip">用户名不可修改</div>
                </el-form-item>

                <el-form-item label="手机号码" prop="phone">
                  <el-input
                    v-model="profileForm.phone"
                    placeholder="请输入手机号码"
                    :disabled="!editing"
                  />
                </el-form-item>

                <el-form-item label="邮箱地址" prop="email">
                  <el-input
                    v-model="profileForm.email"
                    placeholder="请输入邮箱地址"
                    :disabled="!editing"
                  />
                </el-form-item>

                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.gender" :disabled="!editing">
                    <el-radio value="MALE">男</el-radio>
                    <el-radio value="FEMALE">女</el-radio>
                    <el-radio value="UNKNOWN">保密</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="生日">
                  <el-date-picker
                    v-model="profileForm.birthday"
                    type="date"
                    placeholder="选择生日"
                    :disabled="!editing"
                    style="width: 100%"
                  />
                </el-form-item>

                <el-form-item label="个人简介">
                  <el-input
                    v-model="profileForm.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入个人简介"
                    :disabled="!editing"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
              </el-form>

              <!-- 编辑模式切换 -->
              <div class="edit-actions" v-if="!editing">
                <el-button type="primary" @click="handleEdit">
                  <el-icon><Edit /></el-icon>
                  编辑信息
                </el-button>
              </div>
            </el-card>

            <!-- 活动记录 -->
            <el-card class="activity-card">
              <template #header>
                <div class="card-header">
                  <span>最近活动</span>
                  <el-button link @click="loadActivityLog">查看全部</el-button>
                </div>
              </template>

              <div class="activity-list">
                <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
                  <div class="activity-icon">
                    <el-icon :class="getActivityIconClass(activity.type)">
                      <component :is="getActivityIcon(activity.type)" />
                    </el-icon>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ activity.title }}</div>
                    <div class="activity-desc">{{ activity.description }}</div>
                    <div class="activity-time">{{ formatTime(activity.time) }}</div>
                  </div>
                </div>
                <div v-if="recentActivities.length === 0" class="empty-activity">
                  <el-empty description="暂无活动记录" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="avatarDialogVisible" title="更换头像" width="400px">
      <div class="avatar-upload">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar v-if="profileForm.avatar" :size="120" :src="profileForm.avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">
          支持 JPG、PNG 格式，文件大小不超过 2MB
        </div>
      </div>
    </el-dialog>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import {
  User,
  Upload,
  CircleCheck,
  Lock,
  Bell,
  Edit,
  Plus,
  Right,
  Setting,
  Document,
  UserFilled,
  Key
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 响应式数据
const editing = ref(false)
const saving = ref(false)
const notificationsEnabled = ref(true)
const avatarDialogVisible = ref(false)
const profileFormRef = ref<FormInstance>()

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 表单数据
const profileForm = reactive({
  realName: '',
  username: '',
  phone: '',
  email: '',
  gender: 'UNKNOWN',
  birthday: null,
  bio: '',
  avatar: ''
})

// 表单验证规则
const profileRules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 最近活动记录
const recentActivities = ref([
  {
    id: 1,
    type: 'login',
    title: '登录系统',
    description: '从 IP 地址 192.168.1.100 登录',
    time: new Date(Date.now() - 30 * 60 * 1000)
  },
  {
    id: 2,
    type: 'setting',
    title: '修改个人信息',
    description: '更新了手机号码',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000)
  },
  {
    id: 3,
    type: 'document',
    title: '查看报表',
    description: '查看了会员统计报表',
    time: new Date(Date.now() - 24 * 60 * 60 * 1000)
  }
])

// 上传配置
const uploadUrl = '/api/v1/upload/avatar'
const uploadHeaders = computed(() => {
  const token = userStore.token
  return {
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
})

// 方法
const initializeForm = () => {
  if (userInfo.value) {
    Object.assign(profileForm, {
      realName: userInfo.value.realName || '',
      username: userInfo.value.username || '',
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || '',
      gender: userInfo.value.gender || 'UNKNOWN',
      birthday: userInfo.value.birthday ? new Date(userInfo.value.birthday) : null,
      bio: userInfo.value.bio || '',
      avatar: userInfo.value.avatar || ''
    })
  }
}

const handleEdit = () => {
  editing.value = true
}

const handleReset = () => {
  initializeForm()
  editing.value = false
  ElMessage.success('已重置表单')
}

const handleSave = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    saving.value = true

    // 调用更新接口
    await userStore.updateUserInfo(profileForm)

    editing.value = false
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleAvatarUpload = () => {
  avatarDialogVisible.value = true
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    profileForm.avatar = response.data.url
    avatarDialogVisible.value = false
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

const handleChangePassword = () => {
  // 跳转到修改密码页面
  ElMessageBox.confirm('确定要跳转到修改密码页面吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 这里可以添加路由跳转逻辑
    ElMessage.info('跳转到修改密码页面')
  })
}

const loadActivityLog = () => {
  ElMessage.info('加载更多活动记录')
}

// 工具方法
const getRoleTagType = (role?: string) => {
  const roleTypes: Record<string, string> = {
    SYSTEM_ADMIN: 'danger',
    STORE_ADMIN: 'warning',
    CLERK: 'info',
    MEMBER: 'success'
  }
  return roleTypes[role || ''] || 'info'
}

const getRoleText = (role?: string) => {
  const roleTexts: Record<string, string> = {
    SYSTEM_ADMIN: '系统管理员',
    STORE_ADMIN: '门店管理员',
    CLERK: '店员',
    MEMBER: '会员'
  }
  return roleTexts[role || ''] || '未知'
}

const getActivityIcon = (type: string) => {
  const icons: Record<string, any> = {
    login: Key,
    setting: Setting,
    document: Document,
    user: UserFilled
  }
  return icons[type] || Setting
}

const getActivityIconClass = (type: string) => {
  const classes: Record<string, string> = {
    login: 'success',
    setting: 'primary',
    document: 'info',
    user: 'warning'
  }
  return classes[type] || 'primary'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  initializeForm()
})
</script>

<style scoped>
.profile-container {
  padding: 24px;
  min-height: calc(100vh - 120px);
}

.profile-header {
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

.profile-content {
  margin-top: 24px;
}

.profile-card {
  margin-bottom: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.profile-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.profile-avatar {
  text-align: center;
  margin-bottom: 24px;
}

.avatar {
  border: 4px solid #fff;
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.2);
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(99, 102, 241, 0.3);
}

.avatar-actions {
  margin-top: 16px;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #374151;
  min-width: 80px;
}

.info-item span {
  color: #64748b;
  flex: 1;
  text-align: right;
}

.status-card {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.status-item:hover {
  background: #f8fafc;
}

.status-icon {
  font-size: 16px;
}

.status-icon.success {
  color: #22c55e;
}

.status-icon.primary {
  color: #6366f1;
}

.profile-form-card {
  min-height: 600px;
}

.profile-form {
  margin-top: 24px;
}

.form-tip {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.edit-actions {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.activity-card {
  margin-top: 24px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.activity-item:hover {
  background: #e2e8f0;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
  flex-shrink: 0;
}

.activity-icon.success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

.activity-icon.primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.activity-icon.info {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
}

.activity-icon.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.activity-desc {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 4px;
}

.activity-time {
  color: #94a3b8;
  font-size: 12px;
}

.empty-activity {
  padding: 40px 0;
  text-align: center;
}

.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  display: inline-block;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-uploader:hover {
  border-color: #6366f1;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.upload-tip {
  margin-top: 16px;
  color: #64748b;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
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

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .info-item span {
    text-align: left;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .profile-card,
  .avatar,
  .status-item,
  .activity-item,
  .avatar-uploader {
    animation: none !important;
    transition: none !important;
  }
}
</style>