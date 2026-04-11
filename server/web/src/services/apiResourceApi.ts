import { apiClient } from '@/services/config'
import { type ApiResource } from '@/types/api-resource'
import { buildSearchParams } from '@/lib/query-params'

export interface ApiResourceQueryParams {
  page?: number
  page_size?: number
  method?: string
  module?: string
  is_public?: boolean
  keyword?: string
  path?: string
}

export interface ApiResourcePagedResult {
  data: ApiResource[]
  total: number
  page: number
  page_size: number
  total_pages: number
}

interface ApiResourceApiResponse {
  items: ApiResource[]
  total: number
  page: number
  page_size: number
  total_pages: number
}

// GET /system/api-resources - Get API resources with pagination
export async function getApiResources(
  params?: ApiResourceQueryParams
): Promise<ApiResourcePagedResult> {
  const searchParams = buildSearchParams(params)

  const response = await apiClient.get(
    `/api/v1/system/api-resources?${searchParams}`
  )
  const apiData: ApiResourceApiResponse = response.data.data

  // Transform API response to expected format
  return {
    data: apiData.items || [],
    total: apiData.total || 0,
    page: apiData.page || 1,
    page_size: apiData.page_size || 10,
    total_pages: apiData.total_pages || 0,
  }
}
