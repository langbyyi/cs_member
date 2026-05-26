<template>
  <AdminLayout :show-floating-actions="true">
    <div class="dashboard">
    <!-- 动态背景装饰 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="page-header">
      <div class="header-content">
        <div>
          <div class="page-title-wrapper">
            <h1 class="page-title">
              <el-icon class="title-icon"><House /></el-icon>
              仪表盘
              <span v-if="currentUserInfo" class="role-badge">{{ dashboardPermissions.roleDescription }}</span>
            </h1>
            <div class="live-indicator" v-if="dataLoaded">
              <div class="live-dot"></div>
              <span class="live-text">实时数据</span>
            </div>
          </div>
          <p class="page-description">
            欢迎回来，{{ currentUserInfo?.realName || '管理员' }}
            <span v-if="currentUserInfo && currentUserInfo.storeName" class="store-info">
              | {{ currentUserInfo.storeName }}
            </span>
            <span class="time-info">| {{ currentTime }}</span>
          </p>
        </div>
        <div class="header-actions">
          <div class="retry-status" v-if="isRetrying">
            <el-icon class="retry-icon"><Loading /></el-icon>
            <span>正在重试第 {{ retryCount }} 次...</span>
          </div>
          <el-button
            type="primary"
            :icon="Refresh"
            :loading="chartLoading"
            @click="fetchDashboardDataWithRetry"
            class="refresh-btn"
            :disabled="isRetrying"
          >
            {{ chartLoading ? '刷新中...' : (isRetrying ? '重试中...' : '刷新数据') }}
          </el-button>
        </div>
      </div>
      <div class="connection-status" v-if="!dataLoaded">
        <el-alert
          title="数据加载中..."
          type="info"
          :closable="false"
          show-icon
        />
      </div>
      <div class="connection-status" v-else-if="hasError">
        <el-alert
          title="数据加载失败"
          type="error"
          :description="errorMessage"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>{{ errorMessage }}</p>
            <el-button type="primary" size="small" @click="fetchDashboardData" :loading="chartLoading">
              重新加载
            </el-button>
          </template>
        </el-alert>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <EnhancedStatCard
        title="总会员数"
        :value="dataLoaded ? stats.totalMembers : '--'"
        :trend="dataLoaded ? stats.todayNewMembers : undefined"
        :subtitle="dataLoaded && stats.monthNewMembers > 0 ? `本月累计: ${stats.monthNewMembers} 人` : undefined"
        type="primary"
        icon="UserFilled"
        :loading="!dataLoaded"
        :precision="0"
      />

      <EnhancedStatCard
        title="今日订单"
        :value="dataLoaded ? stats.todayOrders : '--'"
        :subtitle="dataLoaded && stats.monthOrders > 0 ? `本月累计: ${stats.monthOrders} 单` : undefined"
        type="success"
        icon="Van"
        :loading="!dataLoaded"
        :precision="0"
      />

      <!-- 营收卡片 - 仅对管理员和门店管理员显示 -->
      <EnhancedStatCard
        v-if="dashboardPermissions.canViewRevenue"
        title="今日营收"
        :value="dataLoaded ? stats.todayRevenue : '--'"
        :subtitle="dataLoaded && stats.monthRevenue > 0 ? `本月累计: ¥${stats.monthRevenue.toLocaleString()}` : undefined"
        type="warning"
        icon="Coin"
        :loading="!dataLoaded"
        :precision="2"
        unit="¥"
      />

      <EnhancedStatCard
        title="活跃会员"
        :value="dataLoaded ? stats.activeMembers : '--'"
        type="info"
        icon="Star"
        :loading="!dataLoaded"
        :precision="0"
        unit="人"
      />
    </div>

    <!-- 实时数据监控 -->
    <RealTimeStats
      :refresh-interval="30"
      :show-chart="true"
      :enable-alerts="true"
      class="real-time-section"
      @refresh="handleRealTimeRefresh"
      @alert="handleRealTimeAlert"
    />

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 营收趋势图表 - 使用新的趋势图表组件 -->
        <el-col :span="16">
          <TrendChart
            :title="dashboardPermissions.canViewRevenue ? '营收趋势' : '订单趋势'"
            :data="{
              dates: revenueData.dates,
              series: [
                {
                  name: '订单数',
                  type: 'line',
                  data: revenueData.orders,
                  smooth: true,
                  color: '#67c23a',
                  yAxisIndex: 1
                },
                ...(dashboardPermissions.canViewRevenue ? [{
                  name: '营收',
                  type: 'line',
                  data: revenueData.revenues,
                  smooth: true,
                  color: '#409eff',
                  areaStyle: true
                }] : [])
              ]
            }"
            :periods="[
              { label: '7天', value: '7d' },
              { label: '30天', value: '30d' },
              { label: '90天', value: '90d' }
            ]"
            :default-period="revenuePeriod"
            :loading="chartLoading"
            :format="dashboardPermissions.canViewRevenue ? 'currency' : 'number'"
            :precision="dashboardPermissions.canViewRevenue ? 2 : 0"
            :show-export="true"
            :show-summary="true"
            @period-change="handleRevenuePeriodChange"
            @chart-click="handleRevenueChartClick"
          />
        </el-col>

        <el-col :span="8">
          <PieChart
            title="会员等级分布"
            :data="memberLevelData"
            :loading="chartLoading"
            :format="'number'"
            :precision="0"
            :show-legend="true"
            :default-chart-type="'doughnut'"
            @chart-click="handleMemberLevelChartClick"
          />
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 支付方式统计 -->
        <el-col :span="12">
          <TrendChart
            title="支付方式统计"
            :data="{
              dates: paymentMethodData.map(item => item.name),
              series: [
                {
                  name: '支付笔数',
                  type: 'bar',
                  data: paymentMethodData.map(item => item.count),
                  color: '#409eff'
                }
              ]
            }"
            :loading="chartLoading"
            :chart-type="'bar'"
            :format="'number'"
            :precision="0"
            :show-chart-type="false"
            :show-period-selector="false"
            :show-zoom="false"
            :height="250"
            @chart-click="handlePaymentChartClick"
          />
        </el-col>

        <!-- 消费时段分布 -->
        <el-col :span="12">
          <TrendChart
            title="消费时段分布"
            :data="{
              dates: consumptionTimeData.timeSlots,
              series: [
                {
                  name: '消费金额',
                  type: 'bar',
                  data: consumptionTimeData.amounts,
                  color: '#67c23a'
                }
              ]
            }"
            :loading="chartLoading"
            :chart-type="'bar'"
            :format="'currency'"
            :precision="0"
            :show-chart-type="false"
            :show-period-selector="false"
            :show-zoom="false"
            :height="250"
            @chart-click="handleConsumptionChartClick"
          />
        </el-col>
      </el-row>

      <!-- 会员增长趋势 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <TrendChart
            title="会员增长趋势"
            :data="{
              dates: memberGrowthData.dates,
              series: [
                {
                  name: '新增会员',
                  type: 'bar',
                  data: memberGrowthData.newMembers,
                  color: '#409eff',
                  yAxisIndex: 1
                },
                {
                  name: '累计会员',
                  type: 'line',
                  data: memberGrowthData.totalMembers,
                  smooth: true,
                  color: '#67c23a',
                  areaStyle: true
                },
                {
                  name: '活跃会员',
                  type: 'line',
                  data: memberGrowthData.activeMembers,
                  smooth: true,
                  color: '#e6a23c'
                }
              ]
            }"
            :periods="[
              { label: '7天', value: '7d' },
              { label: '30天', value: '30d' },
              { label: '90天', value: '90d' }
            ]"
            default-period="7d"
            :loading="memberGrowthLoading"
            :format="'number'"
            :precision="0"
            :show-export="true"
            :show-summary="true"
            :height="350"
            @period-change="handleMemberGrowthPeriodChange"
            @chart-click="handleMemberGrowthChartClick"
          />
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作和最新动态 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <QuickActions />
      </el-col>

      <el-col :span="16">
        <ActivityFeed
          :activities="recentActivities"
          :loading="chartLoading"
          @refresh="refreshActivities"
          @activity-click="handleActivityClick"
          @action="handleActivityAction"
        />
      </el-col>
    </el-row>
  </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Loading, UserFilled, Van, Coin, Star, Ticket, User, ShoppingCart, Medal, InfoFilled, House } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getDashboardStats,
  getRevenueTrend,
  getMemberLevelDistribution,
  getPaymentMethodStats,
  getConsumptionTimeDistribution,
  getRecentActivities,
  getMemberStatistics,
  type UserInfo
} from '@/api/dashboard'

// 导入布局组件
import AdminLayout from '@/components/admin/AdminLayout.vue'
// 导入自定义组件
import StatCard from '@/components/dashboard/StatCard.vue'
import EnhancedStatCard from '@/components/admin/EnhancedStatCard.vue'
import TrendChart from '@/components/dashboard/TrendChart.vue'
import PieChart from '@/components/dashboard/PieChart.vue'
import RealTimeStats from '@/components/dashboard/RealTimeStats.vue'
import QuickActions from '@/components/dashboard/QuickActions.vue'
import ActivityFeed from '@/components/dashboard/ActivityFeed.vue'
import type { RecentActivity } from '@/types/dashboard'

// 响应式数据
const revenuePeriod = ref('7d')
const memberGrowthPeriod = ref('7d')
const chartLoading = ref(false)
const memberGrowthLoading = ref(false)
const dataLoaded = ref(false)
const hasError = ref(false)
const errorMessage = ref('')

// 重试机制相关
const retryCount = ref(0)
const maxRetries = 3
const retryDelay = 2000 // 2秒
const isRetrying = ref(false)

// 自动刷新相关
const autoRefreshEnabled = ref(true)
const autoRefreshInterval = ref(5 * 60 * 1000) // 5分钟
let autoRefreshTimer: NodeJS.Timeout | null = null

// 用户角色信息
const currentUserInfo = ref<UserInfo | null>(null)

// 当前时间
const currentTime = ref('')
let timeTimer: NodeJS.Timeout | null = null

// 角色权限计算
const dashboardPermissions = computed(() => {
  if (!currentUserInfo.value) {
    return {
      canViewRevenue: false,
      canViewFinancialData: false,
      canViewStoreStats: false,
      canViewGlobalData: false,
      roleDescription: '未知角色'
    }
  }

  const { roleCode } = currentUserInfo.value

  switch (roleCode) {
    case 'ADMIN':
      return {
        canViewRevenue: true,
        canViewFinancialData: true,
        canViewStoreStats: true,
        canViewGlobalData: true,
        roleDescription: '系统管理员'
      }
    case 'STORE_MANAGER':
      return {
        canViewRevenue: true,
        canViewFinancialData: true,
        canViewStoreStats: false,
        canViewGlobalData: false,
        roleDescription: '门店管理员'
      }
    case 'STAFF':
      return {
        canViewRevenue: false,
        canViewFinancialData: false,
        canViewStoreStats: false,
        canViewGlobalData: false,
        roleDescription: '普通员工'
      }
    default:
      return {
        canViewRevenue: false,
        canViewFinancialData: false,
        canViewStoreStats: false,
        canViewGlobalData: false,
        roleDescription: '未知角色'
      }
  }
})

// 统计数据
const stats = reactive({
  // 基础字段（所有角色都有）
  totalMembers: 0,
  newMembers: 0,
  totalOrders: 0,
  orderGrowth: 0,
  activeMembers: 0,
  todayOrders: 0,

  // 财务字段（仅ADMIN和STORE_MANAGER有，STAFF为0或空）
  todayRevenue: 0,
  revenueGrowth: 0,

  // 管理员专属字段（仅ADMIN有）
  totalStores: 0,
  todayNewStores: 0,

  // 其他字段
  todayConsumption: 0,
  todayNewMembers: 0,
  monthRevenue: 0,
  monthOrders: 0,
  monthNewMembers: 0,
  totalRevenue: 0
})

// 图表数据
const revenueData = ref<{
  dates: string[]
  revenues: number[]
  orders: number[]
}>({
  dates: [],
  revenues: [],
  orders: []
})

const memberLevelData = ref<Array<{
  name: string
  value: number
  color: string
}>>([])

const paymentMethodData = ref<Array<{
  name: string
  count: number
  percentage?: number
}>>([])

const consumptionTimeData = ref<{
  timeSlots: string[]
  amounts: number[]
  peakHour?: string
  peakHourCount?: number
}>({
  timeSlots: [],
  amounts: []
})

// 会员增长数据
const memberGrowthData = ref<{
  dates: string[]
  newMembers: number[]
  totalMembers: number[]
  activeMembers: number[]
}>({
  dates: [],
  newMembers: [],
  totalMembers: [],
  activeMembers: []
})

// 最新动态
const recentActivities = ref<Array<{
  id?: number
  type: string
  description: string
  time: string
  storeName?: string
  amount?: number
  userName?: string
}>>([])

function getActivityIcon(type: string): string {
  const iconMap: Record<string, string> = {
    member: 'User',
    consumption: 'ShoppingCart',
    points: 'Medal',
    coupon: 'Ticket'
  }
  return iconMap[type] || 'InfoFilled'
}

// 图表事件处理方法
const handleRevenuePeriodChange = (period: string) => {
  revenuePeriod.value = period
  onPeriodChange()
}

const handleRevenueChartClick = (params: any) => {
  // 可以在这里添加更多处理逻辑，比如跳转到详细页面
}

const handleMemberLevelChartClick = (params: any) => {
}

const handlePaymentChartClick = (params: any) => {
}

const handleConsumptionChartClick = (params: any) => {
}

const handleMemberGrowthPeriodChange = (period: string) => {
  memberGrowthPeriod.value = period
  fetchMemberGrowthData()
}

const handleMemberGrowthChartClick = (params: any) => {
}

// 实时数据事件处理
const handleRealTimeRefresh = (data: any) => {
}

const handleRealTimeAlert = (alert: any) => {
  ElMessage.warning(`系统告警: ${alert.message}`)
}

// 活动相关事件处理
const handleActivityClick = (activity: RecentActivity) => {
  ElMessage.info(`查看活动详情: ${activity.description}`)
}

const handleActivityAction = (action: { action: string; activity: RecentActivity }) => {
  switch (action.action) {
    case 'view':
      ElMessage.info(`查看活动详情: ${action.activity.description}`)
      break
    case 'related':
      ElMessage.info(`查看相关记录: ${action.activity.description}`)
      break
  }
}


// 数据获取函数
async function fetchDashboardData() {
  try {
    chartLoading.value = true
    hasError.value = false
    errorMessage.value = ''
    dataLoaded.value = false

    // 检查网络连接
    if (!navigator.onLine) {
      throw new Error('网络连接已断开，请检查网络设置')
    }

    // 并行获取所有数据
    const [
      statsResponse,
      revenueResponse,
      memberLevelResponse,
      paymentMethodResponse,
      consumptionTimeResponse,
      activitiesResponse
    ] = await Promise.all([
      getDashboardStats(),
      getRevenueTrend(revenuePeriod.value.replace('d', '')),
      getMemberLevelDistribution(),
      getPaymentMethodStats(),
      getConsumptionTimeDistribution(),
      getRecentActivities()
    ])

    // 并行获取会员增长数据
    fetchMemberGrowthData()

    // 更新用户信息和统计数据 - 完全适配新的角色差异化API
    const responseData = statsResponse.data

    // 提取并保存用户信息（根据API文档新格式）
    if (responseData && typeof responseData === 'object' && 'userInfo' in responseData) {
      currentUserInfo.value = (responseData as any).userInfo || null
    } else if (responseData && typeof responseData === 'object' && 'roleCode' in responseData) {
      // 如果userInfo直接在根级别
      currentUserInfo.value = responseData as UserInfo
    } else {
      currentUserInfo.value = null
    }

    // 根据API文档，基础统计数据对所有角色都返回
    stats.totalMembers = responseData?.totalMembers || 0
    stats.newMembers = responseData?.newMembers || 0
    stats.totalOrders = responseData?.totalOrders || 0
    stats.orderGrowth = responseData?.orderGrowth || 0
    stats.activeMembers = responseData?.activeMembers || 0
    stats.todayOrders = responseData?.todayOrders || 0

    // 财务字段 - 根据API文档，ADMIN和STORE_MANAGER有真实数据，STAFF返回0
    if (dashboardPermissions.value.canViewRevenue) {
      stats.todayRevenue = responseData?.todayRevenue || 0
      stats.revenueGrowth = responseData?.revenueGrowth || 0
    } else {
      // STAFF角色：API返回0，前端也设置为0
      stats.todayRevenue = 0
      stats.revenueGrowth = 0
    }

    // 管理员专属字段 - 根据API文档，仅ADMIN有
    if (dashboardPermissions.value.canViewStoreStats) {
      stats.totalStores = responseData?.totalStores || 0
      stats.todayNewStores = responseData?.todayNewStores || 0
    } else {
      // STORE_MANAGER和STAFF角色：API不返回这些字段或返回null
      stats.totalStores = 0
      stats.todayNewStores = 0
    }

    // 其他统计字段（根据API文档）
    stats.todayConsumption = responseData?.todayConsumption || 0
    stats.todayNewMembers = responseData?.todayNewMembers || stats.newMembers
    stats.monthRevenue = responseData?.monthRevenue || (stats.todayRevenue || 0)
    stats.monthOrders = responseData?.monthOrders || 0
    stats.monthNewMembers = responseData?.monthNewMembers || 0
    stats.totalRevenue = responseData?.totalRevenue || (stats.todayRevenue || 0)

    // 更新图表数据 - 根据API文档适配角色差异化响应
    const revenueResponseData = revenueResponse.data

    // 营收趋势数据 - 根据角色权限处理
    if (revenueResponseData && Array.isArray(revenueResponseData.dailyRevenues)) {
      const dailyRevenues = revenueResponseData.dailyRevenues

      // 对于STAFF角色，revenue字段会是0或null，前端需要处理
      revenueData.value = {
        dates: dailyRevenues.map((item: any) => item.date),
        revenues: dailyRevenues.map((item: any) =>
          dashboardPermissions.value.canViewRevenue ? (item.revenue || 0) : 0
        ),
        orders: dailyRevenues.map((item: any) => item.orderCount || 0)
      }
    } else {
      revenueData.value = { dates: [], revenues: [], orders: [] }
    }

    // 处理会员等级数据，添加颜色
    const memberLevels = memberLevelResponse.data || []

    if (Array.isArray(memberLevels) && memberLevels.length > 0) {
      memberLevelData.value = memberLevels.map((item: any, index: number) => ({
        name: item.levelName,
        value: item.memberCount,
        color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'][index] || '#909399'
      }))
    } else {
      memberLevelData.value = []
    }

    // 处理支付方式数据
    const paymentMethods = paymentMethodResponse.data?.paymentMethods || {}
    const percentages = paymentMethodResponse.data?.percentages || {}
    paymentMethodData.value = Object.entries(paymentMethods).map(([name, count]) => ({
      name,
      count: count as number,
      percentage: percentages[name] as number
    }))

    // 处理消费时段数据
    const distribution = consumptionTimeResponse.data?.distribution || {}
    consumptionTimeData.value = {
      timeSlots: Object.keys(distribution),
      amounts: Object.values(distribution) as number[],
      peakHour: consumptionTimeResponse.data?.peakHour,
      peakHourCount: consumptionTimeResponse.data?.peakHourCount
    }

    // 处理最新动态数据
    const activities = activitiesResponse.data || []

    if (Array.isArray(activities)) {
      recentActivities.value = activities.map((activity, index) => ({
        id: activity.id || index, // 如果没有id字段，使用索引作为唯一标识
        type: activity.type || 'info',
        description: activity.description || '未知操作',
        time: activity.time || new Date().toLocaleString(),
        storeName: activity.storeName,
        amount: activity.amount,
        userName: activity.userName
      }))
    } else {
      recentActivities.value = []
    }

    dataLoaded.value = true

  } catch (error: any) {

    // 增强错误处理
    if (error.message?.includes('网络连接已断开')) {
      errorMessage.value = error.message
    } else if (error.response?.status === 401) {
      errorMessage.value = '登录已过期，请重新登录'
      // 可以在这里添加自动跳转到登录页的逻辑
      setTimeout(() => {
        window.location.href = '/login'
      }, 3000)
    } else if (error.response?.status === 403) {
      errorMessage.value = '权限不足，无法访问仪表盘数据'
    } else if (error.response?.status === 404) {
      errorMessage.value = '请求的接口不存在，请联系系统管理员'
    } else if (error.response?.status === 500) {
      errorMessage.value = '服务器内部错误，请稍后重试或联系管理员'
    } else if (error.response?.status >= 500) {
      errorMessage.value = '服务器暂时不可用，请稍后重试'
    } else if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      errorMessage.value = '请求超时，请检查网络连接后重试'
    } else if (error.code === 'ERR_NETWORK' || !navigator.onLine) {
      errorMessage.value = '网络连接失败，请检查网络设置'
    } else {
      errorMessage.value = '数据加载失败，请稍后重试'
    }

    // 设置错误状态
    hasError.value = true
    dataLoaded.value = false


    // 可选：发送错误报告到监控系统
    // sendErrorReport(errorInfo)
  } finally {
    chartLoading.value = false
  }
}

// 带重试机制的数据获取
async function fetchDashboardDataWithRetry() {
  try {
    await fetchDashboardData()
    retryCount.value = 0 // 重置重试计数
    startAutoRefresh() // 启动自动刷新
  } catch (error) {

    // 重试逻辑
    if (retryCount.value < maxRetries && !isRetrying.value) {
      isRetrying.value = true
      retryCount.value++


      setTimeout(async () => {
        try {
          await fetchDashboardData()
          retryCount.value = 0
          isRetrying.value = false
          ElMessage.success('数据加载成功')
          startAutoRefresh()
        } catch (retryError) {
          isRetrying.value = false

          if (retryCount.value >= maxRetries) {
            // 错误已由全局拦截器处理
          }
        }
      }, retryDelay * retryCount.value) // 递增延迟
    } else if (retryCount.value >= maxRetries) {
      // 错误已由全局拦截器处理
    }
  }
}

// 启动自动刷新
function startAutoRefresh() {
  stopAutoRefresh() // 先停止现有的定时器

  if (autoRefreshEnabled.value && dataLoaded.value) {
    autoRefreshTimer = setInterval(() => {
      if (document.visibilityState === 'visible') { // 只在页面可见时刷新
        fetchDashboardData()
      }
    }, autoRefreshInterval.value)
  }
}

// 停止自动刷新
function stopAutoRefresh() {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

// 监听页面可见性变化
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible' && dataLoaded.value) {
    // 页面重新可见时刷新数据
    fetchDashboardData()
  }
})

// 监听网络状态变化
window.addEventListener('online', () => {
  ElMessage.success('网络连接已恢复')
  if (hasError.value) {
    fetchDashboardDataWithRetry()
  }
})

window.addEventListener('offline', () => {
  ElMessage.warning('网络连接已断开')
  stopAutoRefresh()
})

// 监听时间周期变化 - 适配角色差异化响应
async function onPeriodChange() {
  try {
    chartLoading.value = true
    const revenueResponse = await getRevenueTrend(revenuePeriod.value.replace('d', ''))
    const revenueResponseData = revenueResponse.data

    if (revenueResponseData && Array.isArray(revenueResponseData.dailyRevenues)) {
      const dailyRevenues = revenueResponseData.dailyRevenues

      // 根据角色权限处理营收数据
      revenueData.value = {
        dates: dailyRevenues.map((item: any) => item.date),
        revenues: dailyRevenues.map((item: any) =>
          dashboardPermissions.value.canViewRevenue ? (item.revenue || 0) : 0
        ),
        orders: dailyRevenues.map((item: any) => item.orderCount || 0)
      }
    } else {
      revenueData.value = { dates: [], revenues: [], orders: [] }
    }
  } catch (error) {
    // 营收趋势数据获取失败，静默处理
  } finally {
    chartLoading.value = false
  }
}

// 获取会员增长数据
async function fetchMemberGrowthData() {
  try {
    memberGrowthLoading.value = true
    const response = await getMemberStatistics({
      startDate: getStartDate(memberGrowthPeriod.value),
      endDate: new Date().toISOString().split('T')[0]
    })

    if (response.data) {
      const statistics = response.data
      // 使用会员统计数据，如果没有详细增长数据，则使用汇总数据
      const today = formatDate(new Date())
      const lastMonth = formatDate(new Date(Date.now() - 29 * 24 * 60 * 60 * 1000))

      memberGrowthData.value = {
        dates: [lastMonth, today],
        newMembers: [Math.floor(statistics.newMembers / 30), statistics.newMembers], // 近似日均新增会员
        totalMembers: [statistics.totalMembers - statistics.newMembers, statistics.totalMembers],
        activeMembers: [Math.floor(statistics.activeMembers / 30), statistics.activeMembers]
      }
    } else {
      memberGrowthData.value = {
        dates: [],
        newMembers: [],
        totalMembers: [],
        activeMembers: []
      }
    }
  } catch (error) {
    memberGrowthData.value = {
      dates: [],
      newMembers: [],
      totalMembers: [],
      activeMembers: []
    }
  } finally {
    memberGrowthLoading.value = false
  }
}

function refreshActivities() {
  fetchDashboardData()
}

// 更新当前时间
function updateCurrentTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 启动时间更新
function startTimeUpdate() {
  updateCurrentTime()
  timeTimer = setInterval(updateCurrentTime, 1000)
}

// 停止时间更新
function stopTimeUpdate() {
  if (timeTimer) {
    clearInterval(timeTimer)
    timeTimer = null
  }
}

onMounted(() => {
  fetchDashboardDataWithRetry()
  startTimeUpdate()
})

onUnmounted(() => {
  stopAutoRefresh()
  stopTimeUpdate()
  // 清理事件监听器
  document.removeEventListener('visibilitychange', () => {})
  window.removeEventListener('online', () => {})
  window.removeEventListener('offline', () => {})
})

// 格式化日期
function formatDate(date: Date): string {
  return date.toISOString().split('T')[0]
}

// 获取开始日期
function getStartDate(period: string): string {
  const now = new Date()
  const days = parseInt(period.replace('d', ''))
  const startDate = new Date(now.getTime() - days * 24 * 60 * 60 * 1000)
  return formatDate(startDate)
}

// 监听时间周期变化
watch(revenuePeriod, onPeriodChange)
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f0f2f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 20px;
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

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.page-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #409eff;
  font-size: 32px;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(103, 194, 58, 0.1);
  border-radius: 16px;
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.role-badge {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  opacity: 0.9;
}

.refresh-btn {
  margin-top: 8px;
}

.retry-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 12px;
  padding: 6px 12px;
  background: #f0f9ff;
  border: 1px solid #bae7ff;
  border-radius: 16px;
  font-size: 13px;
  color: #1890ff;
}

.retry-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
  line-height: 1.5;
}

.store-info {
  color: #409eff;
  font-weight: 500;
}

.time-info {
  color: #909399;
  font-size: 14px;
}

.connection-status {
  margin-top: 16px;
}

.connection-status :deep(.el-alert) {
  border-radius: 8px;
}

.connection-status :deep(.el-alert__content) {
  width: 100%;
}

.connection-status :deep(.el-alert__description) {
  margin: 8px 0 12px 0;
  line-height: 1.5;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.real-time-section {
  margin-bottom: 24px;
}


.charts-section {
  margin-bottom: 24px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}

@media (max-width: 992px) {
  /* 中等屏幕布局调整 */
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-actions {
    display: flex;
    justify-content: flex-end;
  }

  /* 图表区域调整 */
  .charts-section .el-col:first-child {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .real-time-section {
    margin-bottom: 16px;
  }

  .charts-section {
    margin-bottom: 16px;
  }

  /* 图表在小屏幕上的调整 */
  .charts-section .el-col {
    margin-bottom: 16px;
  }

  .quick-actions {
    grid-template-columns: 1fr 1fr;
  }

  .action-btn {
    height: 40px;
    font-size: 13px;
  }

  /* 页面标题区域调整 */
  .page-title-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .page-title {
    font-size: 24px;
    gap: 8px;
  }

  .title-icon {
    font-size: 24px;
  }

  .live-indicator {
    order: 1;
    margin-left: 0;
  }

  .role-badge {
    font-size: 12px;
    padding: 2px 8px;
  }

  .page-description {
    font-size: 14px;
    flex-direction: column;
    gap: 4px;
  }

  .time-info {
    display: none;
  }
}

@media (max-width: 576px) {
  /* 超小屏幕优化 */
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .header-content {
    gap: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .page-description {
    font-size: 13px;
  }

  .refresh-btn {
    font-size: 13px;
    padding: 8px 16px;
  }

  /* 连接状态提示优化 */
  .connection-status {
    margin-top: 12px;
  }

  .connection-status :deep(.el-alert) {
    padding: 12px;
  }

  .connection-status :deep(.el-alert__title) {
    font-size: 14px;
  }

  .connection-status :deep(.el-alert__description) {
    font-size: 12px;
    margin: 4px 0 8px 0;
  }


  /* 实时数据监控优化 */
  .real-time-stats :deep(.real-time-stats__grid) {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .real-time-stats :deep(.real-time-stats__item) {
    padding: 12px;
  }

  .real-time-stats :deep(.real-time-stats__value) {
    font-size: 18px;
  }
}

/* 打印样式 */
@media print {
  .dashboard {
    padding: 0;
  }

  .header-actions,
  .real-time-section {
    display: none !important;
  }

  .charts-section {
    break-inside: avoid;
  }

  .stat-card {
    break-inside: avoid;
    page-break-inside: avoid;
  }

  .quick-actions,
  .activity-feed {
    break-inside: avoid;
    page-break-inside: avoid;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .stat-card {
    border: 2px solid #000;
  }

  .chart-card {
    border: 2px solid #000;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .chart-card,
  .activity-item,
  .action-btn {
    transition: none;
  }

  .stat-card:hover {
    transform: none;
  }

  .action-btn:hover {
    transform: none;
  }

  .real-time-stats__pulse {
    animation: none;
  }
}
</style>