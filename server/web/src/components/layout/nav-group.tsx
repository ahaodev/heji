import React, {type ReactNode} from 'react'
import {Link, useLocation} from '@tanstack/react-router'
import {ChevronDown, ChevronRight} from 'lucide-react'
import {Collapsible, CollapsibleContent, CollapsibleTrigger,} from '@/components/ui/collapsible'
import {
    SidebarGroup,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarMenuSub,
    SidebarMenuSubButton,
    SidebarMenuSubItem,
    useSidebar,
} from '@/components/ui/sidebar'
import {Badge} from '../ui/badge'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '../ui/dropdown-menu'
import {type NavCollapsible, type NavGroup as NavGroupProps, type NavItem, type NavLink,} from './types'
import {getIconByName} from '@/lib/icons'

export function NavGroup({title, icon, items, url, badge}: NavGroupProps) {
    const {state, isMobile} = useSidebar()
    const href = useLocation({select: (location) => location.href})
    const {setOpenMobile} = useSidebar()

    // 当侧边栏折叠时，显示为下拉菜单
    if (state === 'collapsed' && !isMobile) {
        return <NavGroupCollapsedDropdown title={title} icon={icon} items={items} href={href} url={url} badge={badge}/>
    }

    // If NavGroup has a direct URL (no items), render as a direct menu item
    //// 如果 NavGroup 直接提供了 URL（没有子项），则渲染为单个菜单项
    if (url && !items) {
        const groupAsItem = {title, url, icon, badge}

        return (
            <SidebarGroup className='p-0 px-2'>
                <SidebarMenu>
                    <SidebarMenuItem className='h-12'>
                        <SidebarMenuButton
                            asChild
                            isActive={checkIsActive(href, groupAsItem)}
                            tooltip={title}
                            size='lg'
                            className=''
                        >
                            <Link to={url} onClick={() => setOpenMobile(false)}>
                                {icon && React.createElement(getIconByName(icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                                <span>{title}</span>
                                {badge && <NavBadge>{badge}</NavBadge>}
                            </Link>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarGroup>
        )
    }

    // If no items, skip rendering
    if (!items || items.length === 0) {
        return null
    }

    // For single-item groups, render as a direct menu item without collapsible
    //对于单项组，呈现为直接菜单项，无需可折叠
    if (items.length === 1 && !items[0].items) {
        const singleItem = items[0]

        return (
            <SidebarGroup className='p-0 px-2'>
                <SidebarMenu>
                    <SidebarMenuItem className='h-12'>
                        <SidebarMenuButton
                            asChild
                            isActive={checkIsActive(href, singleItem)}
                            tooltip={singleItem.title}
                        >
                            <Link to={singleItem.url} onClick={() => setOpenMobile(false)}>
                                {singleItem.icon && React.createElement(getIconByName(singleItem.icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                                <span>{singleItem.title}</span>
                                {singleItem.badge && <NavBadge>{singleItem.badge}</NavBadge>}
                            </Link>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarGroup>
        )
    }

    return (
        <Collapsible
            defaultOpen={false} // 默认折叠
            className='group/collapsible'
        >
            <SidebarGroup className='p-0 px-2'>
                <SidebarGroupLabel asChild>
                    <CollapsibleTrigger className='h-12'>
                        <div className='flex items-center'>
                            {icon ? React.createElement(getIconByName(icon) || 'div', {className: 'mr-2 h-4 w-4'}) : null}
                            <span className='text-base'>{title}</span>
                        </div>
                        <ChevronDown
                            className="ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180"/>
                    </CollapsibleTrigger>
                </SidebarGroupLabel>
                <CollapsibleContent>
                    <SidebarMenu className='pl-4'>
                        {items.map((item) => {
                            const key = `${item.title}-${item.url}`

                            if (!item.items)
                                return <SidebarMenuLink key={key} item={item} href={href}/>

                            return <SidebarMenuCollapsible key={key} item={item} href={href}/>
                        })}
                    </SidebarMenu>
                </CollapsibleContent>
            </SidebarGroup>
        </Collapsible>
    )
}

function NavGroupCollapsedDropdown({title, icon, items, href, url, badge}: {
    title: string
    icon?: string
    items?: NavItem[]
    href: string
    url?: string
    badge?: string
}) {
    const {setOpenMobile} = useSidebar()
    // If this is a direct URL NavGroup, render as a single menu button in collapsed state
    if (url && !items) {
        const groupAsItem = {title, url, icon, badge}

        return (
            <SidebarGroup className='p-0 px-2'>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton
                            asChild
                            isActive={checkIsActive(href, groupAsItem)}
                            tooltip={title}
                        >
                            <Link to={url} onClick={() => setOpenMobile(false)}>
                                {icon ? (
                                    React.createElement(getIconByName(icon) || 'div', {className: 'h-4 w-4'})
                                ) : (
                                    <span className='text-sm font-semibold'>{title.charAt(0)}</span>
                                )}
                                {badge && <NavBadge>{badge}</NavBadge>}
                            </Link>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarGroup>
        )
    }

    // Handle single-item groups in collapsed state
    if (items && items.length === 1 && !items[0].items) {
        const singleItem = items[0]

        return (
            <SidebarGroup className='p-0 px-2'>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton
                            asChild
                            isActive={checkIsActive(href, singleItem)}
                            tooltip={singleItem.title}
                        >
                            <Link to={singleItem.url} onClick={() => setOpenMobile(false)}>
                                {singleItem.icon ? (
                                    React.createElement(getIconByName(singleItem.icon) || 'div', {className: 'h-4 w-4'})
                                ) : (
                                    <div className='h-4 w-4 flex items-center justify-center'>
                                        <span
                                            className='text-xs font-semibold'>{singleItem.title.charAt(0).toUpperCase()}</span>
                                    </div>
                                )}
                                {singleItem.badge && <NavBadge>{singleItem.badge}</NavBadge>}
                            </Link>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarGroup>
        )
    }

    if (!items || items.length === 0) {
        return null
    }

    return (
        <SidebarGroup className='p-0 px-2'>
            <SidebarMenu>
                <SidebarMenuItem>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <SidebarMenuButton tooltip={title}>
                                {icon ? (
                                    React.createElement(getIconByName(icon) || 'div', {className: 'h-4 w-4 shrink-0'})
                                ) : (
                                    /* Fallback to first letter of title */
                                    <div className='h-4 w-4 flex items-center justify-center shrink-0'>
                                        <span className='text-xs font-semibold'>{title.charAt(0).toUpperCase()}</span>
                                    </div>
                                )}
                            </SidebarMenuButton>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent side='right' align='start' sideOffset={4} className='w-56'>
                            <DropdownMenuLabel>{title}</DropdownMenuLabel>
                            <DropdownMenuSeparator/>
                            {items.map((item) => {
                                if (!item.items) {
                                    // Simple menu item
                                    return (
                                        <DropdownMenuItem key={`${item.title}-${item.url}`} asChild>
                                            <Link
                                                to={'url' in item ? item.url : '#'}
                                                onClick={() => setOpenMobile(false)}
                                                className={`${checkIsActive(href, item) ? 'bg-secondary' : ''}`}
                                            >
                                                {item.icon && React.createElement(getIconByName(item.icon) || 'div', {className: 'mr-2 h-4 w-4'})}
                                                <span>{item.title}</span>
                                                {item.badge && (
                                                    <span className='ml-auto text-xs'>{item.badge}</span>
                                                )}
                                            </Link>
                                        </DropdownMenuItem>
                                    )
                                } else {
                                    // Nested menu item with submenu - flatten for simplicity in collapsed mode
                                    return item.items.map((subItem) => (
                                        <DropdownMenuItem key={`${item.title}-${subItem.title}`} asChild>
                                            <Link
                                                to={'url' in subItem ? subItem.url : '#'}
                                                onClick={() => setOpenMobile(false)}
                                                className={`${checkIsActive(href, subItem) ? 'bg-secondary' : ''} pl-6`}
                                            >
                                                {subItem.icon && React.createElement(getIconByName(subItem.icon) || 'div', {className: 'mr-2 h-4 w-4'})}
                                                <span>{subItem.title}</span>
                                                {subItem.badge && (
                                                    <span className='ml-auto text-xs'>{subItem.badge}</span>
                                                )}
                                            </Link>
                                        </DropdownMenuItem>
                                    ))
                                }
                            })}
                        </DropdownMenuContent>
                    </DropdownMenu>
                </SidebarMenuItem>
            </SidebarMenu>
        </SidebarGroup>
    )
}

function NavBadge({children}: { children: ReactNode }) {
    return <Badge className='rounded-full px-1 py-0 text-xs'>{children}</Badge>
}

function SidebarMenuLink({item, href}: { item: NavLink; href: string }) {
    const {setOpenMobile} = useSidebar()
    return (
        <SidebarMenuItem>
            <SidebarMenuButton
                asChild
                isActive={checkIsActive(href, item)}
                tooltip={item.title}
                size='lg'
            >
                <Link to={item.url} onClick={() => setOpenMobile(false)}>
                    {item.icon && React.createElement(getIconByName(item.icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                    <span>{item.title}</span>
                    {item.badge && <NavBadge>{item.badge}</NavBadge>}
                </Link>
            </SidebarMenuButton>
        </SidebarMenuItem>
    )
}

// 可折叠的侧边栏
function SidebarMenuCollapsible({item, href,}: {
    item: NavCollapsible
    href: string
}) {
    const {setOpenMobile} = useSidebar()
    return (
        <Collapsible
            asChild
            defaultOpen={checkIsActive(href, item, true)}
            className='group/collapsible'
        >
            <SidebarMenuItem>
                <CollapsibleTrigger asChild>
                    <SidebarMenuButton tooltip={item.title} size='lg'>
                        {item.icon && React.createElement(getIconByName(item.icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                        <span>{item.title}</span>
                        {item.badge && <NavBadge>{item.badge}</NavBadge>}
                        <ChevronRight
                            className='ms-auto transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90'/>
                    </SidebarMenuButton>
                </CollapsibleTrigger>
                <CollapsibleContent className='CollapsibleContent'>
                    <SidebarMenuSub>
                        {item.items.map((subItem) => {
                            // Handle nested submenus (like 日志管理 with children)
                            if ('items' in subItem && subItem.items && subItem.items.length > 0) {
                                return (
                                    <Collapsible
                                        key={subItem.title}
                                        asChild
                                        defaultOpen={checkIsActive(href, subItem, true)}
                                        className='group/nested-collapsible'
                                    >
                                        <SidebarMenuSubItem>
                                            <CollapsibleTrigger asChild>
                                                <SidebarMenuSubButton>
                                                    {subItem.icon && React.createElement(getIconByName(subItem.icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                                                    <span>{subItem.title}</span>
                                                    {subItem.badge && <NavBadge>{subItem.badge}</NavBadge>}
                                                    <ChevronRight
                                                        className='ms-auto transition-transform duration-200 group-data-[state=open]/nested-collapsible:rotate-90'/>
                                                </SidebarMenuSubButton>
                                            </CollapsibleTrigger>
                                            <CollapsibleContent>
                                                <SidebarMenuSub>
                                                    {subItem.items.map((nestedItem) => (
                                                        <SidebarMenuSubItem key={nestedItem.title}>
                                                            <SidebarMenuSubButton
                                                                asChild
                                                                isActive={checkIsActive(href, nestedItem)}
                                                            >
                                                                <Link to={'url' in nestedItem ? nestedItem.url : '#'}
                                                                      onClick={() => setOpenMobile(false)}>
                                                                    {nestedItem.icon && React.createElement(getIconByName(nestedItem.icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                                                                    <span>{nestedItem.title}</span>
                                                                    {nestedItem.badge &&
                                                                        <NavBadge>{nestedItem.badge}</NavBadge>}
                                                                </Link>
                                                            </SidebarMenuSubButton>
                                                        </SidebarMenuSubItem>
                                                    ))}
                                                </SidebarMenuSub>
                                            </CollapsibleContent>
                                        </SidebarMenuSubItem>
                                    </Collapsible>
                                )
                            } else {
                                // Regular submenu item
                                return (
                                    <SidebarMenuSubItem key={subItem.title}>
                                        <SidebarMenuSubButton
                                            asChild
                                            isActive={checkIsActive(href, subItem)}
                                        >
                                            <Link to={'url' in subItem ? subItem.url : '#'}
                                                  onClick={() => setOpenMobile(false)}>
                                                {subItem.icon && React.createElement(getIconByName(subItem.icon) || 'div', {className: 'h-4 w-4 shrink-0'})}
                                                <span>{subItem.title}</span>
                                                {subItem.badge && <NavBadge>{subItem.badge}</NavBadge>}
                                            </Link>
                                        </SidebarMenuSubButton>
                                    </SidebarMenuSubItem>
                                )
                            }
                        })}
                    </SidebarMenuSub>
                </CollapsibleContent>
            </SidebarMenuItem>
        </Collapsible>
    )
}


function checkIsActive(href: string, item: NavItem, mainNav = false) {
    return (
        href === item.url || // /endpint?search=param
        href.split('?')[0] === item.url || // endpoint
        !!item?.items?.filter((i) => i.url === href).length || // if child nav is active
        (mainNav &&
            href.split('/')[1] !== '' &&
            href.split('/')[1] === item?.url?.split('/')[1])
    )
}
