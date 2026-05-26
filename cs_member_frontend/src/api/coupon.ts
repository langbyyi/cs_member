import request from '@/utils/request'
import type {
  PageParams,
  PageResponse
} from '@/types'

// 获取当前会员的优惠券列表
export function getMemberCoupons(params: PageParams & {
  status?: number
  couponType?: number
}) {
  return request.get<PageResponse<{
    id: number
    name: string
    discountAmount?: number
    discountPercent?: number
    minAmount?: number
    validUntil: string
    status: number
    couponType: number
  }>>('/member/coupons/my-coupons', { params })
}

// 获取可用的优惠券列表（用于下单选择）
export function getAvailableCoupons(orderAmount?: number) {
  return request.get<Array<{
    id: number
    name: string
    discountAmount: number
    minAmount: number
    validUntil: string
  }>>('/member/coupons/available', {
    params: { orderAmount }
  })
}

// 使用优惠券
export function useCoupon(couponId: number, orderNo: string, orderAmount: number) {
  return request.post<boolean>(`/member/coupons/${couponId}/use`, null, {
    params: { orderNo, orderAmount }
  })
}

// 获取优惠券统计信息
export function getCouponsStatistics() {
  return request.get<{
    totalReceived: number
    available: number
    used: number
    expired: number
    totalSaved: number
    thisMonthReceived: number
    thisMonthUsed: number
  }>('/member/coupons/statistics')
}

// 获取即将过期的优惠券
export function getExpiringSoonCoupons() {
  return request.get<Array<{
    id: number
    name: string
    discountAmount: number
    validUntil: string
    daysLeft: number
  }>>('/member/coupons/expiring-soon')
}

// ==================== 管理端接口 ====================

export interface Coupon {
  id: number
  couponName: string
  couponType: number
  minAmount: number
  discountAmount: number
  totalQuantity: number
  remainingQuantity: number
  usedQuantity: number
  limitPerUser: number
  validType: number
  validDays?: number
  validStartTime?: string
  validEndTime?: string
  status: number
  description?: string
  createdTime: string
}

// 获取优惠券列表（管理端）
export function getCouponList(params: PageParams & {
  couponName?: string
  couponType?: number
  status?: number
  issueType?: number
  validStartTime?: string
  validEndTime?: string
  sortBy?: string
  sortOrder?: string
}) {
  return request.get<PageResponse<Coupon>>('/admin/coupons/page', { params })
}

// 获取优惠券详情（管理端）
export function getCouponDetail(id: number) {
  return request.get<Coupon>(`/admin/coupons/${id}`)
}

// 创建优惠券
export function createCoupon(data: any) {
  return request.post<boolean>('/admin/coupons', data)
}

// 更新优惠券
export function updateCoupon(id: number, data: any) {
  return request.put<boolean>(`/admin/coupons/${id}`, data)
}

// 更新优惠券状态
export function updateCouponStatus(id: number, status: number) {
  return request.put<boolean>(`/admin/coupons/${id}/status/${status}`)
}

// 删除优惠券
export function deleteCoupon(id: number) {
  return request.delete<boolean>(`/admin/coupons/${id}`)
}

// 发放优惠券（单个）
export function issueCoupon(couponId: number, memberId: number) {
  return request.post<boolean>('/admin/coupons/issue', { couponId, memberId })
}

// 批量发放优惠券
export function batchIssueCoupon(data: {
  couponId: string | number
  targetType: number
  memberLevel?: number
  memberIds?: number[]
  quantity: number
  reason: string
}) {
  return request.post<boolean>('/admin/coupons/batch-issue', data)
}

// 获取优惠券统计（管理端）
export function getCouponStatistics() {
  return request.get<{
    totalCoupons: number
    activeCoupons: number
    usedCoupons: number
    totalSaved: number
  }>('/admin/coupons/statistics')
}