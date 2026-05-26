import request from '@/utils/request'
import type {
  PointsRecord,
  PageParams,
  PageResponse
} from '@/types'

// 导出类型定义
export type {
  PointsRecord
} from '@/types'

// 获取当前会员的积分余额
export function getMemberPointsBalance() {
  return request.get<{
    totalPoints: number
    availablePoints: number
    level: number
    levelName: string
    nextLevelPoints: number
    totalEarned: number
    totalUsed: number
  }>('/member/points/balance')
}

// 获取当前会员的积分记录
export function getMemberPointsRecords(params: PageParams & {
  changeType?: number
  startDate?: string
  endDate?: string
}) {
  return request.get<PageResponse<PointsRecord>>('/member/points/records', { params })
}

// 获取积分兑换商品列表
export function getExchangeItems() {
  return request.get<Array<{
    id: number
    name: string
    points: number
    stock: number
    description?: string
    imageUrl?: string
  }>>('/member/points/exchange-items')
}

// 积分兑换
export function exchangePoints(itemId: number, points: number) {
  return request.post<boolean>('/member/points/exchange', {
    itemId,
    points
  })
}

// 获取积分统计信息
export function getPointsStatistics() {
  return request.get<{
    thisMonthEarned: number
    thisMonthUsed: number
    totalEarned: number
    totalUsed: number
    expiredPoints: number
    expiringSoonPoints: number
  }>('/member/points/statistics')
}

// ==================== 管理员积分管理接口 ====================

// 调整会员积分
export function adjustMemberPoints(memberId: number, params: {
  points: number
  reason: string
  remark?: string
}) {
  return request.post<string>('/admin/points/adjust', null, {
    params: {
      memberId,
      ...params
    }
  })
}

// 获取会员积分记录（管理员视角）
export function getMemberPointsRecordsAdmin(memberId: number, params: PageParams & {
  changeType?: number
  startDate?: string
  endDate?: string
}) {
  return request.get<PageResponse<PointsRecord>>(`/admin/points/records/${memberId}`, { params })
}

// 批量调整积分
export function batchAdjustPoints(adjustments: Array<{
  memberId: number
  points: number
  reason: string
  remark?: string
}>) {
  return request.post<string>('/admin/points/batch-adjust', adjustments)
}

// 获取积分操作类型列表
export function getPointsOperationTypes() {
  return request.get<Array<{
    value: number
    label: string
    description: string
  }>>('/admin/points/operation-types')
}

// ==================== 管理员积分查询接口 ====================

// 获取积分列表（管理员视角）
export function getPointsList(params: PageParams & {
  memberId?: number
  memberNo?: string
  changeType?: number
  startDate?: string
  endDate?: string
}) {
  return request.get<PageResponse<PointsRecord>>('/admin/points/list', { params })
}

// 获取会员积分统计信息（管理员视角）
export function getMemberStatistics(memberId?: number) {
  const url = memberId ? `/admin/points/statistics/${memberId}` : '/admin/points/statistics'
  return request.get<{
    totalMembers: number
    totalPoints: number
    averagePoints: number
    activeMembers: number
    thisMonthEarned: number
    thisMonthUsed: number
    topMembers: Array<{
      id: number
      name: string
      memberNo: string
      currentPoints: number
      totalPoints: number
    }>
  }>(url)
}