import type { NavGroup } from '@/components/layout/types'
import { getResourcesWithPermissions } from './resourceApi'
import { BackendMenuAdapter } from '@/lib/backend-menu-adapter'
import { useAuthStore } from '@/stores/auth-store'

/**
 * Menu service for loading and caching menu data from backend API
 */
export class MenuService {
  private cachedMenuData: NavGroup[] | null = null
  private backendAdapter = new BackendMenuAdapter()
  private isLoading: boolean = false
  private loadPromise: Promise<NavGroup[]> | null = null

  /**
   * Load menu data from backend API
   * Fetches menu resources from the backend
   */
  async loadMenuData(): Promise<NavGroup[]> {
    // Return cached data if available
    if (this.cachedMenuData) {
      return this.cachedMenuData
    }

    // If already loading, return the existing promise
    if (this.isLoading && this.loadPromise) {
      return this.loadPromise
    }

    // Start loading
    this.isLoading = true
    this.loadPromise = this._doLoadMenuData()

    try {
      const result = await this.loadPromise
      return result
    } finally {
      this.isLoading = false
      this.loadPromise = null
    }
  }

  /**
   * Internal method to actually load menu data
   */
  private async _doLoadMenuData(): Promise<NavGroup[]> {
    try {
      // Fetch complete resources data including permissions
      const resourcesData = await getResourcesWithPermissions()

      // Set permissions to auth store
      if (resourcesData.permissions) {
        const { setPermissions } = useAuthStore.getState().auth
        setPermissions({
          permissions: resourcesData.permissions.map(p => [p]), // Convert string[] to string[][]
          roles: [], // TODO: get roles from API if available
          menus: resourcesData.menus.map(m => m.id), // Extract menu IDs
          is_admin: false // TODO: determine admin status from API
        })
        console.log('Permissions set to auth store:', resourcesData.permissions)
      }

      // Transform the backend data using the backend adapter
      const navGroups = this.backendAdapter.transformToNavGroups(resourcesData.menus)

      // Cache the transformed data
      this.cachedMenuData = navGroups

      return navGroups
    } catch (error) {
      console.error('Failed to load menu data from backend:', error)
      // Return empty array if backend fails
      return []
    }
  }

  /**
   * Get cached menu data synchronously
   */
  getCachedMenuData(): NavGroup[] | null {
    return this.cachedMenuData
  }

  /**
   * Clear cache and reload menu data
   */
  async reloadMenuData(): Promise<NavGroup[]> {
    this.cachedMenuData = null
    return this.loadMenuData()
  }

  /**
   * Clear cached menu data
   */
  clearCache(): void {
    const hadCache = this.cachedMenuData !== null
    console.log('Clearing menu cache - had cache:', hadCache, 'cached groups:', this.cachedMenuData?.length || 0)
    this.cachedMenuData = null
    this.isLoading = false
    this.loadPromise = null
  }
}

// Export singleton instance
export const menuService = new MenuService()