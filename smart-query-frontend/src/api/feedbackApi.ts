import api from './authApi'

export interface FeedbackRequest {
  sessionId?: number
  messageId?: number
  userId?: number
  feedbackType: 'like' | 'dislike'
  content?: string
  question?: string
  sql?: string
}

/**
 * 提交反馈
 */
export async function submitFeedback(request: FeedbackRequest): Promise<void> {
  await api.post('/feedback', request)
}

/**
 * 获取反馈统计
 */
export async function getFeedbackStats(): Promise<{ likeCount: number; dislikeCount: number; total: number }> {
  const response = await api.get('/feedback/stats')
  return response.data
}
