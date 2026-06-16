import api from './authApi'

export interface StreamEvent {
  type: 'status' | 'sql' | 'result' | 'explanation' | 'execution_plan' | 'complete' | 'error' | 'session'
  data: Record<string, any>
}

export interface StreamCallbacks {
  onStatus?: (message: string, step: string) => void
  onSql?: (sql: string) => void
  onResult?: (columns: string[], data: Record<string, any>[], rowCount: number) => void
  onExplanation?: (explanation: string) => void
  onExecutionPlan?: (plan: Record<string, any>[]) => void
  onComplete?: (response: any) => void
  onError?: (message: string) => void
  onSession?: (sessionId: number) => void
}

/**
 * 发送流式查询请求
 */
export async function streamQuery(
  question: string,
  sessionId: number | null,
  callbacks: StreamCallbacks
): Promise<void> {
  const token = localStorage.getItem('token')
  
  const response = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    },
    body: JSON.stringify({ question, sessionId })
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('No reader available')
  }

  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    
    if (done) {
      break
    }

    buffer += decoder.decode(value, { stream: true })
    
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    let eventType = ''
    let eventData = ''

    for (const line of lines) {
      if (line.startsWith('event:')) {
        eventType = line.substring(6).trim()
      } else if (line.startsWith('data:')) {
        eventData = line.substring(5).trim()
        
        if (eventType && eventData) {
          try {
            const data = JSON.parse(eventData)
            handleEvent(eventType, data, callbacks)
          } catch (e) {
            console.error('Failed to parse SSE data:', e)
          }
          eventType = ''
          eventData = ''
        }
      } else if (line === '') {
        eventType = ''
        eventData = ''
      }
    }
  }
}

function handleEvent(type: string, data: Record<string, any>, callbacks: StreamCallbacks) {
  switch (type) {
    case 'status':
      callbacks.onStatus?.(data.message, data.step)
      break
    case 'sql':
      callbacks.onSql?.(data.sql)
      break
    case 'result':
      callbacks.onResult?.(data.columns, data.data, data.rowCount)
      break
    case 'explanation':
      callbacks.onExplanation?.(data.explanation)
      break
    case 'execution_plan':
      callbacks.onExecutionPlan?.(data.plan)
      break
    case 'complete':
      callbacks.onComplete?.(data)
      break
    case 'error':
      callbacks.onError?.(data.message)
      break
    case 'session':
      callbacks.onSession?.(data.sessionId)
      break
  }
}
