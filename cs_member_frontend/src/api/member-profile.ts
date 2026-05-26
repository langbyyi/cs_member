import request from '@/utils/request'
import type { Member } from '@/types'

// 导出类型定义
export type {
  Member
} from '@/types'

// 获取当前登录会员的个人信息
export function getCurrentMemberProfile() {
  return request.get<Member>('/member/profile/me')
}

// 更新当前登录会员的个人信息
export function updateCurrentMemberProfile(data: any) {
  return request.put<Member>('/member/profile/me', data)
}

// 修改密码
export function changePassword(data: {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}) {
  return request.put<boolean>('/member/profile/change-password', data)
}

// 获取会员积分余额
export function getMemberPointsBalance() {
  return request.get<{
    totalPoints: number
    availablePoints: number
    level: number
    levelName: string
    nextLevelPoints: number
    totalEarned: number
    totalUsed: number
  }>('/member/profile/points')
}

// 获取会员优惠券数量
export function getMemberCouponsCount() {
  return request.get<{
    total: number
    available: number
    used: number
    expired: number
  }>('/member/profile/coupons-count')
}