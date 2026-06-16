import api from './authApi'

export interface TagDefinition {
  id: number
  name: string
  color: string
  description?: string
  createdAt: string
}

export function getTags(): Promise<TagDefinition[]> {
  return api.get('/tags').then(res => res.data)
}

export function getTagById(id: number): Promise<TagDefinition> {
  return api.get(`/tags/${id}`).then(res => res.data)
}

export function createTag(tag: Partial<TagDefinition>): Promise<TagDefinition> {
  return api.post('/tags', tag).then(res => res.data)
}

export function updateTag(id: number, tag: Partial<TagDefinition>): Promise<void> {
  return api.put(`/tags/${id}`, tag)
}

export function deleteTag(id: number): Promise<void> {
  return api.delete(`/tags/${id}`)
}

export function getTagsByTableId(tableId: number): Promise<TagDefinition[]> {
  return api.get(`/tags/tables/${tableId}`).then(res => res.data)
}

export function setTagsForTable(tableId: number, tagIds: number[]): Promise<void> {
  return api.put(`/tags/tables/${tableId}`, tagIds)
}

export function getTablesByTag(tagId: number): Promise<any[]> {
  return api.get(`/tags/filter/${tagId}`).then(res => res.data)
}
