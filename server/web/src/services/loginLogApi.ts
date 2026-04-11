import { apiClient } from '@/services/config'
import { buildSearchParams } from '@/lib/query-params'
import type {
  PaginatedLoginLogsResponse,
  LoginLogFilter,
} from '@/features/system/login-logs/data/schema'

// Login Log Management API - Based on swagger.json /system/login-logs endpoints

// GET /system/login-logs - Get login logs with pagination and filtering
export async function getLoginLogs(
  params?: LoginLogFilter
): Promise<PaginatedLoginLogsResponse> {
  const searchParams = buildSearchParams(params)

  const response = await apiClient.get(
    `/api/v1/system/login-logs?${searchParams}`
  )
  return response.data.data
}

// DELETE /system/login-logs - Clear all login logs
export async function clearAllLoginLogs(): Promise<string> {
  const response = await apiClient.delete('/api/v1/system/login-logs')
  return response.data.data
}
