<template>
  <div class="points-container">
    <!-- 积分概览 -->
    <div class="points-overview">
      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper total">
            <el-icon><Medal /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">{{ memberInfo.currentPoints || 0 }}</div>
            <div class="overview-label">当前积分</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper history">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">{{ memberInfo.totalPoints || 0 }}</div>
            <div class="overview-label">累计积分</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper earned">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">+{{ todayEarned || 0 }}</div>
            <div class="overview-label">今日获得</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper used">
            <el-icon><Minus /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">-{{ todayUsed || 0 }}</div>
            <div class="overview-label">今日使用</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 积分兑换推荐 -->
    <el-card class="exchange-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-icon">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <h3>积分兑换</h3>
          </div>
          <el-button type="primary" class="action-btn" round @click="showExchangeDialog">
            查看更多兑换
          </el-button>
        </div>
      </template>

      <div class="exchange-content">
        <div class="exchange-tips">
          <el-alert
            title="积分兑换说明"
            type="info"
            show-icon
            :closable="false"
            class="custom-alert"
          >
            <template #default>
              <div class="tips-list">
                <span>• 100积分可抵扣1元消费</span>
                <span>• 积分有效期为2年</span>
                <span>• 部分商品不支持积分兑换</span>
              </div>
            </template>
          </el-alert>
        </div>

        <div class="recommend-items">
          <div class="recommend-item" v-for="item in recommendItems" :key="item.id">
            <div class="item-image">
              <div class="placeholder-icon">
            <el-icon><Present /></el-icon>
              </div>
            </div>
            <div class="item-info">
              <h4>{{ item.name }}</h4>
              <p class="item-desc">{{ item.description }}</p>
              <div class="price-row">
                <span class="item-points">{{ item.points }} 积分</span>
                <span class="item-price">¥{{ item.price }}</span>
              </div>
            </div>
            <div class="item-action">
              <el-button type="primary" class="exchange-btn" round @click="handleExchange(item)">
                立即兑换
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 积分记录列表 -->
    <el-card class="records-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-icon">
              <el-icon><List /></el-icon>
            </div>
            <h3>积分明细</h3>
          </div>
          <el-radio-group v-model="activeFilter" size="small" @change="handleFilterChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="earned">获得</el-radio-button>
            <el-radio-button label="used">使用</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table
        :data="pointsList"
        :loading="loading"
        style="width: 100%"
        class="custom-table"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266' }"
      >
        <el-table-column prop="changeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getChangeTypeTag(row.changeType)" size="small" effect="light" round>
              {{ getChangeTypeText(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分变动" width="120" align="right">
          <template #default="{ row }">
            <span :class="['points-change', getPointsClass(row.changeType)]">
              {{ row.changeType === 1 ? '+' : '-' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="currentPoints" label="变动后积分" width="120" align="right">
          <template #default="{ row }">
            <span class="balance-text">{{ row.currentPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeReason" label="说明" min-width="200" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.createTime) }}</span>
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

    <!-- 积分兑换对话框 -->
    <el-dialog
      v-model="exchangeDialogVisible"
      title="积分兑换"
      width="400px"
      :close-on-click-modal="false"
      class="custom-dialog"
      align-center
    >
      <div v-if="selectedExchangeItem" class="exchange-detail">
        <div class="item-preview-large">
          <div class="placeholder-icon-large">
            <el-icon><Gift /></el-icon>
          </div>
        </div>
        
        <div class="confirm-info">
          <h4>{{ selectedExchangeItem.name }}</h4>
          <div class="price-tag">
            <span class="points">{{ selectedExchangeItem.points }} 积分</span>
          </div>
          <p class="confirm-text">确定要兑换此商品吗？</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exchangeDialogVisible = false" round>取消</el-button>
          <el-button type="primary" :loading="exchanging" @click="confirmExchange" round>
            确认兑换
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Medal,
  TrendCharts,
  Plus,
  Minus,
  ShoppingBag,
  Present,
  List
} from '@element-plus/icons-vue'
import { getMemberPointsList, getMemberDetail, exchangePoints } from '@/api/member'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 会员信息
const memberInfo = ref({
  currentPoints: 0,
  totalPoints: 0,
  createdTime: '',
  lastUpdateTime: ''
})

// 积分记录
const pointsList = ref<any[]>([])
const loading = ref(false)

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 筛选类型
const activeFilter = ref('all')

// 推荐兑换商品
const recommendItems = ref([
  {
    id: 1,
    name: '5元无门槛优惠券',
    description: '全场通用，无最低消费限制',
    price: 5.00,
    points: 500
  },
  {
    id: 2,
    name: '10元代金券',
    description: '满20元可用，有效期30天',
    price: 10.00,
    points: 1000
  },
  {
    id: 3,
    name: '精美马克杯',
    description: '品牌正品，品质保证',
    price: 29.90,
    points: 3000
  },
  {
    id: 4,
    name: '购物袋',
    description: '环保材质，结实耐用',
    price: 15.80,
    points: 1600
  }
])

// 兑换对话框
const exchangeDialogVisible = ref(false)
const selectedExchangeItem = ref(null)
const exchanging = ref(false)

// 计算今日获得和使用积分
const todayEarned = computed(() => {
  const today = new Date().toDateString()
  return pointsList.value
    .filter((item: any) => {
      const itemDate = new Date(item.createTime).toDateString()
      return item.changeType === 1 && itemDate === today
    })
    .reduce((sum: number, item: any) => sum + item.points, 0)
})

const todayUsed = computed(() => {
  const today = new Date().toDateString()
  return pointsList.value
    .filter((item: any) => {
      const itemDate = new Date(item.createTime).toDateString()
      return item.changeType === 2 && itemDate === today
    })
    .reduce((sum: number, item: any) => sum + item.points, 0)
})

// 获取会员详细信息
const fetchMemberInfo = async () => {
  if (!userStore.user?.id) return

  try {
    const response = await getMemberDetail(userStore.user.id)
    memberInfo.value = (response.data || response) as any
  } catch (error: any) {
    ElMessage.error('获取会员信息失败')
  }
}

// 获取积分记录
const fetchPointsList = async () => {
  if (!userStore.user?.id) return

  loading.value = true
  try {
    const params: any = {
      memberId: userStore.user.id,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      changeType: activeFilter.value === 'all' ? undefined : Number(activeFilter.value)
    }

    const response = await getMemberPointsList(params)
    const data = (response as any).data || response
    pointsList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error: any) {
    ElMessage.error('获取积分记录失败')
  } finally {
    loading.value = false
  }
}

// 切换筛选类型
const handleFilterChange = () => {
  pagination.current = 1
  fetchPointsList()
}

// 显示兑换对话框
const showExchangeDialog = () => {
  exchangeDialogVisible.value = true
}

// 处理兑换
const handleExchange = (item: any) => {
  selectedExchangeItem.value = item
  exchangeDialogVisible.value = true
}

// 确认兑换
const confirmExchange = async () => {
  if (!selectedExchangeItem.value || !userStore.user?.id) return

  if (memberInfo.value.currentPoints < selectedExchangeItem.value.points) {
    ElMessage.error('积分不足，无法兑换')
    return
  }

  exchanging.value = true
  try {
    await exchangePoints({
      memberId: userStore.user.id,
      exchangeItemId: selectedExchangeItem.value.id,
      points: selectedExchangeItem.value.points
    })

    ElMessage.success('兑换成功！')
    exchangeDialogVisible.value = false

    // 重新获取数据
    fetchMemberInfo()
    fetchPointsList()
  } catch (error: any) {
    ElMessage.error(error.message || '兑换失败')
  } finally {
    exchanging.value = false
  }
}

// 获取变更类型文本
const getChangeTypeText = (type: number) => {
  const types = ['获得', '使用', '管理员调整']
  return types[type - 1] || '未知'
}

// 获取变更类型标签类型
const getChangeTypeTag = (type: number) => {
  const types = ['success', 'warning', 'info']
  return types[type - 1] || 'info'
}

// 获取积分样式类
const getPointsClass = (changeType: number) => {
  return changeType === 1 ? 'earned' : 'used'
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.current = 1
  fetchPointsList()
}

// 页码改变
const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchPointsList()
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchMemberInfo()
  fetchPointsList()
})
</script>

<style scoped>
.points-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 概览卡片 */
.points-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.overview-card {
  border-radius: 16px;
  border: none;
  transition: all 0.3s ease;
  overflow: hidden;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}

.overview-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.overview-icon-wrapper {
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

.overview-icon-wrapper.total {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  box-shadow: 0 8px 16px rgba(253, 160, 133, 0.3);
}

.overview-icon-wrapper.history {
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  box-shadow: 0 8px 16px rgba(143, 211, 244, 0.3);
}

.overview-icon-wrapper.earned {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  box-shadow: 0 8px 16px rgba(251, 194, 235, 0.3);
}

.overview-icon-wrapper.used {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
  box-shadow: 0 8px 16px rgba(255, 154, 158, 0.3);
}

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 24px;
  font-weight: 800;
  color: #2c3e50;
  margin-bottom: 4px;
  line-height: 1.2;
}

.overview-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* 卡片通用样式 */
.exchange-card,
.records-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;
  overflow: hidden;
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

/* 兑换区域 */
.exchange-tips {
  margin-bottom: 24px;
}

.custom-alert {
  border-radius: 8px;
}

.tips-list {
  display: flex;
  gap: 24px;
  font-size: 13px;
}

.recommend-items {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommend-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #fff;
}

.recommend-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: #dcdfe6;
}

.item-image {
  height: 140px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.placeholder-icon {
  font-size: 48px;
  color: #dcdfe6;
}

.item-info {
  padding: 16px;
}

.item-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #909399;
  height: 36px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.item-points {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}

.item-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.item-action {
  padding: 0 16px 16px;
}

.exchange-btn {
  width: 100%;
  font-weight: 600;
}

/* 积分记录表格 */
.points-change {
  font-weight: 600;
  font-family: monospace;
  font-size: 15px;
}

.points-change.earned {
  color: #67c23a;
}

.points-change.used {
  color: #f56c6c;
}

.balance-text {
  font-family: monospace;
  color: #606266;
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

/* 兑换确认弹窗 */
.item-preview-large {
  height: 160px;
  background: #f8f9fa;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.placeholder-icon-large {
  font-size: 64px;
  color: #dcdfe6;
}

.confirm-info {
  text-align: center;
}

.confirm-info h4 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #303133;
}

.price-tag {
  display: inline-block;
  background: #fef0f0;
  padding: 4px 12px;
  border-radius: 20px;
  margin-bottom: 16px;
}

.price-tag .points {
  color: #f56c6c;
  font-weight: 700;
  font-size: 16px;
}

.confirm-text {
  color: #606266;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .points-overview,
  .recommend-items {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .points-overview {
    grid-template-columns: 1fr;
  }

  .recommend-items {
    grid-template-columns: 1fr;
  }

  .tips-list {
    flex-direction: column;
    gap: 8px;
  }

  .card-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .card-header .el-radio-group {
    width: 100%;
    display: flex;
  }
  
  .card-header .el-radio-button {
    flex: 1;
  }
  
  .card-header .el-radio-button__inner {
    width: 100%;
  }

  .action-btn {
    width: 100%;
  }
}
</style>