<template>
  <div class="consumption-management">
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
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">¥{{ formatMoney(statistics.totalRevenue || 0) }}</div>
              <div class="card-label">累计营收</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>12.5%</span>
            </div>
          </div>

          <div class="overview-card active">
            <div class="card-icon">
              <el-icon><List /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(statistics.totalOrderCount || 0) }}</div>
              <div class="card-label">累计订单数</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>8.3%</span>
            </div>
          </div>

          <div class="overview-card new">
            <div class="card-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">¥{{ formatMoney(statistics.todayRevenue || 0) }}</div>
              <div class="card-label">今日营收</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>15.2%</span>
            </div>
          </div>

          <div class="overview-card revenue">
            <div class="card-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(statistics.todayOrderCount || 0) }}</div>
              <div class="card-label">今日订单</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>6.7%</span>
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
                <el-icon class="title-icon"><Search /></el-icon>
                <span>智能筛选</span>
              </div>
            </div>
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="会员信息">
                <el-input v-model="searchForm.keyword" placeholder="会员姓名/编号" clearable @keyup.enter="handleSearch" />
              </el-form-item>
              <el-form-item label="消费单号">
                <el-input v-model="searchForm.consumptionNo" placeholder="请输入单号" clearable @keyup.enter="handleSearch" />
              </el-form-item>
              <el-form-item label="支付方式">
                <el-select v-model="searchForm.paymentMethod" placeholder="全部" clearable style="width: 120px">
                  <el-option label="全部" value="" />
                  <el-option label="微信支付" value="wechat" />
                  <el-option label="支付宝" value="alipay" />
                  <el-option label="现金" value="cash" />
                  <el-option label="余额支付" value="balance" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon> 查询
                </el-button>
                <el-button @click="resetSearch">
                  <el-icon><Refresh /></el-icon> 重置
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
            <span class="table-title">消费记录列表</span>
          </div>
          <div class="header-right">
            <!-- 可以添加导出按钮等 -->
          </div>
        </div>

        <div class="modern-table-wrapper" v-loading="loading">
          <el-table :data="tableData" style="width: 100%" border stripe>
            <el-table-column prop="consumptionNo" label="消费单号" width="180" />
            <el-table-column label="会员信息" min-width="150">
              <template #default="{ row }">
                <div v-if="row.memberId">
                  <!-- 这里暂时没有会员名，需要后端关联查询或者前端处理 -->
                  <!-- 假设后端返回了 memberName 和 memberNo -->
                   {{ row.memberName || '-' }} ({{ row.memberNo || '-' }})
                </div>
                <div v-else>非会员</div>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="消费金额" width="120">
              <template #default="{ row }">
                ¥{{ formatMoney(row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" width="120">
              <template #default="{ row }">
                <el-tag :type="getPaymentMethodTag(row.paymentMethod)">
                  {{ getPaymentMethodText(row.paymentMethod) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="consumptionType" label="消费类型" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'info'">
                  {{ row.status === 'completed' ? '已完成' : row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="consumptionTime" label="消费时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.consumptionTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页组件 -->
        <div class="modern-pagination">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Money, List, TrendCharts, Wallet } from '@element-plus/icons-vue'
import { getConsumptionRecords, getConsumptionStatistics } from '@/api/member'
import dayjs from 'dayjs'

// 统计数据
const statistics = ref({
  todayOrderCount: 0,
  todayRevenue: 0,
  totalOrderCount: 0,
  totalRevenue: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  consumptionNo: '',
  paymentMethod: '',
  dateRange: [] as string[]
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 初始化
onMounted(() => {
  fetchStatistics()
  fetchData()
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getConsumptionStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
  }
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      memberNo: /^\d+$/.test(searchForm.keyword) ? undefined : searchForm.keyword,
      memberName: !/^\d+$/.test(searchForm.keyword) ? searchForm.keyword : undefined,
      consumptionNo: searchForm.consumptionNo,
      paymentMethod: searchForm.paymentMethod,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    
    if (searchForm.keyword) {
        params.memberNo = searchForm.keyword
        params.memberName = searchForm.keyword
    }

    const res = await getConsumptionRecords(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.consumptionNo = ''
  searchForm.paymentMethod = ''
  searchForm.dateRange = []
  handleSearch()
}

// 分页处理
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  fetchData()
}

// 工具函数
const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const formatMoney = (amount: number) => {
  return amount.toFixed(2)
}

const formatDateTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getPaymentMethodText = (method: string) => {
  const map: Record<string, string> = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'cash': '现金',
    'balance': '余额支付'
  }
  return map[method] || method || '其他'
}

const getPaymentMethodTag = (method: string) => {
  const map: Record<string, string> = {
    'wechat': 'success',
    'alipay': 'primary',
    'cash': 'warning',
    'balance': 'danger'
  }
  return map[method] || 'info'
}
</script>

<style scoped>
.consumption-management {
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

.modern-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
