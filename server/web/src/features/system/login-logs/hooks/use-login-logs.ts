import { useQuery, useQueryClient } from '@tanstack/react-query'
import { clearAllLoginLogs, getLoginLogs } from '@/services/loginLogApi'
import { useCrudMutation } from '@/hooks/use-crud-mutation'
import type { LoginLogFilter } from '../data/schema'

// Query keys for React Query
const LOGIN_LOGS_QUERY_KEY = 'login-logs'

// Custom hook for fetching login logs with pagination and filters
export function useLoginLogs(params?: LoginLogFilter) {
  return useQuery({
    queryKey: [LOGIN_LOGS_QUERY_KEY, params],
    queryFn: () => getLoginLogs(params),
    staleTime: 30 * 1000, // 30 seconds - logs change frequently
  })
}

// Custom hook for clearing all login logs
export function useClearAllLoginLogs() {
  return useCrudMutation({
    mutationFn: clearAllLoginLogs,
    queryKeys: [[LOGIN_LOGS_QUERY_KEY]],
    successMessage: '已清空所有登录日志',
    errorMessage: '清空登录日志失败',
  })
}

// Custom hook for refreshing login logs data
export function useRefreshLoginLogs() {
  const queryClient = useQueryClient()

  return () => {
    queryClient.invalidateQueries({ queryKey: [LOGIN_LOGS_QUERY_KEY] })
  }
}
