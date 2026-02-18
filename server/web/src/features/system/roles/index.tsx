import {getRouteApi} from '@tanstack/react-router'
import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'
import {RolesDialogs} from './components/roles-dialogs'
import {RolesPrimaryButtons} from './components/roles-primary-buttons'
import {RolesProvider} from './components/roles-provider'
import {RolesTable} from './components/roles-table'

const route = getRouteApi('/_authenticated')

export function Roles() {
    const search = route.useSearch()
    const navigate = route.useNavigate()

    return (
        <RolesProvider>
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
                        <h2 className='text-2xl font-bold tracking-tight'>角色管理</h2>
                        <p className='text-muted-foreground'>
                            管理用户角色和跨租户权限。
                        </p>
                    </div>
                    <RolesPrimaryButtons/>
                </div>
                <div className='-mx-4 flex-1 overflow-auto px-4 py-1 lg:flex-row lg:space-y-0 lg:space-x-12'>
                    <RolesTable data={[]} search={search} navigate={navigate}/>
                </div>
            </Main>

            <RolesDialogs/>
        </RolesProvider>
    )
}