import {apiClient} from './config'
import {
    type CreateRoleRequest,
    type Role,
    type RoleInfo,
    type RolePagedResult,
    type UpdateRoleRequest
} from '@/types/role'
import {type QueryParams} from '@/types/api'

// Role Management API - Based on swagger.json /system/role endpoints

// GET /system/role - Get all roles
export const getRoles = async (): Promise<RoleInfo[]> => {
    console.log(`[DEBUG] getRoles called, URL: /api/v1/system/role`)

    const response = await apiClient.get(`/api/v1/system/role`)
    console.log(`[DEBUG] getRoles response:`, response.data.data)

    return response.data.data
}

// Role Management API

// Create a new role
export const createRole = async (request: CreateRoleRequest): Promise<Role> => {
    const response = await apiClient.post('/api/v1/system/role', request)
    return response.data.data
}

// Get role by ID
export const getRole = async (id: string): Promise<Role> => {
    const response = await apiClient.get(`/api/v1/system/role/${id}`)
    return response.data.data
}

// Update a role
export const updateRole = async (id: string, request: UpdateRoleRequest): Promise<Role> => {
    const response = await apiClient.put(`/api/v1/system/role/${id}`, request)
    return response.data.data
}

// Delete a role
export const deleteRole = async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/system/role/${id}`)
}

// Get paginated roles (for backwards compatibility)
export const getRolesPaged = async (params?: QueryParams): Promise<RolePagedResult> => {
    // Since the unified API doesn't have pagination for getRoles,
    // we'll need to implement client-side pagination or update the backend
    const roles = await getRoles()

    const page = params?.page || 1
    const pageSize = params?.page_size || 10
    const startIndex = (page - 1) * pageSize
    const endIndex = startIndex + pageSize

    const paginatedRoles = roles.slice(startIndex, endIndex)

    // Convert RoleInfo[] to Role[] format (assuming they have similar structure)
    const roleList: Role[] = paginatedRoles.map(role => ({
        id: role.id,
        name: role.name,
        sequence: 0, // Default value, may need adjustment
        status: 'active' as const,
        created_at: new Date(),
        updated_at: new Date(),
        menu_ids: []
    }))

    return {
        list: roleList,
        total: roles.length,
        page,
        page_size: pageSize,
        total_pages: Math.ceil(roles.length / pageSize)
    }
}

// Get role menus
export const getRoleMenus = async (roleId: string): Promise<string[]> => {
    const response = await apiClient.get(`/api/v1/system/role/${roleId}/menus`)
    return response.data.data
}