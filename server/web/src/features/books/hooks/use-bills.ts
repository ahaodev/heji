import { useQuery } from '@tanstack/react-query'
import type { QueryParams } from '@/types/api'
import { getBills } from '@/services/billApi'

const BILLS_QUERY_KEY = 'bills'

export function useBills(bookId: string, params?: QueryParams) {
  return useQuery({
    queryKey: [BILLS_QUERY_KEY, bookId, params],
    queryFn: () => getBills(bookId, params),
    enabled: !!bookId,
    staleTime: 5 * 60 * 1000,
  })
}
