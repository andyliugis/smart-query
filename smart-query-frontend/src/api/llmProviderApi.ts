import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器 - 添加 token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

export interface LlmProvider {
  id: number
  name: string
  displayName: string
  modelName: string
  isActive: boolean
  available: boolean
}

export interface LlmProviderConfig {
  id?: number
  name: string
  displayName: string
  apiKey?: string
  baseUrl?: string
  modelName: string
  isActive: boolean
  configJson?: string
}

export function getLlmProviders(): Promise<LlmProvider[]> {
  return api.get('/llm-providers').then(res => res.data)
}

export function getActiveProvider(): Promise<{ name: string; available: string }> {
  return api.get('/llm-providers/active').then(res => res.data)
}

export function updateProvider(name: string, config: LlmProviderConfig): Promise<LlmProviderConfig> {
  return api.put(`/llm-providers/${name}`, config).then(res => res.data)
}

export function activateProvider(name: string): Promise<{ message: string }> {
  return api.post(`/llm-providers/${name}/activate`).then(res => res.data)
}