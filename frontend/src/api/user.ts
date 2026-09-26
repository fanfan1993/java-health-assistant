import { http } from './request'
import type { AdminOverview, UserInfo } from '@/types'

export const userApi = {
  /** 当前登录用户 */
  getMe(): Promise<UserInfo> {
    return http<UserInfo>({ url: '/user/me', method: 'GET' })
  },
  /** 管理后台：用户列表 */
  getAdminUsers(): Promise<UserInfo[]> {
    return http<UserInfo[]>({ url: '/admin/users', method: 'GET' })
  },
  /** 管理后台：平台统计 */
  getAdminOverview(): Promise<AdminOverview> {
    return http<AdminOverview>({ url: '/admin/overview', method: 'GET' })
  },
}
