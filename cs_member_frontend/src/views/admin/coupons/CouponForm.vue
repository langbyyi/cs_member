<template>
  <div class="coupon-form">

    <el-row :gutter="20">
      <!-- 左侧表单 -->
      <el-col :span="16">
        <el-card class="form-card">
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="120px"
            :disabled="submitting"
          >
            <el-form-item label="优惠券名称" prop="couponName">
              <el-input
                v-model="formData.couponName"
                placeholder="请输入优惠券名称"
                clearable
                maxlength="50"
                show-word-limit
              />
              <div class="form-tip">
                建议名称简洁明了，便于会员理解
              </div>
            </el-form-item>

            <el-form-item label="优惠券类型" prop="couponType">
              <el-radio-group v-model="formData.couponType" @change="handleTypeChange">
                <el-radio :value="1">满减券</el-radio>
                <el-radio :value="2">折扣券</el-radio>
                <el-radio :value="3">兑换券</el-radio>
              </el-radio-group>
              <div class="form-tip">
                选择优惠券类型，不同类型有不同的优惠方式
              </div>
            </el-form-item>

            <!-- 满减券配置 -->
            <template v-if="formData.couponType === 1">
              <el-form-item label="满减条件" prop="minAmount">
                <el-input-number
                  v-model="formData.minAmount"
                  :min="0"
                  :precision="2"
                  :step="10"
                  style="width: 200px"
                  placeholder="最低消费金额"
                />
                <span class="form-tip">元（消费满此金额可使用）</span>
              </el-form-item>

              <el-form-item label="减免金额" prop="discountAmount">
                <el-input-number
                  v-model="formData.discountAmount"
                  :min="0.01"
                  :precision="2"
                  :step="1"
                  style="width: 200px"
                  placeholder="减免金额"
                  @change="validateDiscountAmount"
                />
                <span class="form-tip">元</span>
                <div class="form-tip" v-if="formData.minAmount > 0 && formData.discountAmount > 0">
                  实际支付：{{ (formData.minAmount - formData.discountAmount).toFixed(2) }}元
                </div>
              </el-form-item>
            </template>

            <!-- 折扣券配置 -->
            <template v-if="formData.couponType === 2">
              <el-form-item label="折扣力度" prop="discountRate">
                <el-input-number
                  v-model="formData.discountRate"
                  :min="1"
                  :max="99"
                  :step="1"
                  style="width: 200px"
                  placeholder="折扣百分比"
                />
                <span class="form-tip">%</span>
                <div class="form-tip">
                  即{{ (formData.discountRate / 10).toFixed(1) }}折
                </div>
              </el-form-item>

              <el-form-item label="最高优惠" prop="maxDiscountAmount">
                <el-input-number
                  v-model="formData.maxDiscountAmount"
                  :min="0"
                  :precision="2"
                  :step="5"
                  style="width: 200px"
                  placeholder="最高优惠金额"
                />
                <span class="form-tip">元（可选，设置后不超过此金额）</span>
              </el-form-item>
            </template>

            <!-- 兑换券配置 -->
            <template v-if="formData.couponType === 3">
              <el-form-item label="兑换内容" prop="exchangeContent">
                <el-input
                  v-model="formData.exchangeContent"
                  type="textarea"
                  :rows="3"
                  placeholder="请描述可兑换的商品或服务"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="兑换价值" prop="exchangeValue">
                <el-input-number
                  v-model="formData.exchangeValue"
                  :min="0.01"
                  :precision="2"
                  :step="1"
                  style="width: 200px"
                  placeholder="兑换价值"
                />
                <span class="form-tip">元（用于统计价值）</span>
              </el-form-item>
            </template>

            <el-divider>发放设置</el-divider>

            <el-form-item label="发放数量" prop="totalQuantity">
              <el-input-number
                v-model="formData.totalQuantity"
                :min="1"
                :max="100000"
                :step="10"
                style="width: 200px"
                placeholder="发放总数"
              />
              <span class="form-tip">张</span>
              <div class="form-tip" v-if="!isEdit">
                建议根据会员数量和活动规模合理设置
              </div>
            </el-form-item>

            <el-form-item label="每人限领" prop="limitPerPerson">
              <el-input-number
                v-model="formData.limitPerPerson"
                :min="1"
                :max="50"
                style="width: 200px"
                placeholder="每人限领数量"
              />
              <span class="form-tip">张</span>
              <div class="form-tip">
                防止同一会员领取过多，提高公平性
              </div>
            </el-form-item>

            <el-form-item label="发放方式" prop="issueType">
              <el-radio-group v-model="formData.issueType">
                <el-radio :value="1">手动发放</el-radio>
                <el-radio :value="2">自动发放</el-radio>
                <el-radio :value="3">活动发放</el-radio>
              </el-radio-group>
              <div class="form-tip">
                <div v-if="formData.issueType === 1">管理员手动发放给指定会员</div>
                <div v-if="formData.issueType === 2">系统根据规则自动发放</div>
                <div v-if="formData.issueType === 3">通过活动批量发放</div>
              </div>
            </el-form-item>

            <el-divider>有效期设置</el-divider>

            <el-form-item label="有效期类型" prop="validityType">
              <el-radio-group v-model="formData.validityType" @change="handleValidityTypeChange">
                <el-radio :value="1">固定期限</el-radio>
                <el-radio :value="2">相对期限</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 固定期限 -->
            <template v-if="formData.validityType === 1">
              <el-form-item label="有效期" prop="validPeriod">
                <el-date-picker
                  v-model="formData.validPeriod"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 400px"
                />
              </el-form-item>
            </template>

            <!-- 相对期限 -->
            <template v-if="formData.validityType === 2">
              <el-form-item label="有效天数" prop="validDays">
                <el-input-number
                  v-model="formData.validDays"
                  :min="1"
                  :max="365"
                  style="width: 200px"
                  placeholder="领取后有效天数"
                />
                <span class="form-tip">天（从领取时开始计算）</span>
              </el-form-item>
            </template>

            <el-form-item label="使用说明" prop="description">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="4"
                placeholder="请输入使用说明、注意事项等"
                maxlength="500"
                show-word-limit
              />
              <div class="form-tip">
                详细的使用说明有助于会员正确使用优惠券
              </div>
            </el-form-item>

            <el-divider />

            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmit"
                :loading="submitting"
                size="large"
              >
                {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '创建优惠券') }}
              </el-button>
              <el-button @click="handleCancel" size="large">取消</el-button>
              <el-button @click="handlePreview" type="info" size="large">预览效果</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧预览 -->
      <el-col :span="8">
        <el-card class="preview-card">
          <template #header>
            <span class="card-title">
              <el-icon><View /></el-icon>
              优惠券预览
            </span>
          </template>
          <div class="coupon-preview">
            <div class="coupon-main" :class="getCouponTypeClass()">
              <div class="coupon-header">
                <h3 class="coupon-title">{{ formData.couponName || '优惠券名称' }}</h3>
                <div class="coupon-type">{{ getCouponTypeText() }}</div>
              </div>

              <div class="coupon-content">
                <div class="coupon-discount" v-if="formData.couponType === 1">
                  <span class="discount-symbol">¥</span>
                  <span class="discount-amount">{{ formData.discountAmount || 0 }}</span>
                  <div class="discount-condition">满{{ formData.minAmount || 0 }}元可用</div>
                </div>

                <div class="coupon-discount" v-if="formData.couponType === 2">
                  <span class="discount-amount">{{ (formData.discountRate / 10).toFixed(1) }}</span>
                  <span class="discount-symbol">折</span>
                  <div class="discount-condition" v-if="formData.maxDiscountAmount">
                    最高优惠{{ formData.maxDiscountAmount }}元
                  </div>
                </div>

                <div class="coupon-discount" v-if="formData.couponType === 3">
                  <span class="discount-text">兑换券</span>
                  <div class="discount-condition">{{ formData.exchangeContent || '兑换内容' }}</div>
                </div>
              </div>

              <div class="coupon-footer">
                <div class="coupon-validity">
                  <template v-if="formData.validityType === 1 && formData.validPeriod">
                    {{ formatDate(formData.validPeriod[0]) }} 至 {{ formatDate(formData.validPeriod[1]) }}
                  </template>
                  <template v-else-if="formData.validityType === 2">
                    领取后{{ formData.validDays || 0 }}天内有效
                  </template>
                  <template v-else>
                    有效期未设置
                  </template>
                </div>
              </div>
            </div>

            <div class="coupon-tips">
              <h4>发放信息</h4>
              <p>总数量：{{ formData.totalQuantity || 0 }}张</p>
              <p>每人限领：{{ formData.limitPerPerson || 0 }}张</p>
              <p>发放方式：{{ getIssueTypeText() }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createCoupon,
  updateCoupon,
  getCouponDetail,
  type Coupon,
  type CreateCouponRequest,
  type UpdateCouponRequest
} from '@/api/coupon'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const couponData = ref<Coupon | null>(null)

// 表单数据
const formData = reactive({
  couponName: '',
  couponType: 1,
  minAmount: 0,
  discountAmount: 0,
  discountRate: 80,
  maxDiscountAmount: 0,
  exchangeContent: '',
  exchangeValue: 0,
  totalQuantity: 100,
  limitPerPerson: 1,
  issueType: 1,
  validityType: 1,
  validPeriod: null as [string, string] | null,
  validDays: 30,
  description: ''
})

// 动态表单验证规则
const formRules = computed<FormRules>(() => {
  const baseRules: FormRules = {
    couponName: [
      { required: true, message: '请输入优惠券名称', trigger: 'blur' },
      { min: 2, max: 50, message: '名称长度在2-50个字符之间', trigger: 'blur' }
    ],
    couponType: [
      { required: true, message: '请选择优惠券类型', trigger: 'change' }
    ],
    totalQuantity: [
      { required: true, message: '请输入发放数量', trigger: 'blur' },
      { type: 'number', min: 1, max: 100000, message: '发放数量在1-100000张之间', trigger: 'blur' }
    ],
    limitPerPerson: [
      { required: true, message: '请输入每人限领数量', trigger: 'blur' },
      { type: 'number', min: 1, max: 50, message: '每人限领1-50张', trigger: 'blur' }
    ],
    issueType: [
      { required: true, message: '请选择发放方式', trigger: 'change' }
    ],
    validityType: [
      { required: true, message: '请选择有效期类型', trigger: 'change' }
    ],
    description: [
      { max: 500, message: '使用说明不能超过500字符', trigger: 'blur' }
    ]
  }

  // 根据优惠券类型添加验证规则
  if (formData.couponType === 1) {
    baseRules.minAmount = [
      { required: true, message: '请输入满减条件', trigger: 'blur' },
      { type: 'number', min: 0, message: '金额不能为负数', trigger: 'blur' }
    ]
    baseRules.discountAmount = [
      { required: true, message: '请输入减免金额', trigger: 'blur' },
      { type: 'number', min: 0.01, message: '减免金额必须大于0', trigger: 'blur' },
      { validator: validateDiscountAmount, trigger: 'blur' }
    ]
  }

  if (formData.couponType === 2) {
    baseRules.discountRate = [
      { required: true, message: '请输入折扣力度', trigger: 'blur' },
      { type: 'number', min: 1, max: 99, message: '折扣范围在1-99%之间', trigger: 'blur' }
    ]
  }

  if (formData.couponType === 3) {
    baseRules.exchangeContent = [
      { required: true, message: '请输入兑换内容', trigger: 'blur' },
      { min: 2, max: 200, message: '兑换内容在2-200字符之间', trigger: 'blur' }
    ]
    baseRules.exchangeValue = [
      { required: true, message: '请输入兑换价值', trigger: 'blur' },
      { type: 'number', min: 0.01, message: '兑换价值必须大于0', trigger: 'blur' }
    ]
  }

  // 根据有效期类型添加验证规则
  if (formData.validityType === 1) {
    baseRules.validPeriod = [
      { required: true, message: '请选择有效期', trigger: 'change' }
    ]
  }

  if (formData.validityType === 2) {
    baseRules.validDays = [
      { required: true, message: '请输入有效天数', trigger: 'blur' },
      { type: 'number', min: 1, max: 365, message: '有效天数在1-365天之间', trigger: 'blur' }
    ]
  }

  return baseRules
})

// 获取优惠券类型文本
function getCouponTypeText() {
  const typeMap: Record<number, string> = {
    1: '满减券',
    2: '折扣券',
    3: '兑换券'
  }
  return typeMap[formData.couponType] || '未知类型'
}

// 获取优惠券类型样式类
function getCouponTypeClass() {
  return `coupon-type-${formData.couponType}`
}

// 获取发放方式文本
function getIssueTypeText() {
  const typeMap: Record<number, string> = {
    1: '手动发放',
    2: '自动发放',
    3: '活动发放'
  }
  return typeMap[formData.issueType] || '未知'
}

// 格式化日期
function formatDate(date: string) {
  return dayjs(date).format('YYYY-MM-DD')
}

// 处理优惠券类型变化
function handleTypeChange() {
  // 重置相关字段
  formData.minAmount = 0
  formData.discountAmount = 0
  formData.discountRate = 80
  formData.maxDiscountAmount = 0
  formData.exchangeContent = ''
  formData.exchangeValue = 0
}

// 处理有效期类型变化
function handleValidityTypeChange() {
  formData.validPeriod = null
  formData.validDays = 30
}

// 验证减免金额
function validateDiscountAmount(rule: any, value: number, callback: Function) {
  if (formData.couponType === 1 && formData.minAmount > 0 && value >= formData.minAmount) {
    callback(new Error('减免金额不能大于等于满减条件'))
  } else {
    callback()
  }
}

// 验证满减金额合理性
function validateDiscountAmountReasonable() {
  if (formData.couponType === 1 && formData.minAmount > 0 && formData.discountAmount >= formData.minAmount) {
    ElMessage.warning('减免金额不能大于等于满减条件')
    formData.discountAmount = Math.floor(formData.minAmount * 0.8)
  }
}

// 获取优惠券详情
async function fetchCouponDetail() {
  try {
    const couponId = Number(route.params.id)
    couponData.value = await getCouponDetail(couponId)

    // 填充表单数据
    Object.assign(formData, {
      couponName: couponData.value.couponName,
      couponType: couponData.value.couponType,
      minAmount: couponData.value.minAmount || 0,
      discountAmount: couponData.value.discountAmount || 0,
      discountRate: couponData.value.discountRate || 80,
      maxDiscountAmount: couponData.value.maxDiscountAmount || 0,
      exchangeContent: couponData.value.exchangeContent || '',
      exchangeValue: couponData.value.exchangeValue || 0,
      totalQuantity: couponData.value.totalQuantity,
      limitPerPerson: couponData.value.limitPerPerson || 1,
      issueType: couponData.value.issueType || 1,
      validityType: 1,
      validPeriod: [
        couponData.value.validStartTime,
        couponData.value.validEndTime
      ],
      validDays: 30,
      description: couponData.value.description || ''
    })
  } catch (error) {
    ElMessage.error('获取优惠券详情失败')
    router.back()
  }
}

// 预览效果
function handlePreview() {
  if (!formData.couponName) {
    ElMessage.warning('请先填写优惠券名称')
    return
  }
  const typeMap: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '兑换券' }
  const issueTypeMap: Record<number, string> = { 1: '自动发放', 2: '手动领取', 3: '定向发放' }
  let benefit = ''
  if (formData.couponType === 1) {
    benefit = `满${formData.minAmount}减${formData.discountAmount}`
  } else if (formData.couponType === 2) {
    benefit = `${formData.discountRate}折${formData.maxDiscountAmount ? '，最高减' + formData.maxDiscountAmount : ''}`
  } else {
    benefit = `${formData.exchangeContent || '兑换商品'}（价值${formData.exchangeValue}）`
  }
  let validInfo = ''
  if (formData.validityType === 1 && formData.validPeriod) {
    validInfo = `${formData.validPeriod[0]} 至 ${formData.validPeriod[1]}`
  } else if (formData.validityType === 2) {
    validInfo = `领取后${formData.validDays}天内有效`
  }
  ElMessageBox.alert(
    `<div style="line-height:2">
      <p><b>名称：</b>${formData.couponName}</p>
      <p><b>类型：</b>${typeMap[formData.couponType]}</p>
      <p><b>优惠：</b>${benefit}</p>
      <p><b>发放数量：</b>${formData.totalQuantity}张</p>
      <p><b>每人限领：</b>${formData.limitPerPerson}张</p>
      <p><b>发放方式：</b>${issueTypeMap[formData.issueType]}</p>
      <p><b>有效期：</b>${validInfo}</p>
      ${formData.description ? `<p><b>说明：</b>${formData.description}</p>` : ''}
    </div>`,
    '优惠券预览',
    { dangerouslyUseHTMLString: true, confirmButtonText: '确定' }
  )
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 构建提交数据
    let submitData: CreateCouponRequest | UpdateCouponRequest

    if (isEdit.value) {
      // 编辑模式
      const couponId = Number(route.params.id)
      submitData = {
        couponName: formData.couponName,
        couponType: formData.couponType,
        minAmount: formData.couponType === 1 ? formData.minAmount : undefined,
        discountAmount: formData.couponType === 1 ? formData.discountAmount : undefined,
        discountRate: formData.couponType === 2 ? formData.discountRate : undefined,
        maxDiscountAmount: formData.couponType === 2 ? formData.maxDiscountAmount : undefined,
        exchangeContent: formData.couponType === 3 ? formData.exchangeContent : undefined,
        exchangeValue: formData.couponType === 3 ? formData.exchangeValue : undefined,
        totalQuantity: formData.totalQuantity,
        limitPerPerson: formData.limitPerPerson,
        issueType: formData.issueType,
        validStartTime: formData.validityType === 1 ? formData.validPeriod![0] : undefined,
        validEndTime: formData.validityType === 1 ? formData.validPeriod![1] : undefined,
        validDays: formData.validityType === 2 ? formData.validDays : undefined,
        description: formData.description
      }

      await updateCoupon(couponId, submitData)
      ElMessage.success('优惠券更新成功')
    } else {
      // 创建模式
      submitData = {
        couponName: formData.couponName,
        couponType: formData.couponType,
        minAmount: formData.couponType === 1 ? formData.minAmount : undefined,
        discountAmount: formData.couponType === 1 ? formData.discountAmount : undefined,
        discountRate: formData.couponType === 2 ? formData.discountRate : undefined,
        maxDiscountAmount: formData.couponType === 2 ? formData.maxDiscountAmount : undefined,
        exchangeContent: formData.couponType === 3 ? formData.exchangeContent : undefined,
        exchangeValue: formData.couponType === 3 ? formData.exchangeValue : undefined,
        totalQuantity: formData.totalQuantity,
        limitPerPerson: formData.limitPerPerson,
        issueType: formData.issueType,
        validStartTime: formData.validityType === 1 ? formData.validPeriod![0] : undefined,
        validEndTime: formData.validityType === 1 ? formData.validPeriod![1] : undefined,
        validDays: formData.validityType === 2 ? formData.validDays : undefined,
        description: formData.description
      }

      await createCoupon(submitData)
      ElMessage.success('优惠券创建成功')
    }

    router.push('/coupons')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 取消
function handleCancel() {
  router.push('/coupons')
}

onMounted(() => {
  if (isEdit.value) {
    fetchCouponDetail()
  }
})
</script>

<style scoped>
.coupon-form {
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

.preview-card {
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

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 优惠券预览样式 */
.coupon-preview {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.coupon-main {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  position: relative;
}

.coupon-main::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  opacity: 0.1;
  z-index: 0;
}

.coupon-type-1 {
  border: 2px solid #409eff;
}

.coupon-type-2 {
  border: 2px solid #67c23a;
}

.coupon-type-3 {
  border: 2px solid #e6a23c;
}

.coupon-header {
  padding: 20px;
  text-align: center;
  position: relative;
  z-index: 1;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.1) 100%);
}

.coupon-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.coupon-type {
  font-size: 14px;
  color: #606266;
  padding: 4px 12px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 12px;
  display: inline-block;
}

.coupon-content {
  padding: 30px 20px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.coupon-discount {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.discount-symbol {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.discount-amount {
  font-size: 48px;
  font-weight: 700;
  color: #f56c6c;
  line-height: 1;
}

.discount-text {
  font-size: 24px;
  font-weight: 600;
  color: #e6a23c;
}

.discount-condition {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.coupon-footer {
  padding: 15px 20px;
  background: #f8f9fa;
  border-top: 1px dashed #e4e7ed;
  position: relative;
  z-index: 1;
}

.coupon-validity {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.coupon-tips {
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.coupon-tips h4 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.coupon-tips p {
  font-size: 12px;
  color: #606266;
  margin: 6px 0;
  line-height: 1.4;
}

/* 表单样式增强 */
.coupon-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.coupon-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.coupon-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.coupon-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.coupon-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: vertical;
}

.coupon-form :deep(.el-radio) {
  margin-right: 20px;
  margin-bottom: 8px;
}

.coupon-form :deep(.el-radio__label) {
  font-weight: 500;
}

.coupon-form :deep(.el-input-number) {
  width: 100%;
}

.coupon-form :deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.coupon-form :deep(.el-divider__text) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.coupon-form :deep(.el-date-editor) {
  width: 100%;
}

/* 按钮样式 */
.coupon-form :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 12px 24px;
}

.coupon-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
}

.coupon-form :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 数字输入框特殊样式 */
.coupon-form :deep(.el-input-number.is-controls-right .el-input__wrapper) {
  padding-right: 32px;
}

/* 预览卡片动画 */
.preview-card {
  transition: all 0.3s ease;
}

.preview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.coupon-main {
  transition: all 0.3s ease;
}

.coupon-main:hover {
  transform: scale(1.02);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .coupon-form :deep(.el-col-16) {
    flex: 0 0 66.6666666667%;
    max-width: 66.6666666667%;
  }

  .coupon-form :deep(.el-col-8) {
    flex: 0 0 33.3333333333%;
    max-width: 33.3333333333%;
  }
}

@media (max-width: 992px) {
  .coupon-form :deep(.el-col-16),
  .coupon-form :deep(.el-col-8) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .preview-card {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .coupon-form {
    padding: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .coupon-preview {
    padding: 12px;
  }

  .coupon-header {
    padding: 15px;
  }

  .coupon-title {
    font-size: 16px;
  }

  .coupon-content {
    padding: 20px 15px;
  }

  .discount-amount {
    font-size: 36px;
  }

  .discount-symbol {
    font-size: 18px;
  }

  .coupon-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  .coupon-form :deep(.el-button--large) {
    width: 100%;
    height: 44px;
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .coupon-form {
    padding: 8px;
  }

  .discount-amount {
    font-size: 28px;
  }

  .coupon-form :deep(.el-form-item__label) {
    font-size: 14px;
  }

  .form-tip {
    font-size: 11px;
  }
}

/* 表单验证状态样式 */
.coupon-form :deep(.el-form-item.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c;
}

.coupon-form :deep(.el-form-item.is-success .el-input__wrapper) {
  box-shadow: 0 0 0 1px #67c23a;
}

/* 加载状态 */
.coupon-form[disabled] {
  opacity: 0.6;
  pointer-events: none;
}

/* 动画效果 */
@keyframes slideInUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.coupon-main {
  animation: slideInUp 0.5s ease-out;
}

/* 特殊效果 */
.coupon-type-1 .discount-amount,
.coupon-type-1 .discount-symbol {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.coupon-type-2 .discount-amount {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.coupon-type-3 .discount-text {
  background: linear-gradient(135deg, #e6a23c 0%, #ffd54f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 优惠券虚线边框效果 */
.coupon-main::after {
  content: '';
  position: absolute;
  top: 50%;
  left: -1px;
  width: 20px;
  height: 20px;
  background: #f8f9fa;
  border-radius: 50%;
  transform: translateY(-50%);
  border: 2px solid inherit;
  z-index: 2;
}

.coupon-main::before {
  content: '';
  position: absolute;
  top: 50%;
  right: -1px;
  width: 20px;
  height: 20px;
  background: #f8f9fa;
  border-radius: 50%;
  transform: translateY(-50%);
  border: 2px solid inherit;
  z-index: 2;
}
</style>
