<template>
  <div class="coupon-list">
    <!-- 动态背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="page-content">
      <!-- 统计概览区域 -->
      <div class="overview-section">
        <div class="overview-cards">
          <div class="overview-card total">
            <div class="card-icon">
              <el-icon><component :is="Icons.Tickets" /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ statistics.totalCoupons || 0 }}</div>
              <div class="card-label">优惠券总数</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>14.8%</span>
            </div>
          </div>

          <div class="overview-card active">
            <div class="card-icon">
              <el-icon><component :is="Icons.Check" /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ statistics.activeCoupons || 0 }}</div>
              <div class="card-label">有效优惠券</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>11.3%</span>
            </div>
          </div>

          <div class="overview-card new">
            <div class="card-icon">
              <el-icon><component :is="Icons.ShoppingCart" /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ statistics.usedCoupons || 0 }}</div>
              <div class="card-label">已使用优惠券</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>28.5%</span>
            </div>
          </div>

          <div class="overview-card revenue">
            <div class="card-icon">
              <el-icon><component :is="Icons.Money" /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">¥{{ statistics.totalSaved || 0 }}</div>
              <div class="card-label">累计节省金额</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>19.7%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-card class="filter-card" shadow="never">
          <div class="filter-content">
            <div class="filter-header">
              <div class="filter-title">
                <el-icon class="title-icon"><component :is="Icons.Search" /></el-icon>
                <span>智能筛选</span>
              </div>
            </div>
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="优惠券名称">
                <el-input v-model="searchForm.couponName" placeholder="请输入优惠券名称" clearable style="width: 200px" />
              </el-form-item>
              <el-form-item label="优惠券类型">
                <el-select v-model="searchForm.couponType" placeholder="请选择优惠券类型" clearable style="width: 150px">
                  <el-option label="满减券" :value="1" />
                  <el-option label="折扣券" :value="2" />
                  <el-option label="兑换券" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
                  <el-option label="正常" :value="1" />
                  <el-option label="停用" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="发放类型">
                <el-select v-model="searchForm.issueType" placeholder="请选择发放类型" clearable style="width: 150px">
                  <el-option label="手动发放" :value="1" />
                  <el-option label="自动发放" :value="2" />
                  <el-option label="活动发放" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="有效期">
                <el-date-picker
                  v-model="searchForm.validDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 240px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">
                  <el-icon><component :is="Icons.Search" /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><component :is="Icons.Refresh" /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>

      <!-- 数据表格区域 -->
      <div class="modern-table-container">
        <div class="table-header-panel">
          <div class="header-left">
            <span class="table-title">优惠券列表</span>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="$router.push('/admin/coupons/create')">
              <el-icon><component :is="Icons.Plus" /></el-icon>
              创建优惠券
            </el-button>
            <el-button type="success" @click="handleBatchIssue">
              <el-icon><component :is="Icons.Tickets" /></el-icon>
              批量发放
            </el-button>
            <el-button type="warning" @click="handleExport">
              <el-icon><component :is="Icons.Download" /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>

        <div class="modern-table-wrapper" v-loading="loading">
          <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="couponName" label="优惠券名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="couponType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.couponType)" size="small">
              {{ getTypeText(row.couponType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠内容" width="150">
          <template #default="{ row }">
            <span v-if="row.couponType === 1" class="discount-content">
              满¥{{ row.minAmount }}减¥{{ row.discountAmount }}
            </span>
            <span v-else-if="row.couponType === 2" class="discount-content">
              {{ (row.discountAmount / 10).toFixed(1) }}折
            </span>
            <span v-else class="discount-content">
              免费兑换
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="发放总数" width="100" sortable="custom">
          <template #default="{ row }">
            <span class="quantity-value">{{ row.totalQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedQuantity" label="已使用" width="100" sortable="custom">
          <template #default="{ row }">
            <span class="used-value">{{ row.usedQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usageRate" label="使用率" width="100" sortable="custom">
          <template #default="{ row }">
            <el-progress
              :percentage="getUsageRate(row)"
              :color="getUsageRateColor(getUsageRate(row))"
              :show-text="false"
              style="width: 60px"
            />
            <span class="usage-rate">{{ getUsageRate(row) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="validStartTime" label="有效期" width="200">
          <template #default="{ row }">
            <div class="validity-period">
              <div>{{ formatDate(row.validStartTime) }}</div>
              <div>{{ formatDate(row.validEndTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              详情
            </el-button>
            <el-button link type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="success" size="small" @click="handleIssue(row)">
              发放
            </el-button>
            <el-dropdown trigger="click" @command="(command) => handleCommand(command, row)">
              <el-button link type="primary" size="small">
                更多<el-icon class="el-icon--right"><component :is="Icons.ArrowDown" /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="viewMembers">查看持券会员</el-dropdown-item>
                  <el-dropdown-item command="viewRecords">使用记录</el-dropdown-item>
                  <el-dropdown-item command="toggleStatus" divided>
                    {{ row.status === 1 ? '停用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" class="danger-item">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>

        <!-- 分页组件 -->
        <div class="modern-pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :background="true"
            layout="total, prev, pager, next"
          />
        </div>
      </div>
    </div>

    <!-- 批量发放对话框 -->
    <el-dialog
      v-model="batchIssueDialogVisible"
      title="批量发放优惠券"
      width="600px"
      :before-close="handleCloseBatchIssueDialog"
    >
      <el-form
        ref="batchIssueFormRef"
        :model="batchIssueForm"
        :rules="batchIssueRules"
        label-width="120px"
      >
        <el-form-item label="选择优惠券" prop="couponId">
          <el-select
            v-model="batchIssueForm.couponId"
            placeholder="请选择优惠券"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              :label="`${coupon.couponName} (剩余${coupon.remainingQuantity}张)`"
              :value="coupon.id"
              :disabled="coupon.remainingQuantity <= 0"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="发放对象" prop="targetType">
          <el-radio-group v-model="batchIssueForm.targetType">
            <el-radio :value="1">全部会员</el-radio>
            <el-radio :value="2">指定会员等级</el-radio>
            <el-radio :value="3">指定会员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="batchIssueForm.targetType === 2" label="会员等级" prop="memberLevel">
          <el-select v-model="batchIssueForm.memberLevel" placeholder="请选择会员等级" style="width: 100%">
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="batchIssueForm.targetType === 3" label="会员列表" prop="memberIds">
          <el-select
            v-model="batchIssueForm.memberIds"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="请输入并选择会员"
            :remote-method="searchMembers"
            :loading="memberSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in memberOptions"
              :key="item.id"
              :label="`${item.name} (${item.phone})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="发放数量" prop="quantity">
          <el-input-number
            v-model="batchIssueForm.quantity"
            :min="1"
            :max="maxIssueQuantity"
            style="width: 100%"
          />
          <div class="form-tip">每位会员发放数量，最多{{ maxIssueQuantity }}张</div>
        </el-form-item>

        <el-form-item label="发放原因" prop="reason">
          <el-input
            v-model="batchIssueForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入发放原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchIssueDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchIssueSubmitting" @click="handleSubmitBatchIssue">
          确定发放
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Tickets,
  Download,
  Check,
  ShoppingCart,
  Money,
  Search,
  Refresh,
  ArrowDown
} from '@element-plus/icons-vue'
import {
  getCouponList,
  updateCouponStatus,
  deleteCoupon,
  issueCoupon,
  batchIssueCoupon,
  getCouponStatistics,
  type Coupon
} from '@/api/coupon'
import { searchMembers } from '@/api/member'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const batchIssueDialogVisible = ref(false)
const batchIssueSubmitting = ref(false)
const batchIssueFormRef = ref<FormInstance>()
const memberSearchLoading = ref(false)
const memberOptions = ref<any[]>([])
const availableCoupons = ref<Coupon[]>([])

// 搜索表单
const searchForm = reactive({
  couponName: '',
  couponType: undefined as number | undefined,
  status: undefined as number | undefined,
  issueType: undefined as number | undefined,
  validDateRange: null as [string, string] | null
})

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 排序参数
const sortParams = reactive({
  prop: '',
  order: ''
})

// 统计数据
const statistics = reactive({
  totalCoupons: 0,
  activeCoupons: 0,
  usedCoupons: 0,
  totalSaved: 0
})

// 批量发放表单
const batchIssueForm = reactive({
  couponId: '',
  targetType: 1,
  memberLevel: undefined as number | undefined,
  memberIds: [] as number[],
  quantity: 1,
  reason: ''
})

// 批量发放表单验证规则
const batchIssueRules: FormRules = {
  couponId: [
    { required: true, message: '请选择优惠券', trigger: 'change' }
  ],
  targetType: [
    { required: true, message: '请选择发放对象', trigger: 'change' }
  ],
  memberLevel: [
    { required: true, message: '请选择会员等级', trigger: 'change' }
  ],
  memberIds: [
    { required: true, message: '请选择会员', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入发放数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '发放数量至少为1', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入发放原因', trigger: 'blur' },
    { min: 2, max: 200, message: '发放原因在2-200字符之间', trigger: 'blur' }
  ]
}

const tableData = ref<Coupon[]>([])

// 使用markRaw包装图标组件，避免不必要的响应式化
const Icons = markRaw({
  Plus,
  Tickets,
  Download,
  Check,
  ShoppingCart,
  Money,
  Search,
  Refresh,
  ArrowDown
})

// 最大发放数量
const maxIssueQuantity = computed(() => {
  const selectedCoupon = availableCoupons.value.find(c => c.id === batchIssueForm.couponId)
  if (!selectedCoupon) return 1
  const remaining = selectedCoupon.remainingQuantity
  return Math.min(remaining, 10) // 限制最多发放10张
})

// 获取优惠券类型标签类型
function getTypeTagType(couponType: number) {
  const typeMap: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning'
  }
  return typeMap[couponType] || 'info'
}

// 获取优惠券类型文本
function getTypeText(couponType: number) {
  const textMap: Record<number, string> = {
    1: '满减券',
    2: '折扣券',
    3: '兑换券'
  }
  return textMap[couponType] || '未知'
}

// 获取状态标签类型
function getStatusTagType(status: number) {
  const statusMap: Record<number, string> = {
    1: 'success',
    2: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
function getStatusText(status: number) {
  const textMap: Record<number, string> = {
    1: '正常',
    2: '停用'
  }
  return textMap[status] || '未知'
}

// 获取使用率
function getUsageRate(coupon: Coupon) {
  if (coupon.totalQuantity === 0) return 0
  return Math.round((coupon.usedQuantity / coupon.totalQuantity) * 100)
}

// 获取使用率颜色
function getUsageRateColor(percentage: number) {
  if (percentage < 30) return '#909399'
  if (percentage < 60) return '#e6a23c'
  if (percentage < 90) return '#409eff'
  return '#67c23a'
}

// 格式化日期
function formatDate(date: string) {
  return dayjs(date).format('YYYY-MM-DD')
}

// 格式化日期时间
function formatDateTime(date: string) {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取优惠券列表
async function fetchCouponList() {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm,
      ...(searchForm.validDateRange && {
        validStartTime: searchForm.validDateRange[0],
        validEndTime: searchForm.validDateRange[1]
      }),
      ...(sortParams.prop && {
        sortBy: sortParams.prop,
        sortOrder: sortParams.order === 'ascending' ? 'asc' : 'desc'
      })
    }

    const response = await getCouponList(params)
    tableData.value = response.records
    pagination.total = response.total
  } catch (error) {
    ElMessage.error('获取优惠券列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
async function fetchStatistics() {
  try {
    const response = await getCouponStatistics()
    Object.assign(statistics, response)
  } catch (error) {
  }
}

// 获取可用的优惠券列表
async function fetchAvailableCoupons() {
  try {
    const response = await getCouponList({ status: 1, size: 1000 })
    availableCoupons.value = response.records.filter(c => c.remainingQuantity > 0)
  } catch (error) {
  }
}

// 搜索会员
async function searchMembers(query: string) {
  if (!query) return

  try {
    memberSearchLoading.value = true
    const response = await searchMembers({ keyword: query, limit: 20 })
    memberOptions.value = response
  } catch (error) {
  } finally {
    memberSearchLoading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchCouponList()
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    couponName: '',
    couponType: undefined,
    status: undefined,
    issueType: undefined,
    validDateRange: null
  })
  handleSearch()
}

// 导出
function handleExport() {
  if (!tableData.value.length) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const couponTypeMap: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '兑换券' }
  const statusMap: Record<number, string> = { 0: '已禁用', 1: '已启用', 2: '已过期' }
  const headers = ['ID', '名称', '类型', '最低消费', '优惠金额', '已发放', '已使用', '状态', '有效期开始', '有效期结束']
  const rows = tableData.value.map(c => [
    c.id, c.name, couponTypeMap[c.couponType] || c.couponType,
    c.minAmount ?? '', c.discountAmount ?? '',
    c.issuedCount ?? 0, c.usedCount ?? 0,
    statusMap[c.status] ?? c.status,
    c.validStartTime ? dayjs(c.validStartTime).format('YYYY-MM-DD') : '',
    c.validEndTime ? dayjs(c.validEndTime).format('YYYY-MM-DD') : ''
  ])
  const csv = '﻿' + [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `优惠券列表_${dayjs().format('YYYYMMDDHHmmss')}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 排序变化
function handleSortChange({ prop, order }: { prop: string; order: string }) {
  sortParams.prop = prop
  sortParams.order = order
  fetchCouponList()
}

// 监听分页变化
watch(() => pagination.page, () => {
  fetchCouponList()
})

// 查看详情
function handleView(row: Coupon) {
  router.push(`/coupons/${row.id}`)
}

// 编辑优惠券
function handleEdit(row: Coupon) {
  router.push(`/coupons/${row.id}`)
}

// 发放优惠券
async function handleIssue(row: Coupon) {
  try {
    const { value: memberId } = await ElMessageBox.prompt(
      '请输入要发放给的会员ID',
      '发放优惠券',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入有效的会员ID'
      }
    )

    await issueCoupon(row.id, parseInt(memberId))
    ElMessage.success('优惠券发放成功')
    fetchCouponList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发放优惠券失败')
    }
  }
}

// 批量发放
function handleBatchIssue() {
  resetBatchIssueForm()
  batchIssueDialogVisible.value = true
  fetchAvailableCoupons()
}

// 重置批量发放表单
function resetBatchIssueForm() {
  Object.assign(batchIssueForm, {
    couponId: '',
    targetType: 1,
    memberLevel: undefined,
    memberIds: [],
    quantity: 1,
    reason: ''
  })
  memberOptions.value = []
}

// 提交批量发放
async function handleSubmitBatchIssue() {
  if (!batchIssueFormRef.value) return

  try {
    const valid = await batchIssueFormRef.value.validate()
    if (!valid) return

    batchIssueSubmitting.value = true

    await batchIssueCoupon(batchIssueForm)

    ElMessage.success('批量发放成功')
    batchIssueDialogVisible.value = false
    fetchCouponList()
    fetchStatistics()
  } catch (error: any) {
    ElMessage.error(error.message || '批量发放失败')
  } finally {
    batchIssueSubmitting.value = false
  }
}

// 关闭批量发放对话框
function handleCloseBatchIssueDialog() {
  if (batchIssueSubmitting.value) return
  batchIssueDialogVisible.value = false
}

// 处理更多操作命令
async function handleCommand(command: string, row: Coupon) {
  switch (command) {
    case 'viewMembers':
      router.push(`/coupons/${row.id}/members`)
      break
    case 'viewRecords':
      router.push(`/coupons/${row.id}/records`)
      break
    case 'toggleStatus':
      await handleToggleStatus(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

// 切换状态
async function handleToggleStatus(row: Coupon) {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(
      `确定要${action}优惠券 "${row.couponName}" 吗？`,
      `${action}优惠券`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateCouponStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除优惠券
async function handleDelete(row: Coupon) {
  try {
    await ElMessageBox.confirm(
      `确定要删除优惠券 "${row.couponName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    fetchCouponList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除优惠券失败')
    }
  }
}

onMounted(() => {
  fetchCouponList()
  fetchStatistics()
})
</script>

<style scoped>
.coupon-list {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f0f2f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 动态背景图形 */
.background-shapes {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
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
  right: -5%;
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #a0c4ff 0%, #c2e9fb 100%);
  animation-delay: 0s;
}

.shape-2 {
  bottom: -10%;
  left: -5%;
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

.page-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.overview-section {
  margin-bottom: 20px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.overview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, var(--card-color) 0%, var(--card-color-light) 100%);
}

.overview-card.total {
  --card-color: #409eff;
  --card-color-light: #79bbff;
}

.overview-card.active {
  --card-color: #67c23a;
  --card-color-light: #95d475;
}

.overview-card.new {
  --card-color: #e6a23c;
  --card-color-light: #ebb563;
}

.overview-card.revenue {
  --card-color: #f56c6c;
  --card-color-light: #f78989;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--card-color) 0%, var(--card-color-light) 100%);
  color: white;
  font-size: 24px;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

.card-trend.up {
  color: #67c23a;
}

.card-trend.down {
  color: #f56c6c;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.filter-content {
  padding: 0;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px 24px;
  border-bottom: 1px solid #f0f2f5;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.title-icon {
  color: #409eff;
  font-size: 18px;
}

.search-form {
  padding: 20px 24px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.overview-card.total .card-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.overview-card.active .card-icon {
  background: #f6ffed;
  color: #52c41a;
}

.overview-card.new .card-icon {
  background: #fff7e6;
  color: #fa8c16;
}

.overview-card.revenue .card-icon {
  background: #fff1f0;
  color: #f5222d;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #666;
}

.filter-section {
  margin-bottom: 20px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.modern-table-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  animation: fadeInUp 0.6s ease-out 0.4s both;
}

.table-header-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.header-right {
  display: flex;
  gap: 12px;
}

.modern-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片样式 */
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
  color: white;
}

.total-icon {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
}

.active-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.used-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #F7BA2A 100%);
}

.saved-icon {
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

.search-card, .table-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 表格样式 */
.discount-content {
  font-weight: 600;
  color: #E6A23C;
}

.quantity-value {
  font-weight: 600;
  color: #409EFF;
}

.used-value {
  font-weight: 600;
  color: #67C23A;
}

.usage-rate {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.validity-period {
  font-size: 12px;
  line-height: 1.4;
}

.validity-period div:first-child {
  color: #409EFF;
  font-weight: 500;
}

.validity-period div:last-child {
  color: #909399;
}

/* 表单提示样式 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 危险操作样式 */
:deep(.danger-item) {
  color: #f56c6c !important;
}

:deep(.danger-item:hover) {
  background-color: #fef0f0 !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .coupon-list {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .stats-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .search-form {
    display: block;
  }

  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }

  .search-form .el-input,
  .search-form .el-select,
  .search-form .el-date-picker {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }

  .header-actions {
    flex-direction: column;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>
