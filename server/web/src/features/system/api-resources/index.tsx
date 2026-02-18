import {getRouteApi} from '@tanstack/react-router'
import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'
import {Skeleton} from '@/components/ui/skeleton'
import {ApiResourcesProvider} from './components/api-resources-provider'
import {ApiResourcesTable} from './components/api-resources-table'
import {useApiResources} from './hooks/use-api-resources'
import {type NavigateFn} from '@/hooks/use-table-url-state'

const route = getRouteApi('/_authenticated/system/api-resources')

export function ApiResources() {
    const search = route.useSearch()
    const navigate = route.useNavigate()

    // Extract query parameters from URL search
    const queryParams = {
        page: search.page || 1,
        page_size: search.page_size || 10,
        module: search.module ? (Array.isArray(search.module) ? search.module.join(',') : search.module) : undefined,
        method: search.method || undefined,
        path: search.path || undefined,
    }

    // Use regular hook with forced refresh on parameter changes
    const {data: apiResourcesData, isLoading, isFetching, error} = useApiResources(queryParams)

    // Create a wrapper function to match NavigateFn type
    const navigateWrapper: NavigateFn = ({search: searchUpdate, replace}) => {
        navigate({search: searchUpdate, replace})
    }

    // Reset function to clear all filters and reset to defaults
    const handleReset = () => {
        // Use window.location to force a complete navigation refresh
        window.location.href = '/system/api-resources'
    }

    return (
        <ApiResourcesProvider>
            <Header fixed>
                <Search/>
                <div className='ms-auto flex items-center space-x-4'>
                    <ThemeSwitch/>
                    <ConfigDrawer/>
                    <ProfileDropdown/>
                </div>
            </Header>

            <Main>
                <div className='mb-2 flex flex-wrap items-center justify-between space-y-2'>
                    <div>
                        <h2 className='text-2xl font-bold tracking-tight'>API资源管理</h2>
                        <p className='text-muted-foreground'>
                            管理API资源和权限绑定
                        </p>
                    </div>
                </div>
                <div className='-mx-4 flex-1 overflow-auto px-4 py-1 lg:flex-row lg:space-y-0 lg:space-x-12'>
                    {isLoading && !apiResourcesData ? (
                        <div className='space-y-4'>
                            <div className='flex items-center justify-between'>
                                <Skeleton className='h-8 w-48'/>
                                <Skeleton className='h-10 w-32'/>
                            </div>
                            <div className='space-y-2'>
                                {Array.from({length: 5}).map((_, i) => (
                                    <Skeleton key={i} className='h-16 w-full'/>
                                ))}
                            </div>
                        </div>
                    ) : error ? (
                        <div className='flex h-32 items-center justify-center text-muted-foreground'>
                            加载API资源数据失败，请重试
                        </div>
                    ) : (
                        <div className='relative'>
                            {isFetching && (
                                <div className='absolute top-0 right-0 z-10'>
                                    <div
                                        className='bg-background/80 backdrop-blur-sm rounded-md px-2 py-1 text-xs text-muted-foreground border'>
                                        更新中...
                                    </div>
                                </div>
                            )}
                            <ApiResourcesTable
                                data={apiResourcesData?.data || []}
                                search={search}
                                navigate={navigateWrapper}
                                totalCount={apiResourcesData?.total || 0}
                                onReset={handleReset}
                            />
                        </div>
                    )}
                </div>
            </Main>
        </ApiResourcesProvider>
    )
}