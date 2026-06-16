import api from './authApi'

export interface ChatSession {
  id: number
  userId: number
  title: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant'
  content: string
  generatedSql?: string
  resultData?: string
  createdAt: string
}

// 获取会话列表
export function getSessions(): Promise<ChatSession[]> {
  return api.get('/chat/sessions').then(res => res.data)
}

// 创建新会话
export function createSession(title?: string): Promise<ChatSession> {
  return api.post('/chat/sessions', { title }).then(res => res.data)
}

// 获取会话消息
export function getSessionMessages(sessionId: number): Promise<ChatMessage[]> {
  return api.get(`/chat/sessions/${sessionId}/messages`).then(res => res.data)
}
