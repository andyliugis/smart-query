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

// 响应拦截器 - 处理认证错误
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export interface AuthResponse {
  token: string
  type: string
  userId: number
  username: string
  nickname: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  nickname?: string
}

export function login(data: LoginRequest): Promise<AuthResponse> {
  return api.post('/auth/login', data).then(res => res.data)
}

export function register(data: RegisterRequest): Promise<AuthResponse> {
  return api.post('/auth/register', data).then(res => res.data)
}

export function saveAuthData(auth: AuthResponse) {
  localStorage.setItem('token', auth.token)
  localStorage.setItem('user', JSON.stringify({
    userId: auth.userId,
    username: auth.username,
    nickname: auth.nickname
  }))
}

export function clearAuthData() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export function getToken(): string | null {
  return localStorage.getItem('token')
}

export function getUserInfo(): { userId: number; username: string; nickname: string } | null {
  const user = localStorage.getItem('user')
  return user ? JSON.parse(user) : null
}

export function isAuthenticated(): boolean {
  return !!getToken()
}

export default api
