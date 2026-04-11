import { type MenuTreeNode, type ResourcesResponse } from '@/types/menu'
import { apiClient } from './config'

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
      console.log(
        'API Response - Total menu items received:',
        resourcesData.menus?.length || 0
      )
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
export const getResourcesWithPermissions =
  async (): Promise<ResourcesResponse> => {
    try {
      const response = await apiClient.get(`/api/v1/resources`)

      if (response.data && response.data.data) {
        return response.data.data
      } else {
        return { menus: [], permissions: null, roles: [], is_admin: false }
      }
    } catch (error) {
      console.error('Failed to fetch resources:', error)
      throw error
    }
  }
