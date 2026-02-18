import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query'
import {toast} from 'sonner'
import type {DictTypeQueryParams} from '@/types/dict'
import {deleteDictType, getDictTypes, updateDictType,} from '@/services/dictApi'

// Query keys for React Query
const DICT_TYPES_QUERY_KEY = 'dict-types'

// Custom hook for fetching dict types with pagination and filters
export function useDictTypes(params?: DictTypeQueryParams) {
    return useQuery({
        queryKey: [DICT_TYPES_QUERY_KEY, params],
        queryFn: () => getDictTypes(params),
        staleTime: 5 * 60 * 1000, // 5 minutes
    })
}

// Custom hook for batch deleting dict types
export function useDeleteDictTypes() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async (typeIds: string[]) => {
            const promises = typeIds.map(id => deleteDictType(id))
            return Promise.all(promises)
        },
        onSuccess: (_, typeIds) => {
            queryClient.invalidateQueries({queryKey: [DICT_TYPES_QUERY_KEY]})
            toast.success(`已删除 ${typeIds.length} 个字典类型`)
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '批量删除字典类型失败')
            throw error
        },
    })
}

// Custom hook for bulk status updates
export function useBulkUpdateDictTypes() {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async ({typeIds, status}: { typeIds: string[]; status: 'active' | 'inactive' }) => {
            const promises = typeIds.map(id =>
                updateDictType(id, {status})
            )
            return Promise.all(promises)
        },
        onSuccess: (_, {typeIds, status}) => {
            queryClient.invalidateQueries({queryKey: [DICT_TYPES_QUERY_KEY]})
            const statusText = status === 'active' ? '启用' : '禁用'
            toast.success(`已${statusText} ${typeIds.length} 个字典类型`)
        },
        onError: (error: any) => {
            toast.error(error?.response?.data?.msg || '批量更新字典类型状态失败')
            throw error
        },
    })
}