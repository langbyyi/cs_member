import request from '@/utils/request'
import type {
  Role,
  CreateRoleRequest,
  PageParams,
  PageResponse
} from '@/types'

// 导出类型定义
export type {
  Role,
  CreateRoleRequest
} from '@/types'

// 分页查询角色
export function getRoleList(params?: PageParams & {
  roleName?: string
  roleCode?: string
  status?: number
}) {
  return request.get<PageResponse<Role>>('/admin/roles/page', { params })
}

// 查询所有角色
export function getAllRoles(params?: { status?: number }) {
  return request.get<Role[]>('/admin/roles', { params })
}

// 创建角色
export function createRole(data: CreateRoleRequest) {
  return request.post<Role>('/admin/roles', data)
}

// 更新角色信息
export function updateRole(id: number, data: Partial<CreateRoleRequest>) {
  return request.put<Role>(`/admin/roles/${id}`, data)
}

// 获取角色详情
export function getRoleDetail(id: number) {
  return request.get<Role>(`/admin/roles/${id}`)
}

// 更新角色状态
export function updateRoleStatus(id: number, status: number) {
  return request.put(`/admin/roles/${id}/status`, null, {
    params: { status }
  })
}

// 删除角色
export function deleteRole(id: number) {
  return request.delete(`/admin/roles/${id}`)
}

// 获取角色权限ID列表
export function getRolePermissions(roleId: number) {
  return request.get<number[]>(`/admin/roles/${roleId}/permissions`)
}

// 分配角色权限
export function assignRolePermissions(roleId: number, permissionIds: number[]) {
  return request.put(`/admin/roles/${roleId}/permissions`, permissionIds)
}

// 获取所有权限列表
export function getAllPermissions() {
  return request.get<any[]>('/admin/roles/permissions/all')
}