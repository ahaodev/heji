export interface Menu {
    id: string
    name: string
    sequence: number
    type: MenuType
    path?: string
    icon: string
    component?: string
    route_name?: string
    query?: string
    is_frame: boolean
    visible: string // show/hide
    permissions?: string
    status: MenuStatus
    parent_id?: string
    children?: Menu[]
    roles?: string[]
    apiResources?:string[]
}

export interface MenuTreeNode {
    id: string
    name: string
    sequence: number
    type: MenuType
    path?: string
    icon: string
    component?: string
    route_name?: string
    query?: string
    is_frame: boolean
    visible: string // show/hide
    permissions?: string
    status: MenuStatus
    parent_id?: string
    children?: MenuTreeNode[]
    roles?: string[]
    apiResources?:string[]
}

export interface CreateMenuRequest {
    name: string
    sequence: number
    type: MenuType
    path?: string
    icon: string
    component?: string
    route_name?: string
    query?: string
    is_frame: boolean
    visible: string // show/hide
    permissions?: string
    status?: MenuStatus
    parent_id?: string
    apiResources?:string[]
}

export interface UpdateMenuRequest {
    name: string
    sequence: number
    type: MenuType
    path?: string
    icon: string
    component?: string
    route_name?: string
    query?: string
    is_frame: boolean
    visible: string // show/hide
    permissions?: string
    status: MenuStatus
    parent_id?: string
    apiResources?:string[]
}

export interface MenuPagedResult {
    list: Menu[]
    total: number
    page: number
    page_size: number
    total_pages: number
}

export interface MenuQueryParams {
    type?: MenuType
    status?: MenuStatus
    parent_id?: string
    search?: string
    page?: number
    page_size?: number
}
// Resources response structure
export interface ResourcesResponse {
    menus: MenuTreeNode[]
    permissions: string[] | null
}

// Type definitions
export type MenuType = 'menu' | 'button'
export type MenuStatus = 'active' | 'inactive'
