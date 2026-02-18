import {useLayout} from '@/context/layout-provider'
import {Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarRail,} from '@/components/ui/sidebar'
import {useSidebarData} from '@/hooks/use-sidebar-data'
import {NavGroup} from './nav-group'
import {NavUser} from './nav-user'
import {TeamSwitcher} from "@/components/layout/team-switcher.tsx";

export function AppSidebar({...props}: React.ComponentProps<typeof Sidebar>) {
    const {collapsible, variant} = useLayout()
    const {sidebarData} = useSidebarData()

    return (
        <Sidebar {...props} collapsible={collapsible} variant={variant}>
            <SidebarHeader>
                <TeamSwitcher/>
            </SidebarHeader>
            <SidebarContent className='p-0'>
                {sidebarData.navGroups.map((props) => (
                    <NavGroup key={props.title} {...props} />
                ))}
            </SidebarContent>
            <SidebarFooter>
                <NavUser user={sidebarData.user}/>
            </SidebarFooter>
            <SidebarRail/>
        </Sidebar>
    )
}
