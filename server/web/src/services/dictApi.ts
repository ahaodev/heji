import { apiClient } from './config'
import {
    type DictType,
    type DictItem,
    type CreateDictTypeRequest,
    type UpdateDictTypeRequest,
    type CreateDictItemRequest,
    type UpdateDictItemRequest,
    type DictTypeQueryParams,
    type DictItemQueryParams,
    type DictTypePagedResult,
    type DictItemPagedResult
} from '@/types/dict'

// Dictionary Type Management API

// Helpers to normalize date fields from API
const parseDictType = (t: any): DictType => ({
  ...t,
  created_at: new Date(t.created_at),
  updated_at: new Date(t.updated_at),
})

const parseDictItem = (i: any): DictItem => ({
  ...i,
  created_at: new Date(i.created_at),
  updated_at: new Date(i.updated_at),
})

// GET /system/dict/types - Get dictionary types with pagination
export const getDictTypes = async (params?: DictTypeQueryParams): Promise<DictTypePagedResult> => {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.page_size) searchParams.append('page_size', params.page_size.toString())
    if (params?.code) searchParams.append('code', params.code)
    if (params?.name) searchParams.append('name', params.name)
    if (params?.status) searchParams.append('status', params.status)
    if (params?.search) searchParams.append('search', params.search)
    if (params?.sort_by) searchParams.append('sort_by', params.sort_by)
    if (params?.order) searchParams.append('order', params.order)

  const response = await apiClient.get(`/api/v1/system/dict/types?${searchParams}`)
  const data = response.data.data as DictTypePagedResult
  return {
    ...data,
    list: (data.list || []).map(parseDictType),
  }
}

// GET /system/dict/types/{id} - Get dictionary type by ID
export const getDictType = async (id: string): Promise<DictType> => {
  const response = await apiClient.get(`/api/v1/system/dict/types/${id}`)
  return parseDictType(response.data.data)
}

// POST /system/dict/types - Create a new dictionary type
export const createDictType = async (data: CreateDictTypeRequest): Promise<DictType> => {
  const response = await apiClient.post('/api/v1/system/dict/types', data)
  return parseDictType(response.data.data)
}

// PUT /system/dict/types/{id} - Update dictionary type
export const updateDictType = async (id: string, data: UpdateDictTypeRequest): Promise<void> => {
    await apiClient.put(`/api/v1/system/dict/types/${id}`, data)
}

// DELETE /system/dict/types/{id} - Delete dictionary type
export const deleteDictType = async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/system/dict/types/${id}`)
}

// Dictionary Item Management API

// GET /system/dict/items - Get dictionary items with pagination
export const getDictItems = async (params?: DictItemQueryParams): Promise<DictItemPagedResult> => {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.page_size) searchParams.append('page_size', params.page_size.toString())
    if (params?.type_id) searchParams.append('type_id', params.type_id)
    if (params?.type_code) searchParams.append('type_code', params.type_code)
    if (params?.label) searchParams.append('label', params.label)
    if (params?.value) searchParams.append('value', params.value)
    if (params?.status) searchParams.append('status', params.status)
    if (params?.search) searchParams.append('search', params.search)
    if (params?.sort_by) searchParams.append('sort_by', params.sort_by)
    if (params?.order) searchParams.append('order', params.order)

  const response = await apiClient.get(`/api/v1/system/dict/items?${searchParams}`)
  const data = response.data.data as DictItemPagedResult
  return {
    ...data,
    list: (data.list || []).map(parseDictItem),
  }
}

// GET /system/dict/items/{id} - Get dictionary item by ID
export const getDictItem = async (id: string): Promise<DictItem> => {
  const response = await apiClient.get(`/api/v1/system/dict/items/${id}`)
  return parseDictItem(response.data.data)
}

// POST /system/dict/items - Create a new dictionary item
export const createDictItem = async (data: CreateDictItemRequest): Promise<DictItem> => {
  const response = await apiClient.post('/api/v1/system/dict/items', data)
  return parseDictItem(response.data.data)
}

// PUT /system/dict/items/{id} - Update dictionary item
export const updateDictItem = async (id: string, data: UpdateDictItemRequest): Promise<void> => {
    await apiClient.put(`/api/v1/system/dict/items/${id}`, data)
}

// DELETE /system/dict/items/{id} - Delete dictionary item
export const deleteDictItem = async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/system/dict/items/${id}`)
}

// Convenience API

// GET /system/dict/types/code/{code}/items - Get active dictionary items by type code
export const getDictItemsByTypeCode = async (typeCode: string): Promise<DictItem[]> => {
  const response = await apiClient.get(`/api/v1/system/dict/types/code/${typeCode}/items`)
  return (response.data.data as any[]).map(parseDictItem)
}

// Utility functions for common operations

// Get all active dictionary types
export const getActiveDictTypes = async (): Promise<DictType[]> => {
    const result = await getDictTypes({ status: 'active', page_size: 1000 })
    return result.list
}

// Get dictionary items for a specific type (by type ID)
export const getDictItemsByTypeId = async (typeId: string, params?: Omit<DictItemQueryParams, 'type_id'>): Promise<DictItemPagedResult> => {
    return getDictItems({ ...params, type_id: typeId })
}

// Toggle dictionary type status
export const toggleDictTypeStatus = async (id: string, currentStatus: 'active' | 'inactive'): Promise<void> => {
    const newStatus = currentStatus === 'active' ? 'inactive' : 'active'
    await updateDictType(id, { status: newStatus })
}

// Toggle dictionary item status
export const toggleDictItemStatus = async (id: string, currentStatus: 'active' | 'inactive'): Promise<void> => {
    const newStatus = currentStatus === 'active' ? 'inactive' : 'active'
    await updateDictItem(id, { status: newStatus })
}

// Set dictionary item as default
export const setDictItemAsDefault = async (id: string): Promise<void> => {
    await updateDictItem(id, { is_default: true })
}
