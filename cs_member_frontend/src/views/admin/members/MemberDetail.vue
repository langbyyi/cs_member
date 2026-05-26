<template>
  <div class="member-detail" v-loading="loading">
    <div class="page-header">
      <div class="header-actions">
        <h1 class="page-title">会员详情</h1>
        <div class="action-buttons">
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </div>
    </div>

    <!-- 基本信息 -->
    <el-card class="member-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><User /></el-icon>
            基本信息
          </span>
          <el-tag :type="getMemberStatusType(memberInfo.status)" size="large">
            {{ getMemberStatusLabel(memberInfo.status) }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <!-- 第一行 -->
        <el-descriptions-item label="会员编号">
          <span class="member-no">{{ memberInfo.memberNo }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          <span class="member-name">{{ memberInfo.name }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          <span class="member-phone">{{ memberInfo.phone }}</span>
        </el-descriptions-item>
        <!-- 第二行 -->
        <el-descriptions-item label="性别">
          {{ getGenderLabel(memberInfo.gender) }}
        </el-descriptions-item>
        <el-descriptions-item label="生日">
          {{ memberInfo.birthday || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ memberInfo.email || '-' }}
        </el-descriptions-item>
        <!-- 第三行 -->
        <el-descriptions-item label="会员等级">
          <div
            style="
              display: inline-block;
              padding: 4px 12px;
              font-size: 12px;
              font-weight: 600;
              text-align: center;
              min-width: 60px;
              border-radius: 4px;
              line-height: 1.4;
            "
            :style="{
              background: memberInfo.memberLevel === 1 ? '#f3f4f6' :
                        memberInfo.memberLevel === 2 ? '#dbeafe' :
                        memberInfo.memberLevel === 3 ? '#fef3c7' :
                        '#fce7f3',
              color: memberInfo.memberLevel === 1 ? '#374151' :
                    memberInfo.memberLevel === 2 ? '#1e40af' :
                    memberInfo.memberLevel === 3 ? '#92400e' :
                    '#9f1239',
              border: `1px solid ${
                memberInfo.memberLevel === 1 ? '#d1d5db' :
                memberInfo.memberLevel === 2 ? '#93c5fd' :
                memberInfo.memberLevel === 3 ? '#fcd34d' :
                '#f9a8d4'
              }`
            }"
          >
            {{ getMemberLevelText(memberInfo.memberLevel) }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="注册门店">
          {{ memberInfo.registerStoreName || '自主注册' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatDateTime(memberInfo.createdTime) }}
        </el-descriptions-item>
        <!-- 第四行 -->
        <el-descriptions-item label="当前积分">
          <span class="points-value">{{ memberInfo.currentPoints }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="账户余额">
          <span class="balance-value">¥{{ memberInfo.balance }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="最后消费">
          {{ memberInfo.lastConsumptionTime ? formatDateTime(memberInfo.lastConsumptionTime) : '暂无' }}
        </el-descriptions-item>
        <!-- 第五行 -->
        <el-descriptions-item label="累计积分">
          <span class="total-points">{{ memberInfo.totalPoints }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="累计消费">
          <span class="consumption-value">¥{{ memberInfo.totalConsumption }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="消费次数">
          <span class="consumption-count">{{ memberInfo.consumptionCount || 0 }}次</span>
        </el-descriptions-item>
        <!-- 地址单独占一行 -->
        <el-descriptions-item label="地址" span="3" v-if="memberInfo.address">
          {{ memberInfo.address }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 统计信息 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="24"><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <h3 class="stat-value">{{ memberInfo.currentPoints }}</h3>
            <p class="stat-label">当前积分</p>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="24"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <h3 class="stat-value">¥{{ memberInfo.balance }}</h3>
            <p class="stat-label">账户余额</p>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="24"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <h3 class="stat-value">{{ memberInfo.consumptionCount || 0 }}</h3>
            <p class="stat-label">消费次数</p>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <h3 class="stat-value">¥{{ memberInfo.totalConsumption }}</h3>
            <p class="stat-label">累计消费</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作 -->
    <el-card class="quick-actions">
      <template #header>
        <span class="header-title">
          <el-icon><Operation /></el-icon>
          快捷操作
        </span>
      </template>

      <div class="action-grid">
        <el-button type="primary" @click="handleAdjustPoints">
          <el-icon><Coin /></el-icon>
          调整积分
        </el-button>
        <el-button type="success" @click="handleAdjustBalance">
          <el-icon><Money /></el-icon>
          调整余额
        </el-button>
        <el-button type="warning" @click="handleViewConsumption">
          <el-icon><ShoppingCart /></el-icon>
          消费记录
        </el-button>
        <el-button type="info" @click="handleViewPoints">
          <el-icon><Medal /></el-icon>
          积分明细
        </el-button>
        <el-button @click="handleViewCoupons">
          <el-icon><Ticket /></el-icon>
          优惠券
        </el-button>
        <el-button @click="handleToggleStatus">
          <el-icon><Switch /></el-icon>
          {{ getToggleStatusText(memberInfo.status) }}
        </el-button>
      </div>
    </el-card>
  </div>

  <!-- 积分调整对话框 -->
  <el-dialog
    v-model="pointsDialogVisible"
    title="积分调整"
    width="500px"
    :before-close="handleClosePointsDialog"
  >
    <el-form
      ref="pointsFormRef"
      :model="pointsForm"
      :rules="pointsRules"
      label-width="100px"
    >
      <el-form-item label="会员信息">
        <div class="member-info">
          <span>{{ memberInfo.name }} ({{ memberInfo.memberNo }})</span>
          <span class="current-points">当前积分: {{ memberInfo.currentPoints }}</span>
        </div>
      </el-form-item>
      <el-form-item label="调整类型" prop="type">
        <el-radio-group v-model="pointsForm.type">
          <el-radio :value="1">增加积分</el-radio>
          <el-radio :value="2">减少积分</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="积分数" prop="points">
        <el-input-number
          v-model="pointsForm.points"
          :min="1"
          :max="10000"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="调整原因" prop="reason">
        <el-input
          v-model="pointsForm.reason"
          type="textarea"
          :rows="3"
          placeholder="请输入积分调整原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pointsDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="pointsSubmitting" @click="handleSubmitPoints">
        确定
      </el-button>
    </template>
  </el-dialog>

  <!-- 余额调整对话框 -->
  <el-dialog
    v-model="balanceDialogVisible"
    title="余额调整"
    width="500px"
  >
    <el-form
      ref="balanceFormRef"
      :model="balanceForm"
      :rules="balanceRules"
      label-width="100px"
    >
      <el-form-item label="会员信息">
        <div class="member-info">
          <span>{{ memberInfo.name }} ({{ memberInfo.memberNo }})</span>
          <span class="current-points">当前余额: {{ memberInfo.balance }}</span>
        </div>
      </el-form-item>
      <el-form-item label="调整类型" prop="type">
        <el-radio-group v-model="balanceForm.type">
          <el-radio :value="1">增加余额</el-radio>
          <el-radio :value="2">减少余额</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number
          v-model="balanceForm.amount"
          :min="0.01"
          :max="99999"
          :precision="2"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="调整原因" prop="reason">
        <el-input
          v-model="balanceForm.reason"
          type="textarea"
          :rows="3"
          placeholder="请输入余额调整原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="balanceDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="balanceSubmitting" @click="handleSubmitBalance">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getMemberDetail, updateMemberStatus, updateMemberPoints, updateMemberBalance, type Member } from '@/api/member'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const memberInfo = ref<Member>({
  id: 0,
  memberNo: '',
  name: '',
  phone: '',
  gender: 1,
  email: '',
  address: '',
  memberLevel: 1,
  totalConsumption: 0,
  totalPoints: 0,
  currentPoints: 0,
  balance: 0,
  status: 1,
  createdTime: '',
  updatedTime: '',
  lastConsumptionTime: '',
  lastLoginTime: '',
  lastLoginIp: '',
  loginCount: 0,
  consumptionCount: 0
})
const pointsDialogVisible = ref(false)
const pointsSubmitting = ref(false)
const pointsFormRef = ref<FormInstance>()

// 积分调整表单
const pointsForm = reactive({
  type: 1,
  points: 0,
  reason: ''
})

// 积分调整表单验证规则
const pointsRules: FormRules = {
  type: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  points: [
    { required: true, message: '请输入积分数', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '积分数在1-10000之间', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' },
    { min: 2, max: 100, message: '调整原因在2-100字符之间', trigger: 'blur' }
  ]
}

// 余额调整
const balanceDialogVisible = ref(false)
const balanceSubmitting = ref(false)
const balanceFormRef = ref<FormInstance>()
const balanceForm = reactive({
  type: 1,
  amount: 0.01,
  reason: ''
})
const balanceRules: FormRules = {
  type: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' },
    { min: 2, max: 100, message: '调整原因在2-100字符之间', trigger: 'blur' }
  ]
}

// 获取会员等级背景色（与列表页保持一致）
function getMemberLevelColor(level: number) {
  const colorMap: Record<number, string> = {
    1: '#f3f4f6',  // 普通会员 - 灰色
    2: '#dbeafe',  // 银卡会员 - 蓝色
    3: '#fef3c7',  // 金卡会员 - 黄色
    4: '#fce7f3'   // 钻石会员 - 粉色
  }
  return colorMap[level] || '#f3f4f6'
}

// 获取会员等级文字颜色（与列表页保持一致）
function getMemberLevelTextColor(level: number) {
  const colorMap: Record<number, string> = {
    1: '#374151',  // 普通会员 - 深灰色
    2: '#1e40af',  // 银卡会员 - 深蓝色
    3: '#92400e',  // 金卡会员 - 深橙色
    4: '#9f1239'   // 钻石会员 - 深粉色
  }
  return colorMap[level] || '#374151'
}

// 获取会员等级文字
function getMemberLevelText(level: number) {
  const textMap: Record<number, string> = {
    1: '普通会员',
    2: '银卡会员',
    3: '金卡会员',
    4: '钻石会员'
  }
  return textMap[level] || '普通会员'
}

// 获取会员状态标签类型
function getMemberStatusType(status: number) {
  const typeMap: Record<number, string> = {
    0: 'danger',    // 禁用 - 红色
    1: 'success',   // 正常 - 绿色
    2: 'warning'    // 冻结 - 橙色
  }
  return typeMap[status] || 'info'
}

// 获取会员状态标签文本
function getMemberStatusLabel(status: number) {
  const labelMap: Record<number, string> = {
    0: '禁用',      // 禁用
    1: '正常',      // 正常
    2: '冻结'       // 冻结
  }
  return labelMap[status] || '未知'
}

// 获取切换状态文本
function getToggleStatusText(status: number) {
  const textMap: Record<number, string> = {
    0: '启用',      // 当前是禁用，切换为启用
    1: '禁用',      // 当前是正常，切换为禁用
    2: '启用'       // 当前是冻结，切换为启用
  }
  return textMap[status] || '启用'
}

// 获取性别标签文本
function getGenderLabel(gender: number) {
  const labelMap: Record<number, string> = {
    1: '男',
    2: '女'
  }
  return labelMap[gender] || '未知'
}

// 格式化日期时间
function formatDateTime(date: string) {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取会员详情
async function fetchMemberDetail() {
  try {
    loading.value = true
    const memberId = Number(route.params.id)

    const response = await getMemberDetail(memberId)

    // 更新会员信息，保留原有数据
    memberInfo.value = {
      ...memberInfo.value,
      ...(response.data || response)
    }
  } catch (error) {
    ElMessage.error('获取会员详情失败')
    // 不再跳转，让用户留在页面上
  } finally {
    loading.value = false
  }
}

// 返回列表
function handleBack() {
  router.push('/admin/members')
}

// 编辑会员
function handleEdit() {
  router.push(`/admin/members/${memberInfo.value.id}/edit`)
}

// 调整积分
function handleAdjustPoints() {
  pointsForm.type = 1
  pointsForm.points = 0
  pointsForm.reason = ''
  pointsDialogVisible.value = true
}

// 调整余额
function handleAdjustBalance() {
  balanceForm.type = 1
  balanceForm.amount = 0.01
  balanceForm.reason = ''
  balanceDialogVisible.value = true
}

// 提交余额调整
async function handleSubmitBalance() {
  if (!balanceFormRef.value) return
  try {
    const valid = await balanceFormRef.value.validate()
    if (!valid) return
    balanceSubmitting.value = true
    const changeAmount = balanceForm.type === 2 ? -balanceForm.amount : balanceForm.amount
    await updateMemberBalance(memberInfo.value.id, changeAmount, balanceForm.reason)
    ElMessage.success('余额调整成功')
    balanceDialogVisible.value = false
    fetchMemberDetail()
  } catch (error) {
    ElMessage.error('余额调整失败')
  } finally {
    balanceSubmitting.value = false
  }
}

// 查看消费记录
function handleViewConsumption() {
  router.push(`/admin/consumption?memberId=${memberInfo.value.id}`)
}

// 查看积分明细
function handleViewPoints() {
  router.push(`/admin/points?memberId=${memberInfo.value.id}`)
}

// 查看优惠券
function handleViewCoupons() {
  router.push(`/admin/coupons?memberId=${memberInfo.value.id}`)
}

// 切换会员状态
async function handleToggleStatus() {
  const currentStatus = memberInfo.value.status
  const action = getToggleStatusText(currentStatus)

  // 状态切换逻辑：
  // 0 (禁用) -> 1 (正常)
  // 1 (正常) -> 0 (禁用)
  // 2 (冻结) -> 1 (正常)
  let newStatus: number
  if (currentStatus === 0) {
    newStatus = 1  // 禁用 -> 正常
  } else if (currentStatus === 1) {
    newStatus = 0  // 正常 -> 禁用
  } else {
    newStatus = 1  // 冻结 -> 正常
  }

  try {
    await ElMessageBox.confirm(
      `确定要${action}会员 ${memberInfo.value.name} 吗？`,
      `${action}会员`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateMemberStatus(memberInfo.value.id, newStatus)
    memberInfo.value.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 提交积分调整
async function handleSubmitPoints() {
  if (!pointsFormRef.value) return

  try {
    const valid = await pointsFormRef.value.validate()
    if (!valid) return

    pointsSubmitting.value = true

    const changePoints = pointsForm.type === 2 ? -pointsForm.points : pointsForm.points

    await updateMemberPoints(
      memberInfo.value.id,
      Math.abs(changePoints),
      pointsForm.type,
      pointsForm.reason
    )

    ElMessage.success('积分调整成功')
    pointsDialogVisible.value = false
    fetchMemberDetail() // 重新获取会员详情
  } catch (error) {
    ElMessage.error('积分调整失败')
  } finally {
    pointsSubmitting.value = false
  }
}

// 关闭积分调整对话框
function handleClosePointsDialog() {
  if (pointsSubmitting.value) return
  pointsDialogVisible.value = false
}

onMounted(() => {
  fetchMemberDetail()
})
</script>

<style scoped>
.member-detail {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.member-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.member-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #409EFF;
}

.member-name {
  font-weight: 600;
  color: #303133;
}

.member-phone {
  font-family: 'Courier New', monospace;
  color: #606266;
}

.points-value {
  font-weight: 600;
  color: #E6A23C;
  font-size: 16px;
}

.total-points {
  color: #909399;
}

.balance-value {
  font-weight: 600;
  color: #67C23A;
  font-size: 16px;
}

.consumption-value {
  font-weight: 600;
  color: #F56C6C;
  font-size: 16px;
}

.consumption-count {
  color: #606266;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  color: white;
}

.stat-card:nth-child(2) .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.stat-card:nth-child(3) .stat-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #F7BA2A 100%);
}

.stat-card:nth-child(4) .stat-icon {
  background: linear-gradient(135deg, #F56C6C 0%, #F78989 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 4px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.quick-actions {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.action-grid .el-button {
  height: 48px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.action-grid .el-button:hover {
  transform: translateY(-2px);
}

.member-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.current-points {
  font-weight: 600;
  color: #E6A23C;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .member-detail {
    padding: 12px;
  }

  .header-actions {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .action-buttons {
    justify-content: center;
  }

  .stats-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .el-descriptions {
    --el-descriptions-table-border: 1px solid #ebeef5;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}

/* 会员等级徽章样式 */
.member-level-badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  min-width: 60px;
  border-radius: 4px;
  border: 1px solid;
  line-height: 1.4;
}
</style>
