<template>
  <div class="coupons-container">
    <!-- 优惠券概览 -->
    <div class="coupons-overview">
      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper available">
            <el-icon><Ticket /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">{{ availableCoupons }}</div>
            <div class="overview-label">可用优惠券</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper used">
            <el-icon><Check /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">{{ usedCoupons }}</div>
            <div class="overview-label">已使用</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper expired">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">{{ expiredCoupons }}</div>
            <div class="overview-label">已过期</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <div class="overview-content">
          <div class="overview-icon-wrapper total">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-value">{{ totalCoupons }}</div>
            <div class="overview-label">全部优惠券</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 优惠券列表 -->
    <el-card class="coupons-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-icon">
              <el-icon><Present /></el-icon>
            </div>
            <h3>我的优惠券</h3>
          </div>
          <el-radio-group v-model="activeStatus" size="small" @change="handleStatusChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="available">可用</el-radio-button>
            <el-radio-button label="used">已使用</el-radio-button>
            <el-radio-button label="expired">已过期</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="coupons-list" v-loading="loading">
        <div
          v-for="coupon in couponsList"
          :key="coupon.id"
          class="coupon-ticket"
          :class="getCouponClass(coupon)"
        >
          <div class="ticket-left">
            <div class="ticket-value">
              <span class="currency">¥</span>
              <span class="amount">{{ coupon.discountAmount }}</span>
            </div>
            <div class="ticket-condition">满{{ coupon.minAmount }}元可用</div>
            <div class="ticket-hole-top"></div>
            <div class="ticket-hole-bottom"></div>
          </div>

          <div class="ticket-right">
            <div class="ticket-info">
              <div class="ticket-header">
                <h4 class="ticket-name">{{ coupon.couponName }}</h4>
                <el-tag :type="getStatusTagType(coupon.status)" size="small" effect="plain" round>
                  {{ getStatusText(coupon.status) }}
                </el-tag>
              </div>
              <p class="ticket-desc">{{ coupon.description }}</p>
              <div class="ticket-meta">
                <el-icon><Timer /></el-icon>
                <span class="ticket-date">有效期至 {{ formatDate(coupon.expireTime) }}</span>
              </div>
            </div>
            <div class="ticket-action" v-if="coupon.status === 1">
              <el-button type="primary" size="small" round @click="handleUseCoupon(coupon)">
                立即使用
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="couponsList.length === 0 && !loading"
        description="暂无优惠券"
        :image-size="160"
      />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="couponsList.length > 0">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Ticket,
  Check,
  Clock,
  Collection,
  Present,
  Timer
} from '@element-plus/icons-vue'
import { getMemberCoupons } from '@/api/member'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 优惠券列表
const couponsList = ref<any[]>([])
const loading = ref(false)

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 筛选状态
const activeStatus = ref('all')

// 统计数据
const availableCoupons = computed(() => {
  return couponsList.value.filter((item: any) => item.status === 1).length
})

const usedCoupons = computed(() => {
  return couponsList.value.filter((item: any) => item.status === 2).length
})

const expiredCoupons = computed(() => {
  return couponsList.value.filter((item: any) => item.status === 3).length
})

const totalCoupons = computed(() => pagination.total)

// 获取优惠券列表
const fetchCouponsList = async () => {
  if (!userStore.user?.id) return

  loading.value = true
  try {
    const params = {
      memberId: userStore.user.id,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      status: activeStatus.value === 'all' ? undefined : getStatusValue(activeStatus.value)
    }

    const response = await getMemberCoupons(params)
    const data = response.data || response as any
    couponsList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error: any) {
    ElMessage.error('获取优惠券列表失败')
  } finally {
    loading.value = false
  }
}

// 切换状态筛选
const handleStatusChange = () => {
  pagination.current = 1
  fetchCouponsList()
}

// 获取状态值
const getStatusValue = (status: string) => {
  const statusMap: Record<string, number> = {
    'available': 1,
    'used': 2,
    'expired': 3
  }
  return statusMap[status]
}

// 获取优惠券样式类
const getCouponClass = (coupon: any) => {
  return {
    'status-available': coupon.status === 1,
    'status-used': coupon.status === 2,
    'status-expired': coupon.status === 3
  }
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statuses = ['可用', '已使用', '已过期']
  return statuses[status - 1] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const types = ['success', 'info', 'warning']
  return types[status - 1] || 'info'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 使用优惠券
const handleUseCoupon = async (coupon: any) => {
  try {
    await ElMessageBox.confirm('确认使用此优惠券？', '提示', { type: 'warning' })
    const { default: request } = await import('@/utils/request')
    await request.post(`/member/coupons/use/${coupon.id}`)
    ElMessage.success('优惠券使用成功')
    fetchCouponsList()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '使用失败')
  }
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.current = 1
  fetchCouponsList()
}

// 页码改变
const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchCouponsList()
}

onMounted(() => {
  fetchCouponsList()
})
</script>

<style scoped>
.coupons-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 概览卡片 */
.coupons-overview {
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

.overview-icon-wrapper.available {
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  box-shadow: 0 8px 16px rgba(103, 194, 58, 0.3);
}

.overview-icon-wrapper.used {
  background: linear-gradient(135deg, #909399 0%, #73767a 100%);
  box-shadow: 0 8px 16px rgba(144, 147, 153, 0.3);
}

.overview-icon-wrapper.expired {
  background: linear-gradient(135deg, #f56c6c 0%, #d45d5d 100%);
  box-shadow: 0 8px 16px rgba(245, 108, 108, 0.3);
}

.overview-icon-wrapper.total {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.3);
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

/* 列表卡片 */
.coupons-card {
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

/* 优惠券票据样式 */
.coupons-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.coupon-ticket {
  display: flex;
  height: 140px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
}

.coupon-ticket:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.ticket-left {
  width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  border-right: 2px dashed rgba(255, 255, 255, 0.3);
}

.status-available .ticket-left {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
}

.status-used .ticket-left {
  background: linear-gradient(135deg, #909399 0%, #73767a 100%);
}

.status-expired .ticket-left {
  background: linear-gradient(135deg, #f56c6c 0%, #d45d5d 100%);
}

.ticket-value {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.currency {
  font-size: 18px;
  font-weight: 600;
  margin-right: 4px;
}

.amount {
  font-size: 40px;
  font-weight: 800;
  line-height: 1;
}

.ticket-condition {
  font-size: 12px;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 12px;
}

/* 票据圆孔效果 */
.ticket-hole-top,
.ticket-hole-bottom {
  position: absolute;
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  right: -10px;
  z-index: 1;
}

.ticket-hole-top {
  top: -10px;
}

.ticket-hole-bottom {
  bottom: -10px;
}

.ticket-right {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: #fff;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.ticket-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  line-height: 1.4;
}

.ticket-desc {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: auto;
}

.ticket-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
}

.ticket-action {
  margin-top: 12px;
  text-align: right;
}

.status-used .ticket-right,
.status-expired .ticket-right {
  background: #fcfcfc;
}

.status-used .ticket-name,
.status-expired .ticket-name {
  color: #909399;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .coupons-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .coupons-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .coupons-overview {
    grid-template-columns: 1fr;
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

  .coupon-ticket {
    height: auto;
    flex-direction: column;
  }

  .ticket-left {
    width: 100%;
    height: 100px;
    border-right: none;
    border-bottom: 2px dashed rgba(255, 255, 255, 0.3);
  }

  .ticket-hole-top,
  .ticket-hole-bottom {
    display: none;
  }
}
</style>