export type UserRole = 'admin' | 'user' | 'viewer';

export type UserStatus = 'active' | 'inactive' | 'invited' | 'suspended';

export interface User {
    id: string;
    username: string;
    email: string;
    phone?: string;
    avatar?: string;
    status: UserStatus;
    created_at: Date;
    updated_at: Date;
    invited_at?: Date;
    invited_by?: string;
    roles?: string[];
}

export interface CreateUserRequest {
    username: string;
    email: string;
    phone?: string;
    password: string;
    status?: UserStatus;
    role_ids?: string[];
}

export interface InviteUserRequest {
    email: string;
    role_ids?: string[];
    message?: string;
}

export interface UserUpdateRequest {
    username?: string;
    email?: string;
    phone?: string;
    password?: string;
    status?: UserStatus;
    avatar?: string;
    role_ids?: string[];
}

export interface MenuItem {
    id: string;
    name: string;
    path: string;
    icon: string;
    component: string;
    sort: number;
    visible: boolean;
    requiresAuth: boolean;
    requiresAdmin: boolean;
}

export interface UserPermissions {
    user_id: string;
    permissions: string[][];
    roles: string[];
    menus: MenuItem[];        // 用户可访问的菜单列表
    is_admin: boolean;        // 是否管理员
}

