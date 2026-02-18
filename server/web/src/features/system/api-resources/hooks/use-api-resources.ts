import {useQuery} from '@tanstack/react-query'
import {
    type ApiResourcePagedResult,
    type ApiResourceQueryParams,
    getApiResources
} from '@/services/apiResourceApi'

// Query keys for React Query
const API_RESOURCES_QUERY_KEY = 'api-resources'

// Custom hook for fetching API resources with pagination and filters
export function useApiResources(params?: ApiResourceQueryParams) {
    return useQuery<ApiResourcePagedResult>({
        queryKey: [API_RESOURCES_QUERY_KEY, params],
        queryFn: () => getApiResources(params),
        staleTime: 0, // Always fetch fresh data
        gcTime: 0, // Don't cache at all to force refresh
        refetchOnWindowFocus: false,
        retry: 2,
        refetchOnMount: true, // Always refetch on mount to ensure data updates
        // Don't use placeholder data to ensure fresh data shows
        placeholderData: undefined,
    })
}

