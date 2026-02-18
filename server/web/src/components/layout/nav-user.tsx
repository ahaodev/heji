import {Link} from '@tanstack/react-router'
import {BadgeCheck, ChevronsUpDown, LogOut} from 'lucide-react'
import useDialogState from '@/hooks/use-dialog-state'
import {useAuthStore} from '@/stores/auth-store'
import {Avatar, AvatarFallback, AvatarImage} from '@/components/ui/avatar'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {SidebarMenu, SidebarMenuButton, SidebarMenuItem, useSidebar,} from '@/components/ui/sidebar'
import {SignOutDialog} from '@/components/sign-out-dialog'

type NavUserProps = {
    user?: {
        name: string
        email: string
        avatar: string
    }
}

export function NavUser({user: propUser}: NavUserProps) {
    const {isMobile} = useSidebar()
    const [open, setOpen] = useDialogState()
    const {auth} = useAuthStore()

    // Use profile data from auth store, fallback to user, then prop user
    const user = auth.profile ? {
        name: auth.profile.username,
        email: auth.profile.email,
        avatar: auth.profile.avatar || ''
    } : auth.user ? {
        name: auth.user.accountNo || 'Admin',
        email: auth.user.email,
        avatar: ''
    } : propUser

    return (
        <>
            <SidebarMenu>
                <SidebarMenuItem>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <SidebarMenuButton
                                size='lg'
                                className='data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground'
                            >
                                <Avatar className='h-8 w-8 rounded-lg'>
                                    <AvatarImage src={user?.avatar} alt={user?.name}/>
                                    <AvatarFallback className='rounded-lg'>
                                        {user?.name ? user.name.charAt(0).toUpperCase() : 'A'}
                                    </AvatarFallback>
                                </Avatar>
                                <div className='grid flex-1 text-start text-sm leading-tight'>
                                    <span className='truncate font-semibold'>{user?.name}</span>
                                    <span className='truncate text-xs'>{user?.email}</span>
                                </div>
                                <ChevronsUpDown className='ms-auto size-4'/>
                            </SidebarMenuButton>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent
                            className='w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg'
                            side={isMobile ? 'bottom' : 'right'}
                            align='end'
                            sideOffset={4}
                        >
                            <DropdownMenuLabel className='p-0 font-normal'>
                                <div className='flex items-center gap-2 px-1 py-1.5 text-start text-sm'>
                                    <Avatar className='h-8 w-8 rounded-lg'>
                                        <AvatarImage src={user?.avatar} alt={user?.name}/>
                                        <AvatarFallback className='rounded-lg'>
                                            {user?.name ? user.name.charAt(0).toUpperCase() : 'A'}
                                        </AvatarFallback>
                                    </Avatar>
                                    <div className='grid flex-1 text-start text-sm leading-tight'>
                                        <span className='truncate font-semibold'>{user?.name}</span>
                                        <span className='truncate text-xs'>{user?.email}</span>
                                    </div>
                                </div>
                            </DropdownMenuLabel>
                            <DropdownMenuSeparator/>

                            <DropdownMenuSeparator/>
                            <DropdownMenuGroup>
                                <DropdownMenuItem asChild>
                                    <Link to='/settings/account'>
                                        <BadgeCheck/>
                                        Account
                                    </Link>
                                </DropdownMenuItem>
                            </DropdownMenuGroup>
                            <DropdownMenuSeparator/>
                            <DropdownMenuItem onClick={() => setOpen(true)}>
                                <LogOut/>
                                登出
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </SidebarMenuItem>
            </SidebarMenu>

            <SignOutDialog open={!!open} onOpenChange={setOpen}/>
        </>
    )
}
