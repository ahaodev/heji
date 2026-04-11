import { useQuery } from '@tanstack/react-query'
import {
  deleteDictType,
  getDictTypes,
  updateDictType,
} from '@/services/dictApi'
import type { DictTypeQueryParams } from '@/types/dict'
import { useCrudMutation } from '@/hooks/use-crud-mutation'

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
  return useCrudMutation({
    mutationFn: (typeIds: string[]) =>
      Promise.all(typeIds.map((id) => deleteDictType(id))),
    queryKeys: [[DICT_TYPES_QUERY_KEY]],
    successMessage: (_, typeIds) => `已删除 ${typeIds.length} 个字典类型`,
    errorMessage: '批量删除字典类型失败',
  })
}

// Custom hook for bulk status updates
export function useBulkUpdateDictTypes() {
  return useCrudMutation({
    mutationFn: ({
      typeIds,
      status,
    }: {
      typeIds: string[]
      status: 'active' | 'inactive'
    }) => Promise.all(typeIds.map((id) => updateDictType(id, { status }))),
    queryKeys: [[DICT_TYPES_QUERY_KEY]],
    successMessage: (_, { typeIds, status }) => {
      const statusText = status === 'active' ? '启用' : '禁用'
      return `已${statusText} ${typeIds.length} 个字典类型`
    },
    errorMessage: '批量更新字典类型状态失败',
  })
}
