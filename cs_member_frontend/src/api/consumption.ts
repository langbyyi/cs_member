import request from '@/utils/request'
import type {
  ConsumptionRecord,
  CreateConsumptionRequest,
  UpdateConsumptionRequest,
  PageParams,
  PageResponse
} from '@/types'

export type {
  ConsumptionRecord,
  CreateConsumptionRequest,
  UpdateConsumptionRequest,
  Member,
  Store
} from '@/types'

// 分页查询消费记录
export function getConsumptionList(params: PageParams & {
  consumptionNo?: string
  memberNo?: string
  memberName?: string
  memberPhone?: string
  storeId?: number
  status?: number
  paymentMethod?: number
  startTime?: string
  endTime?: string
}) {
  return request.get<PageResponse<ConsumptionRecord>>('/admin/consumption/records', { params })
}

// 创建消费记录
export function createConsumption(data: CreateConsumptionRequest) {
  return request.post<ConsumptionRecord>('/admin/consumption', data)
}

// 获取消费记录详情
export function getConsumptionDetail(id: number) {
  return request.get<ConsumptionRecord>(`/admin/consumption/${id}`)
}

// 根据消费单号查询
export function getConsumptionByNo(consumptionNo: string) {
  return request.get<ConsumptionRecord>(`/admin/consumption/no/${consumptionNo}`)
}

// 根据会员ID查询消费记录
export function getConsumptionByMemberId(memberId: number, params?: PageParams & {
  status?: number
}) {
  return request.get<PageResponse<ConsumptionRecord>>(`/admin/consumption/member/${memberId}`, { params })
}

// 更新消费状态
export function updateConsumptionStatus(id: number, status: number) {
  return request.put(`/admin/consumption/${id}/status`, null, {
    params: { status }
  })
}

// 取消消费记录
export function cancelConsumption(id: number, remark?: string) {
  return request.put(`/admin/consumption/${id}/cancel`, null, {
    params: { remark }
  })
}

// 退款消费记录
export function refundConsumption(id: number, remark?: string) {
  return request.put(`/admin/consumption/${id}/refund`, null, {
    params: { remark }
  })
}

// 生成消费单号
export function generateConsumptionNo() {
  return request.get<string>('/admin/consumption/generate-no')
}

// 计算消费金额
export function calculateAmount(params: {
  memberId: number
  originalAmount: number
  couponId?: number
  pointsUsed?: number
}) {
  return request.post<{
    originalAmount: number
    discountAmount: number
    actualAmount: number
  }>('/admin/consumption/calculate-amount', params)
}

// 计算获得积分
export function calculatePoints(actualAmount: number) {
  return request.post<{ points: number }>('/admin/consumption/calculate-points', {
    actualAmount
  })
}

// 今日消费记录
export function getTodayConsumptions(storeId?: number) {
  return request.get<ConsumptionRecord[]>('/admin/consumption/today', {
    params: { storeId }
  })
}

export { getStoreList } from './store'
export { getMemberByPhone } from './member'
