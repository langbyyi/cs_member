import request from '@/utils/request'
import type {
  User,
  CreateUserRequest,
  UpdateUserRequest,
  PageParams,
  PageResponse
} from '@/types'

// 导出类型定义
export type {
  User,
  CreateUserRequest,
  UpdateUserRequest
} from '@/types'

// 员工DTO接口
export interface EmployeeDTO {
  id: number
  phone: string
  name: string
  email?: string
  avatar?: string
  gender?: number
  birthday?: string
  storeId?: number
  storeName?: string
  employeeNo?: string
  status: number
  roleCode: string
  roleName: string
  loginCount?: number
  lastLoginTime?: string
  lastLoginIp?: string
  createdTime: string
  updatedTime: string
}

// 创建员工请求
export interface CreateEmployeeRequest {
  phone: string
  name: string
  email?: string
  gender?: number
  storeId?: number
  roleCode: 'STORE_ADMIN' | 'CLERK'
  password?: string
}

// 更新员工请求
export interface UpdateEmployeeRequest {
  name?: string
  email?: string
  avatar?: string
  gender?: number
  storeId?: number
}

// 员工角色枚举
export enum EmployeeRole {
  CLERK = 'CLERK',        // 店员
  STORE_ADMIN = 'STORE_ADMIN'   // 店长
}

// 员工状态枚举
export enum EmployeeStatus {
  DISABLED = 0,     // 禁用
  ACTIVE = 1,       // 启用
  LOCKED = 2        // 锁定
}

// 分页查询员工
export function getEmployeeList(params: PageParams & {
  name?: string
  phone?: string
  storeId?: number
  status?: number
}) {
  return request.get<PageResponse<EmployeeDTO>>('/admin/employees', {
    params
  })
}

// 创建员工
export function createEmployee(data: CreateEmployeeRequest) {
  return request.post<number>('/admin/employees', data)
}

// 更新员工信息
export function updateEmployee(id: number, data: UpdateEmployeeRequest) {
  return request.put<string>('/admin/employees/' + id, data)
}

// 获取员工详情
export function getEmployeeDetail(id: number) {
  return request.get<EmployeeDTO>('/admin/employees/' + id)
}

// 重置员工密码
export function resetEmployeePassword(id: number, newPassword: string) {
  return request.put('/admin/employees/' + id + '/reset-password', null, {
    params: { newPassword }
  })
}

// 更新员工状态
export function updateEmployeeStatus(id: number, status: number) {
  return request.put('/admin/employees/' + id + '/status', null, {
    params: { status }
  })
}

// 删除员工
export function deleteEmployee(id: number) {
  return request.delete('/admin/employees/' + id)
}

// 批量更新员工状态 (前端功能保留，通过循环调用单个更新实现)
export function batchUpdateEmployeeStatus(employeeIds: number[], status: number) {
  return Promise.all(
    employeeIds.map(id => updateEmployeeStatus(id, status))
  )
}

// 批量重置员工密码 (前端功能保留，通过循环调用单个重置实现)
export function batchResetEmployeePassword(employeeIds: number[], newPassword: string) {
  return Promise.all(
    employeeIds.map(id => resetEmployeePassword(id, newPassword))
  )
}

// 获取员工统计数据
export function getEmployeeStatistics(params?: {
  startDate?: string
  endDate?: string
  storeId?: number
}) {
  return request.get<Record<string, any>>('/admin/statistics/employees', {
    params
  })
}