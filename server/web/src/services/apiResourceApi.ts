import {type ApiResource} from '@/types/api-resource'
import {apiClient} from '@/services/config'

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
export async function getApiResources(params?: ApiResourceQueryParams): Promise<ApiResourcePagedResult> {
    const searchParams = new URLSearchParams()
    if (params?.page) searchParams.append('page', params.page.toString())
    if (params?.page_size) searchParams.append('page_size', params.page_size.toString())
    if (params?.method) searchParams.append('method', params.method)
    if (params?.module) searchParams.append('module', params.module)
    if (params?.is_public !== undefined) searchParams.append('is_public', params.is_public.toString())
    if (params?.keyword) searchParams.append('keyword', params.keyword)
    if (params?.path) searchParams.append('path', params.path)

    const response = await apiClient.get(`/api/v1/system/api-resources?${searchParams}`)
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

