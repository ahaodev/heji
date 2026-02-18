import { apiClient } from './config'
import { type MenuTreeNode, type ResourcesResponse} from '@/types/menu'

/**
 * Resource API service for fetching menu resources from backend
 * Based on the resource_controller.go endpoints
 */

// GET /resources - Get resources (menu tree)
export const getResources = async (): Promise<MenuTreeNode[]> => {
  try {
    const response = await apiClient.get(`/api/v1/resources`)

    if (response.data && response.data.data) {
      const resourcesData: ResourcesResponse = response.data.data
      console.log('API Response - Total menu items received:', resourcesData.menus?.length || 0)
      console.log('API Response - Permissions:', resourcesData.permissions)
      return resourcesData.menus || []
    } else {
      console.warn('No menu data returned from API')
      return []
    }
  } catch (error) {
    console.error('Failed to fetch resources:', error)
    throw error
  }
}

// GET /resources - Get complete resources with permissions
export const getResourcesWithPermissions = async (): Promise<ResourcesResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/resources`)

    if (response.data && response.data.data) {
      const resourcesData: ResourcesResponse = response.data.data
      console.log('API Response - Complete resources:', resourcesData)
      return resourcesData
    } else {
      console.warn('No resources data returned from API')
      return { menus: [], permissions: null }
    }
  } catch (error) {
    console.error('Failed to fetch resources:', error)
    throw error
  }
}
