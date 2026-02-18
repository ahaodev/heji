import type React from 'react'
import { usePermission } from '@/hooks/usePermission'

interface PermissionGuardProps {
  permission?: string
  permissions?: string[]
  requireAll?: boolean
  fallback?: React.ReactNode
  children: React.ReactNode
}

export function PermissionGuard({ 
  permission, 
  permissions, 
  requireAll = false,
  fallback = null,
  children 
}: PermissionGuardProps) {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission()
  
  let hasAccess = false
  
  if (permission) {
    hasAccess = hasPermission(permission)
  } else if (permissions) {
    hasAccess = requireAll 
      ? hasAllPermissions(permissions)
      : hasAnyPermission(permissions)
  }
  
  return hasAccess ? <>{children}</> : <>{fallback}</>
}

// 使用示例:
// <PermissionGuard permission="admin:user:list">
//   <UserTable />
// </PermissionGuard>
// 
// <PermissionGuard 
//   permissions={["admin:user:create", "admin:user:edit"]}
//   requireAll={false}
// >
//   <Button>新增用户</Button>
// </PermissionGuard>