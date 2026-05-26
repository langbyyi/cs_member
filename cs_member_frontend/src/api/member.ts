import request from '@/utils/request'
import type {
  Member,
  CreateMemberRequest,
  UpdateMemberRequest,
  PageParams,
  PageResponse
} from '@/types'

// 导出类型定义
export type {
  Member,
  CreateMemberRequest,
  UpdateMemberRequest
} from '@/types'

// 分页查询会员列表 - 对应后端 /api/v1/admin/members
export function getMemberList(params: PageParams & {
  name?: string
  phone?: string
  memberNo?: string
  memberLevel?: number
  status?: number
  storeName?: string
  startTime?: string
  endTime?: string
  minBalance?: number
  maxBalance?: number
  gender?: string
}) {
  return request.get<PageResponse<Member>>('/admin/members', { params })
}

// 创建会员 - 后端暂未实现，先保留接口
export function createMember(data: CreateMemberRequest) {
  return request.post<Member>('/admin/members', data)
}

// 更新会员信息 - 后端暂未实现，先保留接口
export function updateMember(id: number, data: UpdateMemberRequest) {
  return request.put<Member>(`/admin/members/${id}`, data)
}

// 获取会员详情 - 对应后端 GET /api/v1/admin/members/{id}
export function getMemberDetail(id: number) {
  return request.get<Member>(`/admin/members/${id}`)
}

// 获取所有会员的分页数据 - 对应后端 /api/v1/admin/members/all
export function getAllMembers(params: PageParams) {
  return request.get<PageResponse<Member>>('/admin/members/all', { params })
}

// 带排序的分页查询 - 对应后端 /api/v1/admin/members/sorted
export function getMembersWithSort(params: PageParams & {
  sortField?: string
  sortOrder?: 'ASC' | 'DESC'
}) {
  return request.get<PageResponse<Member>>('/admin/members/sorted', { params })
}

// 会员状态管理
export function updateMemberStatus(id: number, status: number) {
  return request.put(`/admin/members/${id}/status`, null, { params: { status } })
}

// 检查手机号是否存在
export function checkPhoneExists(phone: string, excludeId?: number) {
  return request.get<{ exists: boolean }>('/admin/members/check-phone', {
    params: { phone, excludeId }
  })
}

// 检查邮箱是否存在
export function checkEmailExists(email: string, excludeId?: number) {
  return request.get<{ exists: boolean }>('/admin/members/check-email', {
    params: { email, excludeId }
  })
}

// 根据手机号查询会员
export function getMemberByPhone(phone: string) {
  return request.get<Member>(`/admin/members/phone/${phone}`)
}

// 更新会员积分
export function updateMemberPoints(id: number, changePoints: number, changeType: number, remark?: string) {
  return request.put(`/admin/members/${id}/points`, {
    changePoints,
    changeType,
    remark
  })
}

// 更新会员余额
export function updateMemberBalance(id: number, changeAmount: number, remark?: string) {
  return request.put(`/admin/members/${id}/balance`, {
    changeAmount,
    remark
  })
}

// 生成会员编号
export function generateMemberNo() {
  return request.get<string>('/admin/members/generate-no')
}

// 搜索会员（支持多种搜索条件）
export function searchMembers(params: {
  keyword?: string
  memberNo?: string
  name?: string
  phone?: string
  memberLevel?: number
  status?: number
  limit?: number
}) {
  return request.get<Member[]>('/admin/members/search', { params })
}

// 会员注册
export function register(data: {
  name: string
  phone: string
  email: string
  password: string
  gender?: number
}) {
  return request.post<Member>('/admin/members/register', data)
}

// 获取会员消费记录
export function getMemberConsumptionList(params: PageParams & {
  memberId?: number
  startDate?: string
  endDate?: string
  minAmount?: number
  maxAmount?: number
}) {
  return request.get<PageResponse<any>>('/admin/consumption/records', { params })
}

// 获取会员积分记录
export function getMemberPointsList(params: PageParams & {
  memberId?: number
  changeType?: number
}) {
  return request.get<PageResponse<any>>('/admin/points/records', { params })
}

// 获取会员优惠券列表
export function getMemberCoupons(params: PageParams & {
  memberId?: number
  status?: number
}) {
  return request.get<PageResponse<any>>('/admin/coupons/page', { params })
}

// 积分兑换
export function exchangePoints(data: {
  memberId: number
  exchangeItemId: number
  points: number
}) {
  return request.post('/admin/points/adjust', data)
}

// ==========================================
// 新增积分管理相关API
// ==========================================

// 分页查询积分记录（管理员视角）
export function getPointsRecords(params: PageParams & {
  memberNo?: string
  memberName?: string
  changeType?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<PageResponse<any>>('/admin/points/records', { params })
}

// 获取积分统计数据
export function getPointsStatistics() {
  return request.get<any>('/admin/points/statistics')
}

// 调整会员积分
export function adjustPoints(data: {
  memberId: number
  points: number
  type: number // 1-增加 -1-减少
  reason: string
  remark?: string
  expireTime?: string
}) {
  return request.post<boolean>('/admin/points/adjust', data)
}

// ==================== 消费记录管理接口 ====================

// 分页查询消费记录（管理员视角）
export function getConsumptionRecords(params: PageParams & {
  memberNo?: string
  memberName?: string
  consumptionNo?: string
  paymentMethod?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<PageResponse<any>>('/admin/consumption/records', { params })
}

// 获取消费统计数据
export function getConsumptionStatistics() {
  return request.get<any>('/admin/consumption/statistics')
}

export function deleteMember(id: number) {
  return request.delete(`/admin/members/${id}`)
}

export function batchDeleteMembers(ids: number[]) {
  return request.delete('/admin/members/batch', { data: ids })
}