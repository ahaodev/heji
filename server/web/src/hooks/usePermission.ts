import {useAuthStore} from '@/stores/auth-store'

export function usePermission() {
    const {auth} = useAuthStore()

    const hasPermission = (permission: string): boolean => {
        if (!auth.permissions) return false

        // 管理员自动放行
        if (auth.permissions.is_admin) return true

        // 权限标识匹配
        return auth.permissions.permissions.some(p => {
            // 支持字符串数组 ["admin", "user", "read"] 或单个字符串
            const permStr = Array.isArray(p) ? (p.length === 1 ? p[0] : p.join(':')) : p
            return permStr === permission || matchWildcard(permStr, permission)
        })
    }

    const hasAnyPermission = (permissions: string[]): boolean => {
        return permissions.some(p => hasPermission(p))
    }

    const hasAllPermissions = (permissions: string[]): boolean => {
        return permissions.every(p => hasPermission(p))
    }

    const hasRole = (role: string): boolean => {
        return auth.hasRole(role)
    }

    const canAccessMenu = (menuId: string): boolean => {
        return auth.canAccessMenu(menuId)
    }



    return {
        hasPermission,
        hasAnyPermission,
        hasAllPermissions,
        hasRole,
        canAccessMenu,
    }
}

// 通配符匹配: system:*:* 匹配 system:user:read
function matchWildcard(pattern: string, permission: string): boolean {
    const patternParts = pattern.split(':')
    const permParts = permission.split(':')

    if (patternParts.length !== permParts.length) return false

    return patternParts.every((part, index) =>
        part === '*' || part === permParts[index]
    )
}