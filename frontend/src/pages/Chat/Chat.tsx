import { useEffect, useRef, useState } from 'react'
import { Empty, Input, Popconfirm, Tooltip } from 'antd'
import { DeleteOutlined, PlusOutlined, SendOutlined } from '@ant-design/icons'
import GradientButton from '@/components/GradientButton'
import { chatApi } from '@/api/chat'
import { useSSE } from '@/hooks/useSSE'
import { useAuthStore } from '@/store/auth'
import { useChatStore } from '@/store/chat'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import type { ChatMessage, ID } from '@/types'
import './Chat.scss'

/** 发送一条用户消息（乐观上屏 + SSE 流式回复） */
export default function Chat() {
  useDocumentTitle('AI 心灵伙伴 · AI 心理健康助手')
  const token = useAuthStore((s) => s.token)
  const user = useAuthStore((s) => s.user)
  const sessions = useChatStore((s) => s.sessions)
  const setSessions = useChatStore((s) => s.setSessions)
  const upsertSession = useChatStore((s) => s.upsertSession)
  const removeSession = useChatStore((s) => s.removeSession)

  const [currentId, setCurrentId] = useState<ID | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [streamText, setStreamText] = useState('')
  const [input, setInput] = useState('')
  const { start, stop, streaming } = useSSE()

  const listRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    chatApi
      .getSessions()
      .then(setSessions)
      .catch(() => {})
  }, [setSessions])

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages.length, streamText])

  const openSession = async (id: ID) => {
    stop()
    setStreamText('')
    setCurrentId(id)
    try {
      const list = await chatApi.getMessages(id)
      setMessages(list)
    } catch {
      setMessages([])
    }
  }

  const handleNewSession = async () => {
    stop()
    setStreamText('')
    const session = await chatApi.createSession('新的对话')
    upsertSession(session)
    setCurrentId(session.id)
    setMessages([])
  }

  const handleDeleteSession = async (id: ID) => {
    await chatApi.deleteSession(id)
    removeSession(id)
    if (currentId === id) {
      setCurrentId(null)
      setMessages([])
      setStreamText('')
    }
  }

  const handleSend = async () => {
    const text = input.trim()
    if (!text || streaming) return
    setInput('')
    stop()
    setStreamText('')

    let sessionId = currentId
    if (!sessionId) {
      const session = await chatApi.createSession(text.slice(0, 20))
      upsertSession(session)
      sessionId = session.id
      setCurrentId(session.id)
    }

    const userMsg: ChatMessage = { id: `tmp-${Date.now()}`, role: 'user', content: text }
    // 用户消息立即上屏（同步写入真实状态，流式回复期间始终可见）
    setMessages((prev) => [...prev, userMsg])

    let full = ''
    const finalize = () => {
      setMessages((prev) => [
        ...prev,
        ...(full ? [{ id: `ai-${Date.now()}`, role: 'assistant' as const, content: full }] : []),
      ])
      setStreamText('')
    }

    await start(
      `/api/chat/stream?sessionId=${sessionId}&message=${encodeURIComponent(text)}&token=${encodeURIComponent(token ?? '')}`,
      {
        onDelta: (delta) => {
          full += delta
          setStreamText(full)
        },
        onDone: finalize,
        onError: finalize,
      },
    )
  }

  return (
    <div className="chat-page">
      <aside className="chat-sessions">
        <GradientButton block icon={<PlusOutlined />} onClick={handleNewSession}>
          新建会话
        </GradientButton>
        <div className="session-list">
          {sessions.length === 0 && <div className="session-empty">暂无会话</div>}
          {sessions.map((session) => (
            <div
              key={session.id}
              className={`session-item ${session.id === currentId ? 'active' : ''}`}
              onClick={() => openSession(session.id)}
            >
              <span className="session-title">{session.title || '新的对话'}</span>
              <Popconfirm title="确定删除该会话吗？" onConfirm={(e) => { e?.stopPropagation(); handleDeleteSession(session.id) }} onCancel={(e) => e?.stopPropagation()}>
                <DeleteOutlined
                  className="session-delete"
                  onClick={(e) => e.stopPropagation()}
                />
              </Popconfirm>
            </div>
          ))}
        </div>
      </aside>

      <section className="chat-panel">
        <div className="message-list" ref={listRef}>
          {messages.length === 0 && !streamText && (
            <div className="chat-welcome">
              <div className="chat-welcome-avatar anim-float">🤖</div>
              <h3>你好呀，我是你的 AI 心灵伙伴</h3>
              <p>无论是开心还是烦恼，都可以跟我聊聊，我会一直在这里陪你。</p>
            </div>
          )}
          {messages.map((msg) => (
            <div key={msg.id} className={`message-row ${msg.role}`}>
              <div className={`avatar avatar-${msg.role}`}>
                {msg.role === 'assistant' ? '🤖' : (user?.nickname?.[0] ?? '我')}
              </div>
              <div className="bubble">{msg.content}</div>
            </div>
          ))}
          {streamText && (
            <div className="message-row assistant">
              <div className="avatar avatar-assistant">🤖</div>
              <div className="bubble typing">
                {streamText}
                <span className="cursor" />
              </div>
            </div>
          )}
          {streaming && !streamText && (
            <div className="message-row assistant">
              <div className="avatar avatar-assistant">🤖</div>
              <div className="bubble thinking">
                <span /><span /><span />
              </div>
            </div>
          )}
          {messages.length === 0 && !streamText && <Empty style={{ display: 'none' }} />}
        </div>

        <div className="chat-input-area">
          <Input.TextArea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="说说你今天的心情…（Enter 发送，Shift+Enter 换行）"
            autoSize={{ minRows: 1, maxRows: 4 }}
            onPressEnter={(e) => {
              if (!e.shiftKey) {
                e.preventDefault()
                handleSend()
              }
            }}
          />
          <Tooltip title={streaming ? '回复中…' : '发送'}>
            <GradientButton
              icon={<SendOutlined />}
              loading={streaming}
              onClick={handleSend}
              disabled={!input.trim()}
            />
          </Tooltip>
        </div>
      </section>
    </div>
  )
}
