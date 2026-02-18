import { type UserPagedResult, type QueryParams } from '@/types/api'
import { type User, type CreateUserRequest, type InviteUserRequest, type UserUpdateRequest } from '@/types/user'
import { apiClient } from '@/services/config'

// User Management API - Based on swagger.json /system/user endpoints

// GET /system/user - Get all users
export async function getUsers(params?: QueryParams): Promise<UserPagedResult> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.append('page', params.page.toString())
  if (params?.page_size) searchParams.append('page_size', params.page_size.toString())
  if (params?.search) searchParams.append('search', params.search)
  if (params?.status) searchParams.append('status', params.status)

  const response = await apiClient.get(`/api/v1/system/user?${searchParams}`)
  return response.data.data
}

// POST /system/user - Create a new user
export async function createUser(data: CreateUserRequest): Promise<User> {
  const response = await apiClient.post('/api/v1/system/user', data)
  return response.data.data
}

// POST /system/user/invite - Invite user
export async function inviteUser(data: InviteUserRequest): Promise<User> {
  const response = await apiClient.post('/api/v1/system/user/invite', data)
  return response.data.data
}

// GET /system/user/{id} - Get user by ID
export async function getUser(id: string): Promise<User> {
  const response = await apiClient.get(`/api/v1/system/user/${id}`)
  return response.data.data
}

// PUT /system/user/{id} - Update user
export async function updateUser(id: string, data: UserUpdateRequest): Promise<User> {
  const response = await apiClient.put(`/api/v1/system/user/${id}`, data)
  return response.data.data
}

// DELETE /system/user/{id} - Delete user
export async function deleteUser(id: string): Promise<string> {
  const response = await apiClient.delete(`/api/v1/system/user/${id}`)
  return response.data.data
}

// GET /system/user/{userId}/roles - Get user roles
export async function getUserRoleList(userId: string): Promise<string[]> {
  const response = await apiClient.get(`/api/v1/system/user/${userId}/roles`)
  return response.data.data
}

