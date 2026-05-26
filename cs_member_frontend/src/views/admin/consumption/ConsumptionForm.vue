<template>
  <div class="consumption-form">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/consumption' }">消费记录</el-breadcrumb-item>
        <el-breadcrumb-item>新增消费</el-breadcrumb-item>
      </el-breadcrumb>
      <h1 class="page-title">新增消费记录</h1>
    </div>

    <el-row :gutter="20">
      <!-- 消费表单 -->
      <el-col :span="16">
        <el-card class="form-card">
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="120px"
            :disabled="submitting"
          >
            <el-form-item label="会员手机号" prop="memberPhone">
              <el-input
                v-model="formData.memberPhone"
                placeholder="请输入会员手机号"
                @blur="handleSearchMember"
                @keyup.enter="handleSearchMember"
                clearable
              >
                <template #append>
                  <el-button
                    @click="handleSearchMember"
                    :loading="searchingMember"
                    :disabled="!formData.memberPhone"
                  >
                    查询
                  </el-button>
                </template>
              </el-input>
              <div class="form-tip">
                输入会员手机号查询会员信息
              </div>
            </el-form-item>

            <!-- 会员信息展示 -->
            <el-form-item v-if="memberInfo" label="会员信息">
              <div class="member-info-card">
                <div class="member-basic">
                  <div class="member-avatar">
                    <el-icon size="32"><User /></el-icon>
                  </div>
                  <div class="member-details">
                    <div class="member-name">{{ memberInfo.name }}</div>
                    <div class="member-meta">
                      <span class="member-no">{{ memberInfo.memberNo }}</span>
                      <el-tag :type="getMemberLevelType(memberInfo.memberLevel)" size="small">
                        {{ memberInfo.memberLevelName }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                <div class="member-stats">
                  <div class="stat-item">
                    <span class="stat-label">当前积分</span>
                    <span class="stat-value">{{ memberInfo.currentPoints }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">账户余额</span>
                    <span class="stat-value">¥{{ memberInfo.balance }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">可用优惠券</span>
                    <span class="stat-value">{{ memberInfo.availableCoupons || 0 }}</span>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-divider>消费信息</el-divider>

            <el-form-item label="消费金额" prop="amount">
              <el-input-number
                v-model="formData.amount"
                :min="0.01"
                :precision="2"
                :step="0.01"
                style="width: 200px"
                placeholder="请输入消费金额"
                @change="handleAmountChange"
              />
              <span class="form-tip">元</span>
            </el-form-item>

            <el-form-item label="优惠信息" v-if="memberInfo && formData.amount > 0">
              <div class="discount-section">
                <!-- 使用积分 -->
                <div class="discount-item">
                  <el-checkbox
                    v-model="formData.usePoints"
                    @change="handlePointsToggle"
                    :disabled="!memberInfo.currentPoints"
                  >
                    使用积分抵扣
                  </el-checkbox>
                  <el-input-number
                    v-if="formData.usePoints"
                    v-model="formData.pointsUsed"
                    :min="0"
                    :max="Math.min(memberInfo.currentPoints, Math.floor(formData.amount * 100))"
                    :precision="0"
                    style="width: 150px; margin-left: 12px;"
                    @change="handlePointsUsedChange"
                  />
                  <span class="form-tip" v-if="formData.usePoints">
                    (可用: {{ memberInfo.currentPoints }} 积分, 最多抵扣 {{ Math.floor(formData.amount * 100) }} 积分)
                  </span>
                </div>

                <!-- 选择优惠券 -->
                <div class="discount-item">
                  <el-checkbox
                    v-model="formData.useCoupon"
                    @change="handleCouponToggle"
                  >
                    使用优惠券
                  </el-checkbox>
                  <el-select
                    v-if="formData.useCoupon"
                    v-model="formData.couponId"
                    placeholder="请选择优惠券"
                    style="width: 200px; margin-left: 12px;"
                    @change="handleCouponChange"
                  >
                    <el-option
                      v-for="coupon in availableCoupons"
                      :key="coupon.id"
                      :label="`${coupon.couponName} - 满${coupon.minAmount}减${coupon.discountAmount}`"
                      :value="coupon.id"
                      :disabled="formData.amount < coupon.minAmount"
                    />
                  </el-select>
                  <span class="form-tip" v-if="formData.useCoupon && !availableCoupons.length">
                    暂无可用优惠券
                  </span>
                </div>
              </div>
            </el-form-item>

            <!-- 费用明细 -->
            <el-form-item label="费用明细" v-if="formData.amount > 0">
              <div class="fee-details">
                <div class="fee-row">
                  <span>原价：</span>
                  <span class="fee-amount">¥{{ formData.amount }}</span>
                </div>
                <div class="fee-row" v-if="formData.pointsUsed > 0">
                  <span>积分抵扣：</span>
                  <span class="fee-amount discount">-¥{{ (formData.pointsUsed / 100).toFixed(2) }}</span>
                </div>
                <div class="fee-row" v-if="selectedCoupon && formData.amount >= selectedCoupon.minAmount">
                  <span>优惠券优惠：</span>
                  <span class="fee-amount discount">-¥{{ selectedCoupon.discountAmount }}</span>
                </div>
                <div class="fee-divider"></div>
                <div class="fee-row total">
                  <span>实付金额：</span>
                  <span class="fee-amount total-amount">¥{{ formData.actualAmount.toFixed(2) }}</span>
                </div>
                <div class="fee-row" v-if="calculatedPoints > 0">
                  <span>获得积分：</span>
                  <span class="fee-amount points">+{{ calculatedPoints }}</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="支付方式" prop="paymentMethod">
              <el-radio-group v-model="formData.paymentMethod">
                <el-radio :value="1">现金</el-radio>
                <el-radio :value="2">微信</el-radio>
                <el-radio :value="3">支付宝</el-radio>
                <el-radio :value="4">银行卡</el-radio>
                <el-radio :value="5" v-if="memberInfo && memberInfo.balance > 0">会员卡</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="消费门店" prop="storeId">
              <el-select
                v-model="formData.storeId"
                placeholder="请选择门店"
                style="width: 200px"
                filterable
              >
                <el-option
                  v-for="store in storeList"
                  :key="store.id"
                  :label="store.storeName"
                  :value="store.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="备注">
              <el-input
                v-model="formData.remark"
                type="textarea"
                placeholder="请输入备注信息"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-divider />

            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmit"
                :loading="submitting"
                :disabled="!memberInfo"
                size="large"
              >
                {{ submitting ? '提交中...' : '确认支付' }}
              </el-button>
              <el-button @click="handleCancel" size="large">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧信息面板 -->
      <el-col :span="8">
        <!-- 会员快速信息 -->
        <el-card v-if="memberInfo" class="info-card">
          <template #header>
            <span class="card-title">
              <el-icon><User /></el-icon>
              会员信息
            </span>
          </template>
          <div class="quick-info">
            <div class="info-item">
              <span class="label">会员等级</span>
              <el-tag :type="getMemberLevelType(memberInfo.memberLevel)">
                {{ memberInfo.memberLevelName }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ formatDate(memberInfo.registerTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">累计消费</span>
              <span class="value money">¥{{ memberInfo.totalConsumption }}</span>
            </div>
            <div class="info-item">
              <span class="label">消费次数</span>
              <span class="value">{{ memberInfo.consumptionCount }}次</span>
            </div>
          </div>
        </el-card>

        <!-- 今日消费统计 -->
        <el-card class="info-card">
          <template #header>
            <span class="card-title">
              <el-icon><TrendCharts /></el-icon>
              今日统计
            </span>
          </template>
          <div class="today-stats">
            <div class="stat-card">
              <div class="stat-number">{{ todayStats.totalOrders }}</div>
              <div class="stat-label">消费笔数</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">¥{{ todayStats.totalAmount }}</div>
              <div class="stat-label">消费总额</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ todayStats.memberOrders }}</div>
              <div class="stat-label">会员消费</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createConsumption,
  getMemberByPhone,
  getStoreList,
  getTodayConsumptions,
  calculateAmount,
  calculatePoints,
  generateConsumptionNo,
  type Member,
  type Store,
  type CreateConsumptionRequest
} from '@/api/consumption'
import type { MemberCoupon } from '@/types'
import { getAvailableCoupons } from '@/api/coupon'

const router = useRouter()
const formRef = ref<FormInstance>()

// 响应式数据
const submitting = ref(false)
const searchingMember = ref(false)
const memberInfo = ref<Member | null>(null)
const storeList = ref<Store[]>([])
const availableCoupons = ref<MemberCoupon[]>([])

// 今日统计数据
const todayStats = ref({
  totalOrders: 0,
  totalAmount: 0,
  memberOrders: 0
})

// 表单数据
const formData = reactive({
  memberPhone: '',
  amount: 0,
  actualAmount: 0,
  usePoints: false,
  pointsUsed: 0,
  useCoupon: false,
  couponId: null as number | null,
  paymentMethod: 1,
  storeId: null as number | null,
  remark: '',
  operatorId: 1 // 从用户状态中获取
})

// 选中的优惠券
const selectedCoupon = computed(() => {
  if (!formData.couponId) return null
  return availableCoupons.value.find(coupon => coupon.id === formData.couponId)
})

// 计算获得的积分
const calculatedPoints = computed(() => {
  if (formData.actualAmount <= 0) return 0
  return Math.floor(formData.actualAmount) // 1元=1积分
})

// 表单验证规则
const formRules: FormRules = {
  memberPhone: [
    { required: true, message: '请输入会员手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入消费金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '消费金额必须大于0', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ],
  storeId: [
    { required: true, message: '请选择消费门店', trigger: 'change' }
  ]
}

// 获取会员等级标签类型
function getMemberLevelType(level: number) {
  const typeMap: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'danger'
  }
  return typeMap[level] || 'info'
}

// 格式化日期
function formatDate(date: string) {
  return new Date(date).toLocaleDateString()
}

// 查询会员信息
async function handleSearchMember() {
  if (!formData.memberPhone) {
    ElMessage.warning('请先输入会员手机号')
    return
  }

  try {
    searchingMember.value = true
    memberInfo.value = await getMemberByPhone(formData.memberPhone)
    ElMessage.success('会员信息查询成功')

    // 重置优惠相关字段
    formData.usePoints = false
    formData.pointsUsed = 0
    formData.useCoupon = false
    formData.couponId = null
    availableCoupons.value = []

    // 获取会员可用优惠券
    try {
      const couponRes = await getAvailableCoupons()
      availableCoupons.value = (couponRes as any)?.data || (couponRes as any) || []
    } catch { availableCoupons.value = [] }
  } catch (error: any) {
    ElMessage.error(error.message || '会员不存在')
    memberInfo.value = null
  } finally {
    searchingMember.value = false
  }
}

// 处理金额变化
function handleAmountChange() {
  calculateActualAmount()

  // 如果使用积分，重新计算最大可用积分
  if (formData.usePoints && memberInfo.value) {
    const maxPoints = Math.min(
      memberInfo.value.currentPoints,
      Math.floor(formData.amount * 100)
    )
    if (formData.pointsUsed > maxPoints) {
      formData.pointsUsed = maxPoints
    }
  }
}

// 计算实际支付金额
function calculateActualAmount() {
  let actualAmount = formData.amount

  // 积分抵扣
  if (formData.usePoints && formData.pointsUsed > 0) {
    actualAmount -= formData.pointsUsed / 100
  }

  // 优惠券抵扣
  if (selectedCoupon.value && formData.amount >= selectedCoupon.value.minAmount) {
    actualAmount -= selectedCoupon.value.discountAmount
  }

  formData.actualAmount = Math.max(0, actualAmount)
}

// 积分使用开关
function handlePointsToggle(enabled: boolean) {
  if (!enabled) {
    formData.pointsUsed = 0
  } else if (memberInfo.value) {
    // 默认使用最多可用的积分
    formData.pointsUsed = Math.min(
      memberInfo.value.currentPoints,
      Math.floor(formData.amount * 100)
    )
  }
  calculateActualAmount()
}

// 积分使用数量变化
function handlePointsUsedChange() {
  calculateActualAmount()
}

// 优惠券使用开关
function handleCouponToggle(enabled: boolean) {
  if (!enabled) {
    formData.couponId = null
  }
  calculateActualAmount()
}

// 优惠券选择变化
function handleCouponChange() {
  calculateActualAmount()
}

// 获取门店列表
async function fetchStoreList() {
  try {
    storeList.value = await getStoreList()
    // 默认选择第一个门店
    if (storeList.value.length > 0 && !formData.storeId) {
      formData.storeId = storeList.value[0].id
    }
  } catch (error) {
  }
}

// 获取今日统计数据
async function fetchTodayStats() {
  try {
    const todayConsumptions = await getTodayConsumptions(formData.storeId || undefined)
    todayStats.value = {
      totalOrders: todayConsumptions.length,
      totalAmount: todayConsumptions.reduce((sum, record) => sum + record.actualAmount, 0).toFixed(2),
      memberOrders: todayConsumptions.filter(record => record.memberId).length
    }
  } catch (error) {
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    if (!memberInfo.value) {
      ElMessage.error('请先查询会员信息')
      return
    }

    // 确认支付
    await ElMessageBox.confirm(
      `确认支付 ¥${formData.actualAmount.toFixed(2)} 吗？`,
      '确认支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    // 生成消费单号
    const consumptionNo = await generateConsumptionNo()

    // 创建消费记录
    const consumptionData: CreateConsumptionRequest = {
      memberId: memberInfo.value.id,
      storeId: formData.storeId!,
      amount: formData.amount,
      discountAmount: formData.amount - formData.actualAmount,
      actualAmount: formData.actualAmount,
      pointsUsed: formData.pointsUsed,
      couponId: formData.couponId || undefined,
      paymentMethod: formData.paymentMethod,
      operatorId: formData.operatorId,
      remark: formData.remark
    }

    await createConsumption(consumptionData)
    ElMessage.success('消费记录创建成功')
    router.push('/consumption')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '创建消费记录失败')
    }
  } finally {
    submitting.value = false
  }
}

// 取消
function handleCancel() {
  router.push('/consumption')
}

// 监听表单数据变化
watch([
  () => formData.amount,
  () => formData.usePoints,
  () => formData.pointsUsed,
  () => formData.couponId
], () => {
  calculateActualAmount()
})

// 监听门店变化，更新统计数据
watch(() => formData.storeId, () => {
  fetchTodayStats()
})

onMounted(() => {
  fetchStoreList()
  fetchTodayStats()
})
</script>

<style scoped>
.consumption-form {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 0 0;
}

.form-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.info-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.member-info-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafbfc;
}

.member-basic {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.member-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: white;
}

.member-details {
  flex: 1;
}

.member-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.member-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-no {
  font-family: 'Courier New', monospace;
  color: #606266;
  font-size: 14px;
}

.member-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 12px 8px;
  background: white;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.discount-section {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafbfc;
}

.discount-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.discount-item:last-child {
  margin-bottom: 0;
}

.fee-details {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafbfc;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.fee-row:last-child {
  margin-bottom: 0;
}

.fee-amount {
  font-weight: 600;
  color: #303133;
}

.fee-amount.discount {
  color: #67c23a;
}

.fee-row.total .fee-amount {
  font-size: 18px;
  color: #409eff;
}

.fee-amount.points {
  color: #e6a23c;
}

.fee-divider {
  height: 1px;
  background: #e4e7ed;
  margin: 12px 0;
}

.quick-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-size: 14px;
  color: #606266;
}

.info-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.info-item .value.money {
  color: #67c23a;
  font-weight: 600;
}

.today-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-card {
  text-align: center;
  padding: 16px 8px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 表单样式增强 */
.consumption-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.consumption-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.consumption-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.consumption-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.consumption-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: vertical;
}

.consumption-form :deep(.el-radio) {
  margin-right: 20px;
}

.consumption-form :deep(.el-radio__label) {
  font-weight: 500;
}

.consumption-form :deep(.el-checkbox) {
  margin-right: 20px;
}

.consumption-form :deep(.el-checkbox__label) {
  font-weight: 500;
}

.consumption-form :deep(.el-select) {
  width: 100%;
}

.consumption-form :deep(.el-divider) {
  margin: 24px 0;
}

.consumption-form :deep(.el-divider__text) {
  font-weight: 600;
  color: #303133;
}

/* 按钮样式 */
.consumption-form :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.consumption-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
}

.consumption-form :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 输入框组合样式 */
.consumption-form :deep(.el-input-group) {
  border-radius: 8px;
  overflow: hidden;
}

.consumption-form :deep(.el-input-group__append) {
  background: #409eff;
  color: white;
  border: none;
}

.consumption-form :deep(.el-input-group__append .el-button) {
  border: none;
  color: white;
}

/* 数字输入框样式 */
.consumption-form :deep(.el-input-number) {
  width: 100%;
}

.consumption-form :deep(.el-input-number .el-input__inner) {
  text-align: left;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .consumption-form :deep(.el-col-16) {
    flex: 0 0 66.6666666667%;
    max-width: 66.6666666667%;
  }

  .consumption-form :deep(.el-col-8) {
    flex: 0 0 33.3333333333%;
    max-width: 33.3333333333%;
  }
}

@media (max-width: 992px) {
  .consumption-form :deep(.el-col-16),
  .consumption-form :deep(.el-col-8) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .member-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .today-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .consumption-form {
    padding: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .member-basic {
    flex-direction: column;
    text-align: center;
  }

  .member-stats {
    grid-template-columns: 1fr;
  }

  .today-stats {
    grid-template-columns: 1fr;
  }

  .discount-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .discount-item .el-input-number,
  .discount-item .el-select {
    width: 100% !important;
    margin-left: 0 !important;
  }

  .consumption-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

@media (max-width: 480px) {
  .consumption-form {
    padding: 8px;
  }

  .member-info-card {
    padding: 12px;
  }

  .discount-section {
    padding: 12px;
  }

  .fee-details {
    padding: 12px;
  }

  .consumption-form :deep(.el-button--large) {
    width: 100%;
    height: 44px;
  }
}

/* 动画效果 */
.member-info-card,
.discount-section,
.fee-details {
  transition: all 0.3s ease;
}

.member-info-card:hover,
.discount-section:hover,
.fee-details:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 加载状态 */
.consumption-form[disabled] {
  opacity: 0.6;
  pointer-events: none;
}

/* 成功状态动画 */
@keyframes successPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.success-animation {
  animation: successPulse 0.3s ease-in-out;
}
</style>
