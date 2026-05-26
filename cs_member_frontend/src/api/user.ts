import request from '@/utils/request'

export interface SysUser {
  id: number
  phone: string
  realName: string
  email: string
  status: number
  roleId: number
  roleName: string
  storeId: number
  storeName: string
  createdTime: string
  updatedTime: string
}

export function getUserList(params: { pageNum: number; pageSize: number; phone?: string; realName?: string; status?: number }) {
  return request.get('/admin/users', { params })
}

export function getUserDetail(id: number) {
  return request.get(`/admin/users/${id}`)
}

export function createUser(data: any) {
  return request.post('/admin/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/admin/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}

export function resetUserPassword(id: number, newPassword: string) {
  return request.put(`/admin/users/${id}/reset-password`, null, { params: { newPassword } })
}
