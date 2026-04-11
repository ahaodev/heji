import {
  useMutation,
  useQueryClient,
  type MutationFunction,
} from '@tanstack/react-query'
import { toast } from 'sonner'
import { getErrorMessage } from '@/lib/error'

interface UseCrudMutationOptions<TData, TVariables> {
  mutationFn: MutationFunction<TData, TVariables>
  /** Query keys to invalidate on success. Can be static or computed from mutation result. */
  queryKeys: unknown[][] | ((data: TData, variables: TVariables) => unknown[][])
  /** Toast message on success. Can be static or computed from mutation result. */
  successMessage: string | ((data: TData, variables: TVariables) => string)
  /** Default error message shown when error has no extractable message. */
  errorMessage: string
}

/**
 * Generic CRUD mutation hook that handles query invalidation, toast notifications, and error handling.
 *
 * @example
 * ```ts
 * // Simple usage
 * export const useDeleteUser = () =>
 *   useCrudMutation({
 *     mutationFn: deleteUser,
 *     queryKeys: [['users']],
 *     successMessage: '用户删除成功',
 *     errorMessage: '删除用户失败',
 *   })
 *
 * // Dynamic messages
 * export const useDeleteUsers = () =>
 *   useCrudMutation({
 *     mutationFn: (ids: string[]) => Promise.all(ids.map(deleteUser)),
 *     queryKeys: [['users']],
 *     successMessage: (_, ids) => `已删除 ${ids.length} 个用户`,
 *     errorMessage: '批量删除用户失败',
 *   })
 * ```
 */
export function useCrudMutation<TData = unknown, TVariables = void>(
  options: UseCrudMutationOptions<TData, TVariables>
) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: options.mutationFn,
    onSuccess: (data, variables) => {
      const keys =
        typeof options.queryKeys === 'function'
          ? options.queryKeys(data, variables)
          : options.queryKeys
      for (const key of keys) {
        queryClient.invalidateQueries({ queryKey: key })
      }
      const msg =
        typeof options.successMessage === 'function'
          ? options.successMessage(data, variables)
          : options.successMessage
      toast.success(msg)
    },
    onError: (error: unknown) => {
      toast.error(getErrorMessage(error, options.errorMessage))
      throw error
    },
  })
}
