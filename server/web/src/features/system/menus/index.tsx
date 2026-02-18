import {ConfigDrawer} from '@/components/config-drawer'
import {Header} from '@/components/layout/header'
import {Main} from '@/components/layout/main'
import {ProfileDropdown} from '@/components/profile-dropdown'
import {Search} from '@/components/search'
import {ThemeSwitch} from '@/components/theme-switch'
import {MenusDialogs} from './components/menus-dialogs'
import {MenusPrimaryButtons} from './components/menus-primary-buttons'
import {MenusProvider, useMenus} from './components/menus-provider'
import {MenusTable} from './components/menus-table'
import type {Menu} from '@/types/menu'

function MenusContent() {
    const {setCurrentRow} = useMenus()

    const handleMenuSelect = (menu: Menu) => {
        setCurrentRow(menu)
        console.log('Selected menu:', menu)
    }

    return (
        <>
            <Main>
                <div className='mb-2 flex flex-wrap items-center justify-between space-y-2'>
                    <div>
                        <h2 className='text-2xl font-bold tracking-tight'>菜单管理</h2>
                        <p className='text-muted-foreground'>
                            管理您的菜单
                        </p>
                    </div>
                    <MenusPrimaryButtons/>
                </div>

                <div className='-mx-4 flex-1 overflow-auto px-4 py-1'>
                    <MenusTable onMenuSelect={handleMenuSelect}/>
                </div>
            </Main>

            <MenusDialogs/>
        </>
    )
}

export function Menus() {
    return (
        <MenusProvider>
            <Header fixed>
                <Search/>
                <div className='ms-auto flex items-center space-x-4'>
                    <ThemeSwitch/>
                    <ConfigDrawer/>
                    <ProfileDropdown/>
                </div>
            </Header>

            <MenusContent/>
        </MenusProvider>
    )
}