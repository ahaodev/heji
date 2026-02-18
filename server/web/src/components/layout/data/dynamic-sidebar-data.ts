import {Command} from 'lucide-react'
import {type SidebarData} from '../types'
import {menuService} from '@/services/menu-service'
import {sidebarService} from '@/services/sidebarService'

// Default fallback data
const defaultSidebarData = {
    user: {
        name: 'User',
        email: 'user@example.com',
        avatar: '/avatars/shadcn.jpg',
    },
    teams: [
        {
            name: 'Shadcn Admin',
            logo: Command,
            plan: 'Vite + ShadcnUI',
        },
    ],
}

/**
 * Get dynamic sidebar data from API
 */
export async function getDynamicSidebarData(): Promise<SidebarData> {
    try {
        // Use the new sidebar service to get all data
        const sidebarData = await sidebarService.getSidebarData()

        if (sidebarData.navGroups && sidebarData.navGroups.length > 0) {
            console.log('Dynamic sidebar data: Loaded', sidebarData.navGroups.length, 'dynamic nav groups')

            return {
                user: sidebarData.user,
                teams: sidebarData.teams,
                navGroups: sidebarData.navGroups,
            }
        } else {
            // eslint-disable-next-line no-console
            console.warn('No dynamic menu data loaded')
            return {
                user: sidebarData.user,
                teams: sidebarData.teams,
                navGroups: [],
            }
        }
    } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Failed to load dynamic sidebar data:', error)
        // Fallback to default data if everything fails
        return {
            ...defaultSidebarData,
            navGroups: [],
        }
    }
}

/**
 * Get sidebar data synchronously (returns cached dynamic data or default data)
 */
export function getSidebarData(): SidebarData {
    // Use the sidebar service to get cached data
    const cachedData = sidebarService.getCachedSidebarData()
    const cachedMenuData = menuService.getCachedMenuData()

    return {
        user: cachedData.user || defaultSidebarData.user,
        teams: cachedData.teams || defaultSidebarData.teams,
        navGroups: cachedMenuData || [],
    }
}