import type React from 'react'
import { Button, type buttonVariants } from '@/components/ui/button'
import { usePermission } from '@/hooks/usePermission'
import type { VariantProps } from 'class-variance-authority'

interface PermissionButtonProps extends React.ComponentProps<'button'>, VariantProps<typeof buttonVariants> {
  permission: string
  fallbackMode?: 'hide' | 'disable'
  asChild?: boolean
}

export function PermissionButton({ 
  permission, 
  fallbackMode = 'hide',
  children,
  ...props 
}: PermissionButtonProps) {
  const { hasPermission } = usePermission()
  
  if (!hasPermission(permission)) {
    if (fallbackMode === 'hide') return null
    return <Button {...props} disabled>{children}</Button>
  }
  
  return <Button {...props}>{children}</Button>
}

// 使用示例:
// <PermissionButton 
//   permission="admin:user:delete" 
//   fallbackMode="disable"
//   variant="destructive"
// >
//   删除用户
// </PermissionButton>