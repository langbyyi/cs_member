import request from '@/utils/request'
import type { MemberGradeConfig } from '@/types'

/**
 * 会员等级API
 */
export class MemberGradeAPI {
  /**
   * 获取等级配置列表
   */
  static getGradeList() {
    return request.get<MemberGradeConfig[]>('/admin/member-grade/public/list')
  }

  /**
   * 计算会员等级
   * @param points 积分
   */
  static calculateGrade(points: number) {
    return request.get<MemberGradeConfig>('/member/grade/calculate', {
      params: { points }
    })
  }
}

/**
 * 获取等级配置列表
 */
export function getGradeList() {
  return MemberGradeAPI.getGradeList()
}

/**
 * 计算会员等级
 * @param points 积分
 */
export function calculateGrade(points: number) {
  return MemberGradeAPI.calculateGrade(points)
}