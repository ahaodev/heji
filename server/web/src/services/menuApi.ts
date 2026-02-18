import {apiClient} from './config'
import {
    type CreateMenuRequest,
    type Menu,
    type MenuPagedResult,
    type MenuQueryParams,
    type MenuTreeNode,
    type UpdateMenuRequest
} from '@/types/menu'

// GET /system/menu - Get /menu
export const getMenus = async (params?: MenuQueryParams): Promise<MenuPagedResult> => {
  const searchParams = new URLSearchParams()

  if (params?.type) searchParams.append('type', params.type)
  if (params?.status) searchParams.append('status', params.status)
  if (params?.parent_id) searchParams.append('parent_id', params.parent_id)
  if (params?.search) searchParams.append('search', params.search)
  if (params?.page) searchParams.append('page', params.page.toString())
  if (params?.page_size) searchParams.append('page_size', params.page_size.toString())

  const response = await apiClient.get(`/api/v1/system/menu?${searchParams.toString()}`)
  return response.data.data
}

// POST /system/menu - Create menu
export const createMenu = async (request: CreateMenuRequest): Promise<Menu> => {
  const response = await apiClient.post('/api/v1/system/menu', request)
  return response.data.data
}

// GET /system/menu/tree - Get menu tree
export const getMenuTree = async (status?: string): Promise<MenuTreeNode[]> => {
  const params = new URLSearchParams()
  if (status) params.append('status', status)
  
  const response = await apiClient.get(`/api/v1/system/menu/tree?${params.toString()}`)
  return response.data.data
}

// PUT /system/menu/{id} - Update menu
export const updateMenu = async (id: string, request: UpdateMenuRequest): Promise<Menu> => {
  const response = await apiClient.put(`/api/v1/system/menu/${id}`, request)
  return response.data.data
}

// DELETE /system/menu/{id} - Delete menu
export const deleteMenu = async (id: string): Promise<void> => {
  await apiClient.delete(`/api/v1/system/menu/${id}`)
}
