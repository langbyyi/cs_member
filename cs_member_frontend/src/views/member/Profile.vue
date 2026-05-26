<template>
  <div class="profile-container">
    <div class="profile-layout">
      <!-- 左侧个人信息卡片 -->
      <div class="left-column">
        <el-card class="user-card" shadow="hover">
          <div class="user-header">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :icon="UserFilled" class="member-avatar" />
              <div class="vip-badge-wrapper">
                <div
                  class="custom-vip-tag"
                  :style="memberLevelStyle"
                >
                  <el-icon class="level-icon"><Trophy /></el-icon>
                  {{ getMemberLevelText(memberInfo.memberLevel) || '普通会员' }}
                </div>
              </div>
            </div>
            <h2 class="user-name">{{ memberInfo.name || '未设置' }}</h2>
            <p class="user-id">ID: {{ memberInfo.memberNo || '未分配' }}</p>
          </div>
          
          <div class="user-actions">
            <el-button type="primary" class="edit-btn" :icon="Edit" @click="showEditDialog" round>
              编辑资料
            </el-button>
          </div>

          <div class="user-meta">
            <div class="meta-item">
              <span class="label">注册时间</span>
              <span class="value">{{ formatDate(memberInfo.createdTime) }}</span>
            </div>
            <div class="meta-item">
              <span class="label">状态</span>
              <el-tag :type="getStatusType(memberInfo.status)" size="small" effect="light" round>
                {{ getStatusText(memberInfo.status) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧统计与详情 -->
      <div class="right-column">
        <!-- 统计卡片网格 -->
        <div class="stats-grid">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon-wrapper points">
                <el-icon><Medal /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ memberInfo.currentPoints || 0 }}</div>
                <div class="stat-label">当前积分</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon-wrapper consumption">
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ memberInfo.totalConsumption || 0 }}</div>
                <div class="stat-label">累计消费</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon-wrapper balance">
                <el-icon><Wallet /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ memberInfo.balance || 0 }}</div>
                <div class="stat-label">账户余额</div>
              </div>
            </div>
          </el-card>

          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon-wrapper coupons">
                <el-icon><Ticket /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ availableCoupons || 0 }}</div>
                <div class="stat-label">可用优惠券</div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 详细信息卡片 -->
        <el-card class="details-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>详细资料</h3>
            </div>
          </template>
          
          <div class="details-grid">
            <div class="detail-item">
              <span class="detail-label">手机号码</span>
              <span class="detail-value">{{ memberInfo.phone || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">邮箱地址</span>
              <span class="detail-value">{{ memberInfo.email || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">性别</span>
              <span class="detail-value">{{ getGenderText(memberInfo.gender) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">生日</span>
              <span class="detail-value">{{ memberInfo.birthday || '未设置' }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">地址</span>
              <span class="detail-value">{{ memberInfo.address || '未设置' }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 编辑信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑个人信息"
      width="500px"
      :close-on-click-modal="false"
      class="custom-dialog"
      align-center
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
        class="edit-form"
      >
        <el-form-item label="用户名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="editForm.address"
            type="textarea"
            :rows="3"
            placeholder="请输入地址"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  Edit,
  Medal,
  ShoppingCart,
  Wallet,
  Ticket,
  Trophy
} from '@element-plus/icons-vue'
import { updateMember } from '@/api/member'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 页面刷新时获取最新会员信息
// 页面刷新时获取最新会员信息
onMounted(async () => {
})

// 会员信息 - 直接使用登录时获取的完整信息
const memberInfo = computed(() => ({
  name: userStore.userInfo?.name || '',
  memberNo: userStore.userInfo?.memberNo || '',
  phone: userStore.userInfo?.phone || '',
  email: userStore.userInfo?.email || '',
  gender: userStore.userInfo?.gender || 1,
  birthday: userStore.userInfo?.birthday || '',
  address: userStore.userInfo?.address || '',
  memberLevel: userStore.userInfo?.memberLevel || 1,
  status: userStore.userInfo?.status || 1,
  currentPoints: userStore.userInfo?.currentPoints || 0,
  totalPoints: userStore.userInfo?.totalPoints || 0,
  totalConsumption: userStore.userInfo?.totalConsumption || 0,
  balance: userStore.userInfo?.balance || 0,
  createdTime: userStore.userInfo?.createdTime || '',
  lastConsumptionTime: userStore.userInfo?.lastConsumptionTime || ''
}))

// 可用优惠券数量
const availableCoupons = ref(0)

// 编辑相关
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const saving = ref(false)

const editForm = reactive({
  name: '',
  gender: 1,
  birthday: '',
  address: ''
})

const editRules: FormRules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在2-50个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}


// 显示编辑对话框
const showEditDialog = () => {
  const info = memberInfo.value
  editForm.name = info.name || ''
  editForm.gender = info.gender || 1
  editForm.birthday = info.birthday || ''
  editForm.address = info.address || ''
  editDialogVisible.value = true
}

// 保存个人信息
const saveProfile = async () => {
  if (!editFormRef.value) return

  try {
    const valid = await editFormRef.value.validate()
    if (!valid) return

    saving.value = true

    const updateData = {
      name: editForm.name,
      gender: editForm.gender,
      birthday: editForm.birthday,
      address: editForm.address
    }

    await updateMember(userStore.user?.id || 0, updateData)

    // 更新本地用户信息
    if (userStore.user) {
      userStore.user.name = updateData.name
    }

    // 更新页面信息
    memberInfo.value.name = updateData.name
    memberInfo.value.gender = updateData.gender
    memberInfo.value.birthday = updateData.birthday
    memberInfo.value.address = updateData.address

    ElMessage.success('个人信息更新成功')
    editDialogVisible.value = false
  } catch (error: any) {
    // 错误已由全局拦截器处理
  } finally {
    saving.value = false
  }
}

// 获取会员等级文本 - 使用动态等级配置
const getMemberLevelText = (level: number) => {
  const gradeConfig = userStore.gradeConfigs.find((g: any) => g.sortOrder === level)
  return gradeConfig ? gradeConfig.gradeName : '普通会员'
}

// 获取会员等级类型 - 使用动态等级配置颜色
const getMemberLevelType = (level: number) => {
  // 默认类型，因为我们会用style覆盖颜色
  return 'info'
}

// 获取会员等级颜色
const getMemberLevelColor = (level: number) => {
  const gradeConfig = userStore.gradeConfigs.find((g: any) => g.sortOrder === level)
  return gradeConfig ? gradeConfig.color : '#909399'
}

// 计算会员等级样式
const memberLevelStyle = computed(() => {
  const color = getMemberLevelColor(memberInfo.value.memberLevel)
  return {
    backgroundColor: color,
    borderColor: color,
    color: '#fff',
    boxShadow: `0 4px 12px ${color}66`, // 动态阴影，带透明度
    fontWeight: 'bold',
    padding: '0 12px',
    height: '28px',
    fontSize: '13px',
    transition: 'all 0.3s ease'
  }
})

// 获取性别文本
const getGenderText = (gender: number) => {
  const genders = ['未设置', '男', '女']
  return genders[gender] || '未设置'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statuses = ['正常', '禁用', '黑名单']
  return statuses[status - 1] || '正常'
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types = ['success', 'warning', 'danger']
  return types[status - 1] || 'success'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '未设置'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

/* 左侧用户卡片 */
.user-card {
  text-align: center;
  border-radius: 16px;
  border: none;
  /* overflow: hidden; Removed to prevent clipping of absolute elements */
  transition: all 0.3s ease;
  position: relative; /* Ensure z-index works */
  z-index: 1;
}

.user-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.user-header {
  padding: 32px 20px;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px 16px 0 0; /* Add radius here since we removed overflow:hidden */
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 24px; /* Increased margin to make space for badge */
}

.member-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 4px solid #fff;
  box-shadow: 0 8px 24px rgba(118, 75, 162, 0.25);
}

.vip-badge-wrapper {
  position: absolute;
  bottom: -10px; /* Adjusted position */
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  z-index: 10; /* Increased z-index */
}

.custom-vip-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: 28px;
  border-radius: 14px; /* Round shape */
  font-size: 12px;
  color: #fff; /* Default text color */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.user-id {
  margin: 0;
  font-size: 13px;
  color: #909399;
  font-family: monospace;
}

.user-actions {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.edit-btn {
  width: 100%;
  height: 40px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.user-meta {
  padding: 24px 20px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.meta-item .label {
  font-size: 14px;
  color: #606266;
}

.meta-item .value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 右侧内容区 */
.right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  border-radius: 16px;
  border: none;
  transition: all 0.3s ease;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon-wrapper.points {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  box-shadow: 0 8px 16px rgba(253, 160, 133, 0.3);
}

.stat-icon-wrapper.consumption {
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  box-shadow: 0 8px 16px rgba(143, 211, 244, 0.3);
}

.stat-icon-wrapper.balance {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  box-shadow: 0 8px 16px rgba(251, 194, 235, 0.3);
}

.stat-icon-wrapper.coupons {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
  box-shadow: 0 8px 16px rgba(255, 154, 158, 0.3);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #2c3e50;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* 详细资料卡片 */
.details-card {
  flex: 1;
  border-radius: 16px;
  border: none;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  position: relative;
  padding-left: 12px;
}

.card-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #409eff 0%, #a0cfff 100%);
  border-radius: 2px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px 40px;
  padding: 12px 0;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item.full-width {
  grid-column: span 2;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.detail-value {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
  padding-bottom: 8px;
  border-bottom: 1px solid #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }

  .detail-item.full-width {
    grid-column: span 1;
  }
}
</style>