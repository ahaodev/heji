import {useEffect, useState} from 'react'
import {type SidebarData} from '@/components/layout/types'
import {getDynamicSidebarData, getSidebarData} from '@/components/layout/data/dynamic-sidebar-data'
import {useAuthStore} from '@/stores/auth-store'

export function useSidebarData() {
    const [sidebarData, setSidebarData] = useState<SidebarData>(getSidebarData())
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    
    // 监听用户状态变化
    const { auth } = useAuthStore()
    // 创建更稳定的用户标识符
    const userKey = JSON.stringify({
        accountNo: auth.user?.accountNo,
        email: auth.user?.email,
        profileId: auth.profile?.id,
        profileEmail: auth.profile?.email,
        accessToken: auth.accessToken ? auth.accessToken.substring(0, 20) : null // 只用token前20字符作为标识
    })

    useEffect(() => {
        let mounted = true

        const loadData = async () => {
            try {
                setIsLoading(true)
                setError(null)
                
                console.log('Loading sidebar data for user:', userKey)
                const dynamicData = await getDynamicSidebarData()

                if (mounted) {
                    setSidebarData(dynamicData)
                    console.log('Sidebar data updated, nav groups count:', dynamicData.navGroups?.length || 0)
                }
            } catch (err) {
                if (mounted) {
                    setError(err instanceof Error ? err.message : 'Failed to load menu data')
                    console.error('Failed to load sidebar data:', err)
                    // Keep the current/fallback data on error
                }
            } finally {
                if (mounted) {
                    setIsLoading(false)
                }
            }
        }

        loadData()

        return () => {
            mounted = false
        }
    }, [userKey]) // 当用户关键信息改变时重新加载数据

    const reloadData = async () => {
        try {
            setIsLoading(true)
            setError(null)
            
            // Clear cache first to force fresh data load
            const { menuService } = await import('@/services/menu-service')
            menuService.clearCache()
            console.log('Manual reload: cache cleared, loading fresh data...')

            const dynamicData = await getDynamicSidebarData()
            setSidebarData(dynamicData)
            console.log('Manual reload completed, nav groups:', dynamicData.navGroups?.length || 0)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to reload menu data')
            console.error('Manual reload failed:', err)
        } finally {
            setIsLoading(false)
        }
    }

    return {
        sidebarData,
        isLoading,
        error,
        reloadData,
    }
}