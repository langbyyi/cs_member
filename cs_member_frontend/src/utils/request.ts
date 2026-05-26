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

    const healthUrl = '/api/v1/member/health' // 使用相对路径，让Vite代理
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
  baseURL: '/api/v1', // API基础路径
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
      // 检查token是否已经包含Bearer前缀
      if (token.startsWith('Bearer ')) {
        config.headers.Authorization = token
      } else {
        config.headers.Authorization = `Bearer ${token}`
      }
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

    // 检查业务状态码，如果不是200（成功），则显示错误消息
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code !== 200) {
        // 显示后端返回的错误消息
        const errorMessage = data.message || '业务处理失败'
        ElMessage.error(errorMessage)
        const businessError = new Error(errorMessage)
          ; (businessError as any).code = data.code
          ; (businessError as any).response = data
        return Promise.reject(businessError)
      }
    }

    // 成功响应，返回完整的响应对象 {code, message, data, timestamp}
    // 这样前端可以访问 message 和其他字段
    return data
  },
  (error: any) => {
    // 只处理后端返回了错误状态码的情况，其他网络错误不弹窗
    if (error.response) {
      const { status, data } = error.response

      // 只有当后端返回了响应数据时才弹窗显示错误信息
      if (data && typeof data === 'object' && data.message) {
        ElMessage.error(data.message)
      }

      const httpError = new Error(data?.message || `请求失败 (${status})`)
        ; (httpError as any).code = data?.code || status
        ; (httpError as any).httpStatus = status
        ; (httpError as any).response = data

      return Promise.reject(httpError)
    }

    // 其他网络错误（无响应、超时等）不弹窗，直接返回错误
    return Promise.reject(error)
  }
)

export default service
