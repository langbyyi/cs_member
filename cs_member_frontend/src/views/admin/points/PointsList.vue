<template>
  <div class="points-management">
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
              <el-icon><Coin /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(statistics.totalPoints || 0) }}</div>
              <div class="card-label">累计发放积分</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>18.6%</span>
            </div>
          </div>

          <div class="overview-card active">
            <div class="card-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(statistics.totalMembers || 0) }}</div>
              <div class="card-label">参与会员数</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>9.2%</span>
            </div>
          </div>

          <div class="overview-card new">
            <div class="card-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(statistics.thisMonthEarned || 0) }}</div>
              <div class="card-label">本月发放</div>
            </div>
            <div class="card-trend up">
              <el-icon><TrendCharts /></el-icon>
              <span>22.4%</span>
            </div>
          </div>

          <div class="overview-card revenue">
            <div class="card-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatNumber(statistics.thisMonthUsed || 0) }}</div>
              <div class="card-label">本月消耗</div>
            </div>
            <div class="card-trend down">
              <el-icon><TrendCharts /></el-icon>
              <span>5.3%</span>
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
              <el-form-item label="变动类型">
                <el-select v-model="searchForm.changeType" placeholder="全部" clearable style="width: 120px">
                  <el-option label="全部" value="" />
                  <el-option label="获得" value="increase" />
                  <el-option label="使用" value="decrease" />
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
            <span class="table-title">积分记录列表</span>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdjustPoints">
              <el-icon><Plus /></el-icon> 积分调整
            </el-button>
          </div>
        </div>

        <div class="modern-table-wrapper" v-loading="loading">
          <el-table :data="tableData" style="width: 100%" border stripe>
            <el-table-column prop="memberNo" label="会员编号" width="140" />
            <el-table-column prop="memberName" label="会员姓名" width="120" />
            <el-table-column prop="pointsBefore" label="变动前" width="100" />
            <el-table-column prop="pointsChange" label="变动积分" width="120">
              <template #default="{ row }">
                <span :class="row.pointsChange > 0 ? 'text-success' : 'text-danger'">
                  {{ row.pointsChange > 0 ? '+' : '' }}{{ row.pointsChange }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="pointsAfter" label="变动后" width="100" />
            <el-table-column prop="changeReason" label="变动原因" min-width="150" />
            <el-table-column prop="referenceType" label="关联类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getReferenceTypeTag(row.referenceType)">
                  {{ getReferenceTypeText(row.referenceType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="recordTime" label="记录时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.recordTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="expireTime" label="过期时间" width="160">
              <template #default="{ row }">
                {{ row.expireTime ? formatDateTime(row.expireTime) : '-' }}
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

    <!-- 积分调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="积分调整"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef" label-width="100px">
        <el-form-item label="选择会员" prop="memberId">
          <el-select
            v-model="adjustForm.memberId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入会员姓名/手机号/编号搜索"
            :remote-method="searchMember"
            :loading="memberLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in memberOptions"
              :key="item.id"
              :label="`${item.name} (${item.phone}) - 余额:${item.currentPoints}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" prop="type">
          <el-radio-group v-model="adjustForm.type">
            <el-radio :value="1">增加积分</el-radio>
            <el-radio :value="-1">扣减积分</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input-number v-model="adjustForm.points" :min="1" :max="100000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime" v-if="adjustForm.type === 1">
          <el-date-picker
            v-model="adjustForm.expireTime"
            type="datetime"
            placeholder="请选择过期时间（可选）"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            :disabled-date="(time) => time.getTime() < Date.now()"
          />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-select v-model="adjustForm.reason" placeholder="请选择或输入原因" filterable allow-create style="width: 100%">
            <el-option label="活动奖励" value="活动奖励" />
            <el-option label="系统补偿" value="系统补偿" />
            <el-option label="违规扣除" value="违规扣除" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="adjustDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdjust" :loading="adjustLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { Search, Refresh, Plus, Coin, User, TrendCharts, Wallet } from '@element-plus/icons-vue'
import { getPointsRecords, getPointsStatistics, adjustPoints, getMemberList } from '@/api/member'
import dayjs from 'dayjs'

// 统计数据
const statistics = ref({
  totalPoints: 0,
  totalMembers: 0,
  thisMonthEarned: 0,
  thisMonthUsed: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  changeType: '',
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

// 调整积分表单
const adjustDialogVisible = ref(false)
const adjustLoading = ref(false)
const adjustFormRef = ref<FormInstance>()
const adjustForm = reactive({
  memberId: undefined as number | undefined,
  type: 1,
  points: 0,
  reason: '',
  remark: '',
  expireTime: ''
})

// 会员搜索相关
const memberLoading = ref(false)
const memberOptions = ref<any[]>([])

const searchMember = async (query: string) => {
  if (query) {
    memberLoading.value = true
    try {
      const res = await getMemberList({
        page: 1,
        size: 20,
        name: query,
        phone: query,
        memberNo: query
      })
      if (res.code === 200) {
        memberOptions.value = res.data.list
      }
    } catch (error) {
    } finally {
      memberLoading.value = false
    }
  } else {
    memberOptions.value = []
  }
}

const adjustRules = {
  memberId: [{ required: true, message: '请输入会员ID', trigger: 'blur' }],
  points: [{ required: true, message: '请输入积分数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  fetchStatistics()
  fetchData()
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getPointsStatistics()
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
      memberNo: /^\d+$/.test(searchForm.keyword) ? undefined : searchForm.keyword, // 简单判断，实际可能需要更严谨
      memberName: !/^\d+$/.test(searchForm.keyword) ? searchForm.keyword : undefined,
      changeType: searchForm.changeType,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    
    // 如果是纯数字，尝试作为会员ID或编号查询，这里简化处理，后端支持模糊查询
    if (searchForm.keyword) {
        // 后端支持 memberNo 和 memberName
        // 这里简单处理：传给 memberName，后端 xml 中是用 LIKE 查询
        // 实际上后端 xml 分别判断了 memberNo 和 memberName
        // 我们可以两个都传相同的 keyword，或者前端区分
        // 为了简单，我们假设 keyword 是 memberName
        // 如果需要支持 memberNo，可以修改 params
        params.memberNo = searchForm.keyword
        params.memberName = searchForm.keyword
    }

    const res = await getPointsRecords(params)
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
  searchForm.changeType = ''
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

// 积分调整
const handleAdjustPoints = () => {
  adjustForm.memberId = undefined
  adjustForm.type = 1
  adjustForm.points = 0
  adjustForm.reason = ''
  adjustForm.remark = ''
  adjustForm.expireTime = ''
  adjustDialogVisible.value = true
}

const submitAdjust = async () => {
  if (!adjustFormRef.value) return
  
  await adjustFormRef.value.validate(async (valid) => {
    if (valid) {
      adjustLoading.value = true
      try {
        const res = await adjustPoints({
          memberId: Number(adjustForm.memberId),
          points: adjustForm.points,
          type: adjustForm.type,
          reason: adjustForm.reason,
          remark: adjustForm.remark,
          expireTime: adjustForm.type === 1 ? adjustForm.expireTime : undefined
        })
        
        if (res.code === 200) {
          ElMessage.success('积分调整成功')
          adjustDialogVisible.value = false
          fetchData()
          fetchStatistics()
        } else {
          ElMessage.error(res.message || '积分调整失败')
        }
      } catch (error) {
        ElMessage.error('积分调整失败')
      } finally {
        adjustLoading.value = false
      }
    }
  })
}

// 工具函数
const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const formatDateTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getReferenceTypeText = (type: string) => {
  const map: Record<string, string> = {
    'consumption': '消费',
    'coupon': '优惠券',
    'manual': '手动调整',
    'refund': '退款',
    'expire': '过期'
  }
  return map[type] || type || '其他'
}

const getReferenceTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'consumption': 'success',
    'coupon': 'warning',
    'manual': 'primary',
    'refund': 'danger',
    'expire': 'info'
  }
  return map[type] || 'info'
}
</script>

<style scoped>
.points-management {
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

.text-success {
  color: #67c23a;
  font-weight: bold;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.modern-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>