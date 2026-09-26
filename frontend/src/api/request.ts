import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { message } from 'antd'
import type { Result } from '@/types'
import { useAuthStore } from '@/store/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000,
})

request.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function goLogin() {
  useAuthStore.getState().logout()
  window.location.href = '/login'
}

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data as Result<unknown>
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data as unknown as AxiosResponse
      }
      if (res.code === 401) {
        goLogin()
      } else {
        message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      goLogin()
    } else {
      const msg: string = error.response?.data?.message || error.message || '网络错误'
      message.error(msg)
    }
    return Promise.reject(error)
  },
)

/** 泛型请求：拦截器已解包 Result，直接返回 data */
export async function http<T>(config: AxiosRequestConfig): Promise<T> {
  const data = await request.request<unknown>(config)
  return data as T
}

export default request
