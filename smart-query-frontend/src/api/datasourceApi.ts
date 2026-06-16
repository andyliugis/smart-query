import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

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

export interface DatasourceConfig {
  id?: number
  name: string
  dbType: string
  jdbcUrl: string
  username?: string
  password?: string
  status?: string
}

export function getDatasources(): Promise<DatasourceConfig[]> {
  return api.get('/datasources').then(res => res.data)
}

export function getDatasource(id: number): Promise<DatasourceConfig> {
  return api.get(`/datasources/${id}`).then(res => res.data)
}

export function createDatasource(config: DatasourceConfig): Promise<DatasourceConfig> {
  return api.post('/datasources', config).then(res => res.data)
}

export function updateDatasource(id: number, config: DatasourceConfig): Promise<DatasourceConfig> {
  return api.put(`/datasources/${id}`, config).then(res => res.data)
}

export function deleteDatasource(id: number): Promise<void> {
  return api.delete(`/datasources/${id}`).then(res => res.data)
}

export function testConnection(config: DatasourceConfig): Promise<{ success: boolean; message: string; elapsedMs: number }> {
  return api.post('/datasources/test', config).then(res => res.data)
}

export function getTableNames(id: number): Promise<string[]> {
  return api.get(`/datasources/${id}/tables`).then(res => res.data)
}