import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

// 用户基本信息类型
export interface UserInfo {
  id: number
  phone: string
  name: string
  email?: string
  avatar?: string
  gender?: number
  birthday?: string
  storeId?: number
  employeeNo?: string
  status: number
}

// 门店接口类型定义
export interface Store {
  id?: number
  storeNo?: string
  storeName: string
  storeType: 'direct' | 'franchise'
  province?: string
  city?: string
  district?: string
  address?: string
  contactPhone?: string
  contactEmail?: string
  businessHours?: string
  managerId?: number | null
  managerName?: string
  area?: number | null
  memberCount?: number
  status: 0 | 1  // 0-停业 1-营业
  openTime?: string
  createdTime?: string
  updatedTime?: string
}

export interface StoreListParams {
  pageNum: number
  pageSize: number
  storeName?: string
  storeType?: string
  status?: number | null
  province?: string
  city?: string
}

export interface StoreListResponse {
  records: Store[]
  total: number
  size: number
  current: number
  pages: number
}

// 门店API接口
export const storeApi = {
  /**
   * 获取门店列表（分页）
   */
  getStoreList(params: StoreListParams): Promise<AxiosResponse> {
    return request({
      url: '/admin/stores',
      method: 'get',
      params
    })
  },

  /**
   * 获取所有营业中的门店
   */
  getOpenStores(): Promise<AxiosResponse> {
    return request({
      url: '/admin/stores/open',
      method: 'get'
    })
  },

  /**
   * 根据ID获取门店详情
   */
  getStoreById(id: number): Promise<AxiosResponse> {
    return request({
      url: `/admin/stores/${id}`,
      method: 'get'
    })
  },

  /**
   * 创建门店
   */
  createStore(data: Omit<Store, 'id' | 'createdTime' | 'updatedTime'>): Promise<AxiosResponse> {
    return request({
      url: '/admin/stores',
      method: 'post',
      data
    })
  },

  /**
   * 更新门店信息
   */
  updateStore(id: number, data: Partial<Store>): Promise<AxiosResponse> {
    return request({
      url: `/admin/stores/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 更新门店状态
   */
  updateStoreStatus(id: number, status: number): Promise<AxiosResponse> {
    return request({
      url: `/admin/stores/${id}/status`,
      method: 'put',
      params: { status }
    })
  },

  /**
   * 删除门店
   */
  deleteStore(id: number): Promise<AxiosResponse> {
    return request({
      url: `/admin/stores/${id}`,
      method: 'delete'
    })
  },

  /**
   * 根据状态查询门店
   */
  getStoresByStatus(status: number): Promise<AxiosResponse> {
    return request({
      url: '/admin/stores',
      method: 'get',
      params: { status }
    })
  },

  /**
   * 根据日期范围统计门店数量
   */
  countStoresByDateRange(startTime: string, endTime: string): Promise<AxiosResponse> {
    return request({
      url: '/admin/stores/statistics/count',
      method: 'get',
      params: { startTime, endTime }
    })
  },

  /**
   * 获取门店的员工列表
   */
  getStoreEmployees(storeId: number): Promise<AxiosResponse> {
    return request({
      url: `/admin/stores/${storeId}/employees`,
      method: 'get'
    })
  }
}

// Standalone exports for backward compatibility
export const getStoreList = storeApi.getStoreList
export const getOpenStores = storeApi.getOpenStores

export default storeApi