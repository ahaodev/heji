import { apiClient } from '@/services/config'
import type {
  PaginatedLoginLogsResponse,
  LoginLogFilter
} from '@/features/system/login-logs/data/schema'

// Login Log Management API - Based on swagger.json /system/login-logs endpoints

// GET /system/login-logs - Get login logs with pagination and filtering
export async function getLoginLogs(params?: LoginLogFilter): Promise<PaginatedLoginLogsResponse> {
  const searchParams = new URLSearchParams()

  if (params?.page) searchParams.append('page', params.page.toString())
  if (params?.page_size) searchParams.append('page_size', params.page_size.toString())
  if (params?.username) searchParams.append('username', params.username)
  if (params?.login_ip) searchParams.append('login_ip', params.login_ip)
  if (params?.status) searchParams.append('status', params.status)
  if (params?.browser) searchParams.append('browser', params.browser)
  if (params?.os) searchParams.append('os', params.os)
  if (params?.start_time) searchParams.append('start_time', params.start_time)
  if (params?.end_time) searchParams.append('end_time', params.end_time)
  if (params?.sort_by) searchParams.append('sort_by', params.sort_by)
  if (params?.order) searchParams.append('order', params.order)

  const response = await apiClient.get(`/api/v1/system/login-logs?${searchParams}`)
  return response.data.data
}

// DELETE /system/login-logs - Clear all login logs
export async function clearAllLoginLogs(): Promise<string> {
  const response = await apiClient.delete('/api/v1/system/login-logs')
  return response.data.data
}