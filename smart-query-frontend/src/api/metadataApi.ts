import api from './authApi'

// ===== 类型定义 =====

export interface ColumnMetadata {
  id: number
  tableId: number
  columnName: string
  displayName: string
  dataType: string
  role: string  // dimension / metric / none
  description: string
  enumValues: string
}

export interface TableMetadata {
  id: number
  datasourceId: number
  tableName: string
  displayName: string
  description: string
  syncTime: string
  columns: ColumnMetadata[]
  tags: any[]
}

export interface MetricDefinition {
  id: number
  tableId: number
  name: string
  description: string
  expression: string
  aggFunction: string
  unit: string
}

export interface DimensionDefinition {
  id: number
  tableId: number
  name: string
  description: string
  columnName: string
  enumValues: string
}

export interface SqlSample {
  id: number
  question: string
  sql: string
  tableName: string
  tags: string
  enabled: boolean
}

// ===== 表 & 字段 =====

export function getTables(): Promise<TableMetadata[]> {
  return api.get('/metadata/tables').then(res => res.data)
}

export function updateTable(id: number, data: Partial<TableMetadata>): Promise<void> {
  return api.put(`/metadata/tables/${id}`, data).then(() => {})
}

export function updateColumn(id: number, data: Partial<ColumnMetadata>): Promise<void> {
  return api.put(`/metadata/columns/${id}`, data).then(() => {})
}

export function syncMetadata(): Promise<{ success: boolean; syncedTables: number }> {
  return api.post('/metadata/sync').then(res => res.data)
}

// ===== 指标 =====

export function getMetrics(): Promise<MetricDefinition[]> {
  return api.get('/metadata/metrics').then(res => res.data)
}

export function addMetric(data: Omit<MetricDefinition, 'id'>): Promise<void> {
  return api.post('/metadata/metrics', data).then(() => {})
}

export function removeMetric(id: number): Promise<void> {
  return api.delete(`/metadata/metrics/${id}`).then(() => {})
}

// ===== 维度 =====

export function getDimensions(): Promise<DimensionDefinition[]> {
  return api.get('/metadata/dimensions').then(res => res.data)
}

export function addDimension(data: Omit<DimensionDefinition, 'id'>): Promise<void> {
  return api.post('/metadata/dimensions', data).then(() => {})
}

export function removeDimension(id: number): Promise<void> {
  return api.delete(`/metadata/dimensions/${id}`).then(() => {})
}

// ===== SQL 样本 =====

export function getSamples(): Promise<SqlSample[]> {
  return api.get('/samples').then(res => res.data)
}

export function addSample(data: Omit<SqlSample, 'id'>): Promise<void> {
  return api.post('/samples', data).then(() => {})
}

export function removeSample(id: number): Promise<void> {
  return api.delete(`/samples/${id}`).then(() => {})
}
