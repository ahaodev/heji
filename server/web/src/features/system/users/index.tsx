import {getRouteApi} from '@tanstack/react-router'
import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'
import {UsersDialogs} from './components/users-dialogs'
import {UsersPrimaryButtons} from './components/users-primary-buttons'
import {UsersProvider} from './components/users-provider'
import {UsersTable} from './components/users-table'
import {useUsers} from './hooks/use-users'
import {Skeleton} from '@/components/ui/skeleton'

const route = getRouteApi('/_authenticated/system/user')

export function Users() {
    const search = route.useSearch()
    const navigate = route.useNavigate()

    // Extract query parameters from URL search
    const queryParams = {
        page: search.page || 1,
        page_size: search.pageSize || 10,
        search: search.username || undefined,
        status: search.status ? (Array.isArray(search.status) ? search.status.join(',') : search.status) : undefined,
    }

    const {data: usersData, isLoading, error} = useUsers(queryParams)

    // Create a wrapper function to match NavigateFn type
    const navigateWrapper = ({search: searchUpdate, replace}: { search: any, replace?: boolean }) => {
        navigate({search: searchUpdate, replace})
    }

    return (
        <UsersProvider>
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
                        <h2 className='text-2xl font-bold tracking-tight'>用户列表</h2>
                        <p className='text-muted-foreground'>
                            在此管理您的用户及其角色。
                        </p>
                    </div>
                    <UsersPrimaryButtons/>
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
                            加载用户数据失败，请重试
                        </div>
                    ) : (
                        <UsersTable
                            data={usersData?.list || []}
                            search={search}
                            navigate={navigateWrapper}
                            totalCount={usersData?.total || 0}
                        />
                    )}
                </div>
            </Main>

            <UsersDialogs/>
        </UsersProvider>
    )
}
