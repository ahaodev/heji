import {getRouteApi} from '@tanstack/react-router'
import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'
import {DictsProvider} from './components/dicts-provider'
import {DictsPrimaryButtons} from './components/dicts-primary-buttons'
import {DictTypesTable} from './components/dict-types-table'
import {DictsDialogs} from './components/dicts-dialogs'
import {useDictTypes} from './hooks/use-dict-types'
import {Skeleton} from '@/components/ui/skeleton'

const route = getRouteApi('/_authenticated/system/dict')

export function Dicts() {
    const search = route.useSearch()
    const navigate = route.useNavigate()

    const queryParams = {
        page: search.page || 1,
        page_size: search.page_size || 10,
        search: search.search || undefined,
        status: search.status || undefined,
    }

    const {data: dictTypesData, isLoading, error} = useDictTypes(queryParams)

    const navigateWrapper = ({search: searchUpdate, replace}: { search: any; replace?: boolean }) => {
        navigate({search: searchUpdate, replace})
    }

    return (
        <DictsProvider>
            <Header fixed>
                <Search/>
                <div className='ms-auto flex items-center space-x-4'>
                    <ThemeSwitch/>
                    <ConfigDrawer/>
                    <ProfileDropdown/>
                </div>
            </Header>

            <Main>
                <div className='mb-2 space-y-2'>
                    <div className='flex items-center justify-between'>
                        <h2 className='text-2xl font-bold tracking-tight'>字典管理</h2>
                        <div className='ml-auto'>
                            <DictsPrimaryButtons/>
                        </div>
                    </div>
                    <p className='text-muted-foreground'>管理系统字典类型和字典项，配置业务数据字典。</p>
                </div>

                <div className='-mx-4 flex-1 overflow-auto px-4 py-1'>
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
                        <div
                            className='flex h-32 items-center justify-center text-muted-foreground'>加载字典数据失败，请重试</div>
                    ) : (
                        <div className='grid grid-cols-1 gap-6'>
                            <div>
                                <DictTypesTable
                                    data={dictTypesData?.list || []}
                                    search={search}
                                    navigate={navigateWrapper}
                                    totalCount={dictTypesData?.total || 0}
                                />
                            </div>
                        </div>
                    )}
                </div>
            </Main>

            <DictsDialogs/>
        </DictsProvider>
    )
}
