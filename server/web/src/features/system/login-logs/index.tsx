import {getRouteApi} from '@tanstack/react-router'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'
import {ConfigDrawer} from '@/components/config-drawer'
import {Skeleton} from '@/components/ui/skeleton'
import {LoginLogsTable} from './components/login-logs-table'
import {LogsPrimaryButtons} from './components/logs-primary-buttons'
import {useLoginLogs} from './hooks/use-login-logs'

const route = getRouteApi('/_authenticated/system/login-logs')

export function LoginLogs() {
    const search = route.useSearch()
    const navigate = route.useNavigate()

    // Extract query parameters from URL search
    const queryParams = {
        page: search.page || 1,
        page_size: search.pageSize || 10,
        username: search.username || undefined,
        status: search.status && search.status.length > 0 ? search.status[0] as 'success' | 'failed' : undefined,
    }

    const {data: logsData, isLoading, error} = useLoginLogs(queryParams)

    // Create a wrapper function to match NavigateFn type
    const navigateWrapper = ({search: searchUpdate, replace}: { search: any, replace?: boolean }) => {
        navigate({search: searchUpdate, replace})
    }

    return (
        <>
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
                        <h2 className='text-2xl font-bold tracking-tight'>登录日志</h2>
                        <p className='text-muted-foreground'>
                            查看和管理系统登录日志记录
                        </p>
                    </div>
                    <LogsPrimaryButtons/>
                </div>

                <div className='-mx-4 flex-1 overflow-auto px-4 py-1 lg:flex-row lg:space-y-0 lg:space-x-12'>
                    {isLoading ? (
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
                            加载登录日志失败，请重试
                        </div>
                    ) : (
                        <LoginLogsTable
                            data={logsData?.list || []}
                            search={search}
                            navigate={navigateWrapper}
                            totalCount={logsData?.total || 0}
                        />
                    )}
                </div>
            </Main>
        </>
    )
}