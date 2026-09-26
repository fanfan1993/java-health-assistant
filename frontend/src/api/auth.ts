import { http } from './request'
import type { LoginResult } from '@/types'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams extends LoginParams {
  nickname: string
}

export const authApi = {
  /** 登录 */
  login(data: LoginParams): Promise<LoginResult> {
    return http<LoginResult>({ url: '/auth/login', method: 'POST', data })
  },
  /** 注册 */
  register(data: RegisterParams): Promise<LoginResult> {
    return http<LoginResult>({ url: '/auth/register', method: 'POST', data })
  },
}
