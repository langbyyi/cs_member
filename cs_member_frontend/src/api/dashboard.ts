import request from '@/utils/request'

// 用户信息接口
export interface UserInfo {
  roleCode: 'ADMIN' | 'STORE_MANAGER' | 'STAFF',
  roleName: string,
  name?: string,
  realName?: string,
  storeName: string,
  storeId: number
}

// 获取仪表盘统计数据 - 支持角色差异化
export function getDashboardStats() {
  return request.get<{
    // 基础字段（所有角色都有）
    totalMembers: number,
    newMembers: number,
    totalOrders: number,
    orderGrowth: number,
    activeMembers: number,
    activeDecrease: number, // 新增：活跃度下降百分比

    // 财务字段（仅ADMIN和STORE_MANAGER有，STAFF为0或空）
    todayRevenue?: number,
    revenueGrowth?: number,

    // 管理员专属字段（仅ADMIN有）
    totalStores?: number,
    todayNewStores?: number,

    // 用户信息（所有角色都有）
    userInfo: UserInfo,

    // 向后兼容字段
    todayConsumption?: number,
    todayNewMembers?: number,
    monthRevenue?: number,
    monthOrders?: number,
    monthNewMembers?: number
  }>('/admin/dashboard/stats')
}

// 获取营收趋势数据
export function getRevenueTrend(period: string = '7') {
  return request.get<{
    dailyRevenues: Array<{
      date: string,
      revenue: number,
      orderCount: number
    }>
  }>('/admin/dashboard/revenue-trend', {
    params: { period }
  })
}

// 获取会员等级分布
export function getMemberLevelDistribution() {
  return request.get<Array<{
    level: number,
    levelName: string,
    memberCount: number,
    percentage: number
  }>>('/admin/dashboard/member-level-distribution')
}

// 获取支付方式统计
export function getPaymentMethodStats() {
  return request.get<{
    paymentMethods: Record<string, number>,
    totalOrders: number,
    percentages: Record<string, number>
  }>('/admin/dashboard/payment-method-stats')
}

// 获取消费时段分布
export function getConsumptionTimeDistribution() {
  return request.get<{
    distribution: Record<string, number>,
    totalOrders: number,
    peakHour: string,
    peakHourCount: number,
    percentages: Record<string, number>
  }>('/admin/dashboard/consumption-time-distribution')
}

// 获取最新动态 - 支持角色差异化
export function getRecentActivities() {
  return request.get<Array<{
    type: string,
    description: string,
    time: string,
    // 扩展字段支持不同角色的活动类型
    storeName?: string, // 管理员可能看到门店信息
    amount?: number, // 管理员可能看到金额信息
    userName?: string // 用户相关信息
  }>>('/admin/dashboard/recent-activities')
}

// 获取业务统计数据 - 支持时间范围查询（路径修正为GET方法）
export function getBusinessStatistics(params?: {
  startDate?: string
  endDate?: string
  storeId?: number
  statisticsType?: string
  groupBy?: string
}) {
  return request.get<{
    totalRevenue: number,
    totalOrders: number,
    averageOrderValue: number,
    totalMembers: number,
    newMembers: number,
    activeMembers: number,
    totalPointsIssued: number,
    totalPointsConsumed: number,
    totalCouponsIssued: number,
    totalCouponsUsed: number,
    dailyStatistics?: Array<{
      statisticsDate: string,
      revenue: number,
      orderCount: number,
      averageOrderValue: number,
      newMemberCount: number,
      activeMemberCount: number,
      pointsIssued: number,
      pointsConsumed: number
    }>
  }>('/admin/statistics/business', {
    params
  })
}

// 获取会员统计数据 - 新增接口
export function getMemberStatistics(params?: {
  startDate?: string
  endDate?: string
  storeId?: number
}) {
  return request.get<{
    totalMembers: number,
    newMembers: number,
    activeMembers: number,
    memberGrowthRate: number,
    memberRetentionRate: number,
    levelDistribution: Array<{
      level: number,
      levelName: string,
      memberCount: number,
      percentage: number
    }>
  }>('/admin/statistics/members', {
    params
  })
}

// 获取商品销售统计 - 新增接口
export function getProductStatistics(params?: {
  startDate?: string
  endDate?: string
  storeId?: number
}) {
  return request.get<{
    totalSales: number,
    totalRevenue: number,
    averageOrderValue: number,
    topProducts: Array<{
      productId: number,
      productName: string,
      salesCount: number,
      revenue: number,
      percentage: number
    }>
  }>('/admin/statistics/products', {
    params
  })
}

// 获取仪表板概览数据 - 新增接口
export function getDashboardOverview(params?: {
  startDate?: string
  endDate?: string
  storeId?: number
}) {
  return request.get<{
    totalRevenue: number,
    totalOrders: number,
    averageOrderValue: number,
    totalMembers: number,
    newMembers: number,
    activeMembers: number
  }>('/admin/statistics/dashboard', {
    params
  })
}

// 获取月度趋势数据 - 新增接口
export function getMonthlyTrend(months: number = 12) {
  return request.get<{
    revenueTrend: Array<{ month: string; orderCount: number; revenue: number }>
    memberTrend: Array<{ month: string; newMembers: number }>
    pointsTrend: Array<{ month: string; earned: number; used: number }>
  }>('/admin/statistics/monthly-trend', {
    params: { months }
  })
}