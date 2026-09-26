import { http } from './request'
import type { Overview } from '@/types'

export const statsApi = {
  /** 仪表盘统计 */
  getOverview(): Promise<Overview> {
    return http<Overview>({ url: '/stats/overview', method: 'GET' })
  },
}
