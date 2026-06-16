import api from './authApi'

export interface QueryResponse {
  success: boolean
  sql: string
  columns: string[]
  data: Record<string, any>[]
  errorMessage: string
  explanation: string
  executionPlan?: Record<string, any>[]
  sessionId?: number
}

export interface QueryRequest {
  question: string
  sessionId?: number
}

/**
 * 发送自然语言查询请求
 */
export async function queryData(request: QueryRequest): Promise<QueryResponse> {
  const response = await api.post<QueryResponse>('/query', request)
  return response.data
}

/**
 * 健康检查
 */
export async function healthCheck(): Promise<boolean> {
  try {
    const response = await api.get('/health')
    return response.data.status === 'ok'
  } catch {
    return false
  }
}
