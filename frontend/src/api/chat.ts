import { http } from './request'
import type { ChatMessage, ChatSession, ID } from '@/types'

export const chatApi = {
  /** 会话列表 */
  getSessions(): Promise<ChatSession[]> {
    return http<ChatSession[]>({ url: '/chat/sessions', method: 'GET' })
  },
  /** 新建会话 */
  createSession(title?: string): Promise<ChatSession> {
    return http<ChatSession>({ url: '/chat/session', method: 'POST', data: { title } })
  },
  /** 删除会话 */
  deleteSession(id: ID): Promise<void> {
    return http<void>({ url: `/chat/session/${id}`, method: 'DELETE' })
  },
  /** 会话历史消息 */
  getMessages(sessionId: ID): Promise<ChatMessage[]> {
    return http<ChatMessage[]>({ url: `/chat/session/${sessionId}/messages`, method: 'GET' })
  },
}
