import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getRoles } from '@/services/roleApi'
import {
  createUser,
  deleteUser,
  getUser,
  getUserRoleList,
  getUsers,
  inviteUser,
  updateUser,
} from '@/services/userApi'
import type { QueryParams } from '@/types/api'
import type { UserUpdateRequest } from '@/types/user'
import { toast } from 'sonner'
import { getErrorMessage } from '@/lib/error'
import { useCrudMutation } from '@/hooks/use-crud-mutation'

// Query keys for React Query
const USERS_QUERY_KEY = 'users'
const USER_QUERY_KEY = 'user'

// Custom hook for fetching users with pagination and filters
export function useUsers(params?: QueryParams) {
  return useQuery({
    queryKey: [USERS_QUERY_KEY, params],
    queryFn: async () => {
      const usersResult = await getUsers(params)
      const [rolesData] = await Promise.all([getRoles()])

      // Create a map of role ID to role name for efficient lookups
      const roleMap = new Map(rolesData.map((role) => [role.id, role.name]))

      // Fetch roles for each user and map role IDs to names
      const usersWithRoles = await Promise.all(
        usersResult.list.map(async (user) => {
          try {
            const roleIds = await getUserRoleList(user.id)
            const roleNames = roleIds.map(
              (roleId) => roleMap.get(roleId) || roleId
            )
            return { ...user, roles: roleNames }
          } catch (error) {
            console.warn(`Failed to fetch roles for user ${user.id}:`, error)
            return { ...user, roles: [] }
          }
        })
      )

      return {
        ...usersResult,
        list: usersWithRoles,
      }
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

// Custom hook for fetching single user
export function useUser(id: string, enabled = true) {
  return useQuery({
    queryKey: [USER_QUERY_KEY, id],
    queryFn: () => getUser(id),
    enabled: !!id && enabled,
  })
}

// Custom hook for creating user
export function useCreateUser() {
  return useCrudMutation({
    mutationFn: createUser,
    queryKeys: [[USERS_QUERY_KEY]],
    successMessage: '用户创建成功',
    errorMessage: '创建用户失败',
  })
}

// Custom hook for updating user
export function useUpdateUser() {
  return useCrudMutation({
    mutationFn: ({ id, data }: { id: string; data: UserUpdateRequest }) =>
      updateUser(id, data),
    queryKeys: (_, { id }) => [[USERS_QUERY_KEY], [USER_QUERY_KEY, id]],
    successMessage: '用户更新成功',
    errorMessage: '更新用户失败',
  })
}

// Custom hook for deleting user
export function useDeleteUser() {
  return useCrudMutation({
    mutationFn: deleteUser,
    queryKeys: [[USERS_QUERY_KEY]],
    successMessage: '用户删除成功',
    errorMessage: '删除用户失败',
  })
}

// Custom hook for batch deleting users
export function useDeleteUsers() {
  return useCrudMutation({
    mutationFn: (userIds: string[]) =>
      Promise.all(userIds.map((id) => deleteUser(id))),
    queryKeys: [[USERS_QUERY_KEY]],
    successMessage: (_, userIds) => `已删除 ${userIds.length} 个用户`,
    errorMessage: '批量删除用户失败',
  })
}

// Custom hook for inviting user
export function useInviteUser() {
  return useCrudMutation({
    mutationFn: inviteUser,
    queryKeys: [[USERS_QUERY_KEY]],
    successMessage: '邀请发送成功',
    errorMessage: '邀请用户失败',
  })
}

// Custom hook for refreshing users data
export function useRefreshUsers() {
  const queryClient = useQueryClient()

  return () => {
    queryClient.invalidateQueries({ queryKey: [USERS_QUERY_KEY] })
  }
}

// Custom hook for bulk status updates
export function useBulkUpdateUsers() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async ({
      userIds,
      status,
    }: {
      userIds: string[]
      status: 'active' | 'inactive' | 'suspended'
    }) => {
      const promises = userIds.map((id) => updateUser(id, { status }))
      return Promise.all(promises)
    },
    onSuccess: (_, { userIds, status }) => {
      queryClient.invalidateQueries({ queryKey: [USERS_QUERY_KEY] })
      const statusText =
        status === 'active' ? '激活' : status === 'inactive' ? '停用' : '暂停'
      toast.success(`已${statusText} ${userIds.length} 个用户`)
    },
    onError: (error: unknown) => {
      toast.error(getErrorMessage(error, '批量更新用户状态失败'))
      throw error
    },
  })
}
