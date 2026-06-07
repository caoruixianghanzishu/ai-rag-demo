import request from './request'

export function chat(data) {
  return request.post('/api/chat', data)
}

/**
 * SSE 流式聊天
 */
export function streamChat(message, sessionId, onMessage, onDone, onError) {
  const params = new URLSearchParams({ message })
  if (sessionId) {
    params.append('sessionId', sessionId)
  }

  const eventSource = new EventSource(`/api/chat/stream?${params.toString()}`)
  let finished = false

  eventSource.onmessage = (event) => {
    if (event.data) {
      onMessage(event.data)
    }
  }

  eventSource.onerror = () => {
    if (!finished) {
      finished = true
      eventSource.close()
      onDone?.()
    }
  }

  return {
    close: () => {
      finished = true
      eventSource.close()
    }
  }
}
