<template>
  <div class="consumption-container">
    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item label="消费金额">
          <div class="amount-range">
            <el-input-number
              v-model="searchForm.minAmount"
              placeholder="最小金额"
              :min="0"
              :precision="2"
              :controls="false"
              class="amount-input"
            />
            <span class="range-separator">-</span>
            <el-input-number
              v-model="searchForm.maxAmount"
              placeholder="最大金额"
              :min="0"
              :precision="2"
              :controls="false"
              class="amount-input"
            />
          </div>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch" round>
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset" round>
            重置
          </el-button>
          <el-button :icon="Download" @click="handleExport" round plain>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 消费统计 -->
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon-wrapper total">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ totalConsumption }}</div>
            <div class="stat-label">总消费金额</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon-wrapper count">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalRecords }}</div>
            <div class="stat-label">消费笔数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon-wrapper average">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ averageAmount }}</div>
            <div class="stat-label">平均消费</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon-wrapper month">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ thisMonthConsumption }}</div>
            <div class="stat-label">本月消费</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 消费记录列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-icon">
              <el-icon><List /></el-icon>
            </div>
            <h3>消费记录</h3>
          </div>
          <span class="record-count">共 {{ pagination.total }} 条记录</span>
        </div>
      </template>

      <el-table
        :data="consumptionList"
        :loading="loading"
        style="width: 100%"
        class="custom-table"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266' }"
      >
        <el-table-column prop="consumptionNo" label="消费单号" width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.consumptionNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="消费门店" min-width="140" />
        <el-table-column prop="consumptionAmount" label="消费金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.consumptionAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pointsEarned" label="获得积分" width="100" align="right">
          <template #default="{ row }">
            <span class="points-text">+{{ row.pointsEarned }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentType(row.paymentMethod)" size="small" effect="light" round>
              {{ getPaymentText(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="consumptionTime" label="消费时间" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.consumptionTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :icon="View"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 消费详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="消费详情"
      width="500px"
      :close-on-click-modal="false"
      class="custom-dialog"
      align-center
    >
      <div v-if="selectedRecord" class="detail-content">
        <div class="detail-header">
          <div class="detail-amount">¥{{ selectedRecord.consumptionAmount }}</div>
          <div class="detail-status">交易成功</div>
        </div>
        
        <div class="detail-list">
          <div class="detail-item">
            <span class="label">消费单号</span>
            <span class="value mono">{{ selectedRecord.consumptionNo }}</span>
          </div>
          <div class="detail-item">
            <span class="label">消费门店</span>
            <span class="value">{{ selectedRecord.storeName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">获得积分</span>
            <span class="value points">+{{ selectedRecord.pointsEarned }}</span>
          </div>
          <div class="detail-item">
            <span class="label">支付方式</span>
            <span class="value">{{ getPaymentText(selectedRecord.paymentMethod) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">消费时间</span>
            <span class="value">{{ formatDateTime(selectedRecord.consumptionTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">操作员</span>
            <span class="value">{{ selectedRecord.operatorName || '系统' }}</span>
          </div>
          <div class="detail-item" v-if="selectedRecord.remark">
            <span class="label">备注</span>
            <span class="value">{{ selectedRecord.remark }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false" round>关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  Download,
  Money,
  Tickets,
  DataLine,
  Calendar,
  View,
  List
} from '@element-plus/icons-vue'
import { getMemberConsumptionList } from '@/api/member'
import { useUserStore } from '@/stores/user'
import { exportToExcel } from '@/utils/export'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  minAmount: null,
  maxAmount: null
})
const dateRange = ref([])

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 消费记录列表
const consumptionList = ref([])
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const selectedRecord = ref(null)

// 计算统计数据
const totalConsumption = computed(() => {
  return consumptionList.value.reduce((sum, item: any) => sum + (parseFloat(item.consumptionAmount) || 0), 0).toFixed(2)
})

const totalRecords = computed(() => pagination.total)

const averageAmount = computed(() => {
  const count = consumptionList.value.length
  return count > 0 ? (parseFloat(totalConsumption.value) / count).toFixed(2) : '0.00'
})

const thisMonthConsumption = computed(() => {
  const currentMonth = new Date().getMonth()
  const currentYear = new Date().getFullYear()
  return consumptionList.value
    .filter((item: any) => {
      const date = new Date(item.consumptionTime)
      return date.getMonth() === currentMonth && date.getFullYear() === currentYear
    })
    .reduce((sum, item: any) => sum + (parseFloat(item.consumptionAmount) || 0), 0)
    .toFixed(2)
})

// 获取消费记录
const fetchConsumptionList = async () => {
  if (!userStore.user?.id) return

  loading.value = true
  try {
    const params = {
      memberId: userStore.user.id,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      minAmount: searchForm.minAmount,
      maxAmount: searchForm.maxAmount
    }

    const response = await getMemberConsumptionList(params)
    consumptionList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error('获取消费记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchConsumptionList()
}

// 重置
const handleReset = () => {
  dateRange.value = []
  searchForm.minAmount = null
  searchForm.maxAmount = null
  pagination.current = 1
  fetchConsumptionList()
}

// 导出
const handleExport = () => {
  if (consumptionList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const headers = [
    { title: '消费单号', key: 'consumptionNo' },
    { title: '消费门店', key: 'storeName' },
    { title: '消费金额', key: 'consumptionAmount' },
    { title: '获得积分', key: 'pointsEarned' },
    { title: '支付方式', key: 'paymentMethod' },
    { title: '消费时间', key: 'consumptionTime' }
  ]

  const data = consumptionList.value.map((item: any) => ({
    ...item,
    paymentMethod: getPaymentText(item.paymentMethod),
    consumptionTime: formatDateTime(item.consumptionTime)
  }))

  exportToExcel(data, '消费记录', headers)
}

// 查看详情
const handleViewDetail = (record: any) => {
  selectedRecord.value = record
  detailDialogVisible.value = true
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.current = 1
  fetchConsumptionList()
}

// 页码改变
const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchConsumptionList()
}

// 获取支付方式文本
const getPaymentText = (method: number) => {
  const methods = ['现金', '微信', '支付宝', '银行卡', '会员卡']
  return methods[method - 1] || '未知'
}

// 获取支付方式类型
const getPaymentType = (method: number) => {
  const types = ['info', 'success', 'primary', 'warning', 'danger']
  return types[method - 1] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchConsumptionList()
})
</script>

<style scoped>
.consumption-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.amount-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.amount-input {
  width: 120px;
}

.range-separator {
  color: #909399;
}

.search-actions {
  margin-left: auto;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
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

.stat-icon-wrapper.total {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.3);
}

.stat-icon-wrapper.count {
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  box-shadow: 0 8px 16px rgba(103, 194, 58, 0.3);
}

.stat-icon-wrapper.average {
  background: linear-gradient(135deg, #e6a23c 0%, #b88230 100%);
  box-shadow: 0 8px 16px rgba(230, 162, 60, 0.3);
}

.stat-icon-wrapper.month {
  background: linear-gradient(135deg, #f56c6c 0%, #d45d5d 100%);
  box-shadow: 0 8px 16px rgba(245, 108, 108, 0.3);
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

/* 列表卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 32px;
  height: 32px;
  background: #ecf5ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 18px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.record-count {
  font-size: 14px;
  color: #909399;
}

.mono-text {
  font-family: monospace;
  color: #606266;
}

.amount-text {
  font-weight: 700;
  color: #303133;
}

.points-text {
  color: #e6a23c;
  font-weight: 600;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 详情弹窗 */
.detail-content {
  padding: 10px 0;
}

.detail-header {
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.detail-amount {
  font-size: 36px;
  font-weight: 800;
  color: #303133;
  margin-bottom: 8px;
}

.detail-status {
  display: inline-block;
  padding: 4px 12px;
  background: #f0f9eb;
  color: #67c23a;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.detail-item .label {
  color: #909399;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

.detail-item .value.mono {
  font-family: monospace;
}

.detail-item .value.points {
  color: #e6a23c;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-actions {
    margin-left: 0;
    display: flex;
    gap: 8px;
  }

  .search-actions .el-button {
    flex: 1;
    margin: 0;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .el-table {
    font-size: 12px;
  }
}
</style>