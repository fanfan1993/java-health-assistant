import { http } from './request'
import type { ID, MoodJournal, PageResult } from '@/types'

export const journalApi = {
  /** 分页获取日记 */
  getJournals(page = 1, size = 10): Promise<PageResult<MoodJournal>> {
    return http<PageResult<MoodJournal>>({
      url: '/mood/journal',
      method: 'GET',
      params: { page, size },
    })
  },
  /** 新建日记 */
  addJournal(data: { content: string; mood: number; tags: string[] }): Promise<MoodJournal> {
    return http<MoodJournal>({ url: '/mood/journal', method: 'POST', data })
  },
  /** 删除日记 */
  removeJournal(id: ID): Promise<void> {
    return http<void>({ url: `/mood/journal/${id}`, method: 'DELETE' })
  },
}
