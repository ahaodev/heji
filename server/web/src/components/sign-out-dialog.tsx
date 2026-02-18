import { useNavigate } from '@tanstack/react-router'
import { useAuthStore } from '@/stores/auth-store'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { logout } from '@/services/authApi'
import { removeCookie } from '@/lib/cookies'
import { ACCESS_TOKEN, REFRESH_TOKEN } from '@/types/constants'

interface SignOutDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function SignOutDialog({ open, onOpenChange }: SignOutDialogProps) {
  const navigate = useNavigate()
  const { auth } = useAuthStore()

  const handleSignOut = async () => {
    try {
      // 调用后端登出API
      await logout()
      console.log('Logout API called successfully')
    } catch (error) {
      // 即使API调用失败，也要继续清理本地状态
      console.warn('Logout API failed, but continuing with local cleanup:', error)
    } finally {
      // 清理认证状态和所有存储（auth.reset()已包含clearSidebarCache）
      auth.reset()
      
      // 额外清理可能残留的token相关数据
      removeCookie(ACCESS_TOKEN)
      removeCookie(REFRESH_TOKEN)
      localStorage.removeItem(ACCESS_TOKEN)
      localStorage.removeItem(REFRESH_TOKEN)
      
      // 清理其他可能的用户相关本地存储
      localStorage.removeItem('userProfile')
      localStorage.removeItem('userPermissions')
      localStorage.removeItem('lastLoginTime')
      
      // After logout, redirect to sign-in without preserving current location
      // User should go to root directory after login, not back to the previous page
      navigate({
        to: '/sign-in',
        replace: true,
      })
    }
  }

  return (
    <ConfirmDialog
      open={open}
      onOpenChange={onOpenChange}
      title='登出'
      desc='确定要登出吗？登出后需要重新登录才能访问您的账户'
      confirmText='登出'
      handleConfirm={handleSignOut}
      className='sm:max-w-sm'
    />
  )
}
