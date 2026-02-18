// 角色信息 (对应后端RoleInfo)
export interface RoleInfo {
    id: string          // 角色ID
    name: string        // 角色显示名称
    type: string        // 角色类型
}

// Role Management
export interface Role {
    id: string
    name: string
    sequence: number
    status: 'active' | 'inactive'
    created_at: Date
    updated_at: Date
    menu_ids?: string[]
}

export interface CreateRoleRequest {
    name: string
    sequence: number
    status?: 'active' | 'inactive'
    menu_ids?: string[]
}

export interface UpdateRoleRequest {
    name?: string
    sequence?: number
    status?: 'active' | 'inactive'
    menu_ids?: string[]
}

export interface RolePagedResult {
    list: Role[]
    total: number
    page: number
    page_size: number
    total_pages: number
}


export interface AssignRoleRequest {
    user_id: string
    role_id: string
}