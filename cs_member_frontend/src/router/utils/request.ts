import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 网络连接检测函数
export const checkNetworkConnection = async () => {
  try {
    // 尝试连接后端服务
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)
    
    const healthUrl = 'http://localhost:8080/api/v1/auth/health'
    const response = await fetch(healthUrl, {
      method: 'GET',
      signal: controller.signal
    })
    
    clearTimeout(timeoutId)
    return { connected: true, status: response.status }
  } catch (error: any) {
    return {
      connected: false,
      error: error.message,
      code: error.code
    }
  }
}


// 根据HTTP状态码获取错误消息的辅助函数
function getErrorMessage(status: number): string {
  switch (status) {
    case 400:
      return '请求参数错误'
    case 401:
      return '登录已过期，请重新登录'
    case 403:
      return '没有权限访问该资源'
    case 404:
      return '请求的资源不存在'
    case 429:
      return '请求过于频繁，请稍后重试'
    case 500:
      return '服务器内部错误'
    default:
      return `请求失败 (${status})`
  }
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    const token = userStore.token

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    // 检查业务状态码，如果不是200（成功），则抛出错误
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code !== 200) {
        // 特殊处理权限错误
        if (data.code === 401) {
          const authError = new Error('登录已过期，请重新登录')
          ;(authError as any).code = data.code
          ;(authError as any).response = data
          ;(authError as any).isAuthError = true
          return Promise.reject(authError)
        }

        if (data.code === 403) {
          const permError = new Error('权限不足，无法访问该资源')
          ;(permError as any).code = data.code
          ;(permError as any).response = data
          ;(permError as any).isPermissionError = true
          return Promise.reject(permError)
        }

        // 创建业务错误，包含后端的错误信息
        const businessError = new Error(data.message || '业务处理失败')
        ;(businessError as any).code = data.code
        ;(businessError as any).response = data
        return Promise.reject(businessError)
      }
    }

    // 成功响应，返回完整的响应对象 {code, message, data, timestamp}
    // 这样前端可以访问 message 和其他字段
    return data
  },
  (error: any) => {
    let errorMessage = '网络错误，请稍后重试'

    if (error.response) {
      // 服务器返回了错误状态码，但响应体中可能包含后端的业务错误信息
      const { status, data } = error.response

      // 特殊处理HTTP状态码错误
      if (status === 401) {
        const authError = new Error('登录已过期，请重新登录')
        ;(authError as any).code = 401
        ;(authError as any).httpStatus = status
        ;(authError as any).isAuthError = true
        return Promise.reject(authError)
      }

      if (status === 403) {
        const permError = new Error('权限不足，无法访问该资源')
        ;(permError as any).code = 403
        ;(permError as any).httpStatus = status
        ;(permError as any).isPermissionError = true
        return Promise.reject(permError)
      }

      // 检查是否是后端返回的业务错误格式
      if (data && typeof data === 'object' && 'code' in data && 'message' in data) {
        // 直接使用后端的错误信息，创建Error对象
        const backendError = new Error(data.message)
        ;(backendError as any).code = data.code
        ;(backendError as any).httpStatus = status
        ;(backendError as any).response = data
        return Promise.reject(backendError)
      }

      // 其他HTTP错误，但也检查响应体中是否有错误消息
      const responseMessage = data?.message || getErrorMessage(status)
      const httpError = new Error(responseMessage)
      ;(httpError as any).code = data?.code || status
      ;(httpError as any).httpStatus = status
      ;(httpError as any).response = data

      return Promise.reject(httpError)

    } else if (error.request) {
      // 请求已发出，但没有收到响应 - 真正的网络错误
      if (error.code === 'ECONNREFUSED' || error.message?.includes('Connection refused')) {
        errorMessage = '后端服务未启动 (端口8080)，请启动后端服务'
      } else if (error.code === 'NETWORK_ERROR' || error.message?.includes('ERR_NETWORK')) {
        errorMessage = '网络连接失败，请检查网络连接'
      } else if (error.code === 'ETIMEDOUT' || error.message?.includes('timeout')) {
        errorMessage = '请求超时，请检查网络连接'
      } else {
        errorMessage = '无法连接到后端服务，请检查网络设置'
      }
    } else {
      errorMessage = error.message || '未知网络错误'
    }

    return Promise.reject(error)
  }
)

export default service
