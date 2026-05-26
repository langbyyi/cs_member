import request from '@/utils/request'
import type {
  ConsumptionRecord,
  PageParams,
  PageResponse
} from '@/types'

// 导出类型定义
export type {
  ConsumptionRecord
} from '@/types'

// 获取当前会员的消费记录
export function getMemberConsumptionRecords(params: PageParams & {
  startDate?: string
  endDate?: string
  paymentMethod?: number
}) {
  return request.get<PageResponse<ConsumptionRecord>>('/member/consumption/records', { params })
}

// 获取消费详情
export function getConsumptionDetail(recordId: number) {
  return request.get<ConsumptionRecord>(`/member/consumption/records/${recordId}`)
}

// 获取消费统计信息
export function getConsumptionStatistics(period?: string) {
  return request.get<{
    totalAmount: number
    totalCount: number
    avgAmount: number
    maxAmount: number
    minAmount: number
    thisMonthAmount: number
    thisMonthCount: number
    lastMonthAmount: number
    lastMonthCount: number
  }>('/member/consumption/statistics', {
    params: { period }
  })
}

// 获取消费趋势数据
export function getConsumptionTrend(days?: number) {
  return request.get<Array<{
    date: string
    amount: number
    count: number
  }>>('/member/consumption/trend', {
    params: { days }
  })
}

// 获取常用门店信息
export function getFavoriteStores() {
  return request.get<Array<{
    storeId: number
    storeName: string
    address: string
    visitCount: number
    totalAmount: number
  }>>('/member/consumption/favorite-stores')
}

// 获取消费偏好分析
export function getConsumptionPreferences() {
  return request.get<{
    favoritePaymentMethod: string
    favoriteTimeSlot: string
    favoriteCategory: string
    avgVisitInterval: string
    mostVisitedStore: string
  }>('/member/consumption/preferences')
}