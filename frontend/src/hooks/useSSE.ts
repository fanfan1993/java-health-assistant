import { useCallback, useRef, useState } from 'react'

export interface SSEHandlers {
  /** 收到增量文本 */
  onDelta?: (delta: string) => void
  /** 流正常结束 */
  onDone?: () => void
  /** 出错（AbortError 不触发） */
  onError?: (err: unknown) => void
}

/**
 * 基于 fetch + ReadableStream 的 SSE 流式读取 hook。
 * 兼容两种 data 事件：JSON `{"delta":"..."}` 与纯文本；`[DONE]` 表示结束。
 */
export function useSSE() {
  const controllerRef = useRef<AbortController | null>(null)
  const [streaming, setStreaming] = useState(false)

  const stop = useCallback(() => {
    controllerRef.current?.abort()
    controllerRef.current = null
    setStreaming(false)
  }, [])

  const start = useCallback(
    async (url: string, handlers: SSEHandlers = {}) => {
      stop()
      const controller = new AbortController()
      controllerRef.current = controller
      setStreaming(true)
      try {
        const res = await fetch(url, {
          signal: controller.signal,
          headers: { Accept: 'text/event-stream' },
        })
        if (!res.ok || !res.body) {
          throw new Error(`SSE 连接失败（${res.status}）`)
        }
        const reader = res.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''
        let finished = false
        while (!finished) {
          const { value, done } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() ?? ''
          for (const line of lines) {
            const trimmed = line.trim()
            if (!trimmed.startsWith('data:')) continue
            const payload = trimmed.slice(5).trim()
            if (!payload) continue
            if (payload === '[DONE]') {
              finished = true
              break
            }
            try {
              const json = JSON.parse(payload) as { delta?: string }
              if (typeof json.delta === 'string' && json.delta.length > 0) {
                handlers.onDelta?.(json.delta)
              }
            } catch {
              // 非 JSON：原文追加
              handlers.onDelta?.(payload)
            }
          }
        }
        handlers.onDone?.()
      } catch (err) {
        if ((err as Error)?.name !== 'AbortError') {
          handlers.onError?.(err)
        }
      } finally {
        if (controllerRef.current === controller) {
          controllerRef.current = null
          setStreaming(false)
        }
      }
    },
    [stop],
  )

  return { start, stop, streaming }
}
