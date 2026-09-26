import { create } from 'zustand'
import type { ChatSession, ID } from '@/types'

interface ChatState {
  sessions: ChatSession[]
  setSessions: (sessions: ChatSession[]) => void
  upsertSession: (session: ChatSession) => void
  removeSession: (id: ID) => void
}

export const useChatStore = create<ChatState>((set) => ({
  sessions: [],
  setSessions: (sessions) => set({ sessions }),
  upsertSession: (session) =>
    set((s) => ({
      sessions: [session, ...s.sessions.filter((item) => item.id !== session.id)],
    })),
  removeSession: (id) =>
    set((s) => ({ sessions: s.sessions.filter((item) => item.id !== id) })),
}))
