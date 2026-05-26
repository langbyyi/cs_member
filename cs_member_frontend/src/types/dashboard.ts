// 仪表盘相关类型定义

// 用户信息接口
export interface UserInfo {
  roleCode: 'ADMIN' | 'STORE_MANAGER' | 'STAFF'
  roleName: string
  storeName: string
  storeId: number
}

// 仪表盘统计数据
export interface DashboardStats {
  // 基础字段（所有角色都有）
  totalMembers: number
  newMembers: number
  totalOrders: number
  orderGrowth: number
  activeMembers: number
  activeDecrease: number // 活跃度下降百分比
  todayOrders: number

  // 财务字段（仅ADMIN和STORE_MANAGER有，STAFF为0或空）
  todayRevenue?: number
  revenueGrowth?: number

  // 管理员专属字段（仅ADMIN有）
  totalStores?: number
  todayNewStores?: number

  // 用户信息（所有角色都有）
  userInfo: UserInfo

  // 向后兼容字段
  todayConsumption?: number
  todayNewMembers?: number
  monthRevenue?: number
  monthOrders?: number
  monthNewMembers?: number
  totalRevenue?: number
}

// 营收趋势数据
export interface RevenueTrendData {
  dailyRevenues: Array<{
    date: string
    revenue: number
    orderCount: number
  }>
}

// 会员等级分布数据
export interface MemberLevelDistribution {
  level: number
  levelName: string
  memberCount: number
  percentage: number
}

// 支付方式统计数据
export interface PaymentMethodStats {
  paymentMethods: Record<string, number>
  totalOrders: number
  percentages: Record<string, number>
}

// 消费时段分布数据
export interface ConsumptionTimeDistribution {
  distribution: Record<string, number>
  totalOrders: number
  peakHour: string
  peakHourCount: number
  percentages: Record<string, number>
}

// 最新动态数据
export interface RecentActivity {
  id?: number
  type: string
  description: string
  time: string
  storeName?: string
  amount?: number
  userName?: string
}

// 业务统计数据
export interface BusinessStatistics {
  totalRevenue: number
  totalOrders: number
  averageOrderValue: number
  totalMembers: number
  newMembers: number
  activeMembers: number
  totalPointsIssued: number
  totalPointsConsumed: number
  totalCouponsIssued: number
  totalCouponsUsed: number
  dailyStatistics?: Array<{
    statisticsDate: string
    revenue: number
    orderCount: number
    averageOrderValue: number
    newMemberCount: number
    activeMemberCount: number
    pointsIssued: number
    pointsConsumed: number
  }>
}

// 会员统计数据
export interface MemberStatistics {
  totalMembers: number
  newMembers: number
  activeMembers: number
  memberGrowthRate: number
  memberRetentionRate: number
  levelDistribution: Array<{
    level: number
    levelName: string
    memberCount: number
    percentage: number
  }>
}

// 商品销售统计数据
export interface ProductStatistics {
  totalSales: number
  totalRevenue: number
  averageOrderValue: number
  topProducts: Array<{
    productId: number
    productName: string
    salesCount: number
    revenue: number
    percentage: number
  }>
}

// 仪表板概览数据
export interface DashboardOverview {
  totalRevenue: number
  totalOrders: number
  averageOrderValue: number
  totalMembers: number
  newMembers: number
  activeMembers: number
}

// 实时监控数据
export interface RealTimeMonitoringData {
  onlineUsers: number
  currentOrders: number
  systemLoad: number
  activeConnections: number
  errorCount: number
  responseTime: number
}

// 图表配置接口
export interface ChartPeriodOption {
  label: string
  value: string
}

export interface ChartSeries {
  name: string
  type: 'line' | 'bar'
  data: number[]
  color?: string
  smooth?: boolean
  areaStyle?: boolean
  yAxisIndex?: number
}

export interface ChartData {
  dates: string[]
  series: ChartSeries[]
}

export interface ChartSummaryItem {
  label: string
  value: string
  trend?: number
}

// 统计卡片数据接口
export interface StatCardData {
  title: string
  value: number | string
  type?: 'primary' | 'success' | 'warning' | 'info' | 'danger'
  icon?: string
  unit?: string
  trend?: number
  trendType?: 'percentage' | 'absolute'
  subtitle?: string
  tooltip?: string
  loading?: boolean
  clickable?: boolean
  showProgress?: boolean
  progress?: number
  progressStatus?: 'success' | 'warning' | 'exception'
  extraInfo?: string
  iconSize?: number
  iconColor?: string
  precision?: number
}

// 饼图数据接口
export interface PieChartData {
  name: string
  value: number
  color?: string
}

export interface PieChartLegendData extends PieChartData {
  percentage: number
}

// 实时统计项接口
export interface RealTimeItem {
  label: string
  value: number
  icon: string
  color: string
  unit?: string
  format?: 'number' | 'currency' | 'percentage'
  change?: number
  changed?: boolean
}

// 告警信息接口
export interface Alert {
  level: 'warning' | 'error' | 'info'
  message: string
  time: Date
}

// 时间序列数据接口
export interface TimeSeriesData {
  time: string
  value: number
}

// 仪表盘权限接口
export interface DashboardPermissions {
  canViewRevenue: boolean
  canViewFinancialData: boolean
  canViewStoreStats: boolean
  canViewGlobalData: boolean
  roleDescription: string
}

// 仪表盘刷新状态接口
export interface DashboardRefreshStatus {
  isLoading: boolean
  hasError: boolean
  errorMessage: string
  lastUpdateTime: Date
  retryCount: number
  isRetrying: boolean
}