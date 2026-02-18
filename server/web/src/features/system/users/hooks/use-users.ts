import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query'
import {toast} from 'sonner'
import type {QueryParams} from '@/types/api'
import type {UserUpdateRequest} from '@/types/user'
import {createUser, deleteUser, getUser, getUserRoleList, getUsers, inviteUser, updateUser,} from '@/services/userApi'
import {getRoles} from '@/services/roleApi'

// Query keys for React Query
const USERS_QUERY_KEY = 'users'
const USER_QUERY_KEY = 'user'

// Custom hook for fetching users with pagination and filters
export function useUsers(params?: QueryParams) {
    return useQuery({
        queryKey: [USERS_QUERY_KEY, params],
        queryFn: async () => {
            const usersResult = await getUsers(params)
            const [rolesData] = await Promise.all([
                getRoles()
            ])

            // Create a map of role ID to role name for efficient lookups
            const roleMap = new Map(rolesData.map(role => [role.id, role.name]))

            // Fetch roles for each user and map role IDs to names
            const usersWithRoles = await Promise.all(
                usersResult.list.map(async (user) => {
                    try {
                        const roleIds = await getUserRoleList(user.id)
                        const roleNames = roleIds.map(roleId => roleMap.get(roleId) || roleId)
                        return {...user, roles: roleNames}
                    } catch (error) {
                        console.warn(`Failed to fetch roles for user ${user.id}:`, error)
                        return {...user, roles: []}
                    }
                })
            )

            return {
                ...usersResult,
                list: usersWithRoles
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
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: createUser,
        onSuccess: (data) => {
            queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
            toast.success('用户创建成功')
            return data
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '创建用户失败')
            throw error
        },
    })
}

// Custom hook for updating user
export function useUpdateUser() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: ({id, data}: { id: string; data: UserUpdateRequest }) =>
            updateUser(id, data),
        onSuccess: (data, {id}) => {
            queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
            queryClient.invalidateQueries({queryKey: [USER_QUERY_KEY, id]})
            toast.success('用户更新成功')
            return data
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '更新用户失败')
            throw error
        },
    })
}

// Custom hook for deleting user
export function useDeleteUser() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: deleteUser,
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
            toast.success('用户删除成功')
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '删除用户失败')
            throw error
        },
    })
}

// Custom hook for batch deleting users
export function useDeleteUsers() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async (userIds: string[]) => {
            const promises = userIds.map(id => deleteUser(id))
            return Promise.all(promises)
        },
        onSuccess: (_, userIds) => {
            queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
            toast.success(`已删除 ${userIds.length} 个用户`)
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '批量删除用户失败')
            throw error
        },
    })
}

// Custom hook for inviting user
export function useInviteUser() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: inviteUser,
        onSuccess: (data) => {
            queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
            toast.success('邀请发送成功')
            return data
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '邀请用户失败')
            throw error
        },
    })
}

// Custom hook for refreshing users data
export function useRefreshUsers() {
    const queryClient = useQueryClient()

    return () => {
        queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
    }
}

// Custom hook for bulk status updates
export function useBulkUpdateUsers() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async ({userIds, status}: { userIds: string[]; status: 'active' | 'inactive' | 'suspended' }) => {
            const promises = userIds.map(id =>
                updateUser(id, {status})
            )
            return Promise.all(promises)
        },
        onSuccess: (_, {userIds, status}) => {
            queryClient.invalidateQueries({queryKey: [USERS_QUERY_KEY]})
            const statusText = status === 'active' ? '激活' : status === 'inactive' ? '停用' : '暂停'
            toast.success(`已${statusText} ${userIds.length} 个用户`)
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '批量更新用户状态失败')
            throw error
        },
    })
}