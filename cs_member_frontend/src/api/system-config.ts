import request from '@/utils/request'

// 系统配置项接口定义
export interface SystemConfig {
  id?: number
  configKey: string
  configValue: string
  configType?: 'system' | 'business' | 'security' | 'notification'
  configName?: string
  description?: string
  dataType?: 'string' | 'number' | 'boolean' | 'json'
  sortOrder?: number
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  deleted?: number
}

// 会员等级配置接口定义
export interface MemberGradeConfig {
  id?: number
  gradeCode: string
  gradeName: string
  minPoints: number
  maxPoints?: number
  pointsMultiplier: number
  discountRate: number
  color: string
  sortOrder: number
  status?: number
  createdTime?: string
  updatedTime?: string
}

// ========== 系统配置 API ==========

/**
 * 获取所有系统配置
 */
export function getAllConfigs() {
  return request.get<SystemConfig[]>('/admin/system-config/list')
}

/**
 * 保存单个配置
 */
export function saveConfig(configKey: string, configValue: string) {
  return request.post<boolean>('/admin/system-config/save', {
    configKey,
    configValue
  })
}

/**
 * 批量保存配置
 */
export function batchSaveConfigs(configs: Record<string, string>) {
  return request.post<boolean>('/admin/system-config/batch', configs)
}

// ========== 会员等级 API ==========

/**
 * 获取排序后的会员等级列表
 */
export function getSortedMemberGrades() {
  return request.get<MemberGradeConfig[]>('/admin/member-grade/sorted')
}

/**
 * 创建会员等级
 */
export function createMemberGrade(grade: Partial<MemberGradeConfig>) {
  return request.post<boolean>('/admin/member-grade/create', grade)
}

/**
 * 更新会员等级
 */
export function updateMemberGrade(grade: Partial<MemberGradeConfig>) {
  return request.put<boolean>('/admin/member-grade/update', grade)
}

/**
 * 删除会员等级
 */
export function deleteMemberGrade(id: number) {
  return request.delete<boolean>(`/admin/member-grade/${id}`)
}