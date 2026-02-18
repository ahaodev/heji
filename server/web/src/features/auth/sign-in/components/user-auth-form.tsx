import { useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { Loader2, LogIn } from 'lucide-react'
import { toast } from 'sonner'
import { useAuthStore } from '@/stores/auth-store'
import { cn } from '@/lib/utils'
import { login } from '@/services/authApi'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { PasswordInput } from '@/components/password-input'

const formSchema = z.object({
  username: z
    .string()
    .min(1, '请输入用户名'),
  password: z
    .string()
    .min(1, '请输入密码'),
})

interface UserAuthFormProps extends React.HTMLAttributes<HTMLFormElement> {
  redirectTo?: string
}

export function UserAuthForm({
  className,
  redirectTo,
  ...props
}: UserAuthFormProps) {
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { auth } = useAuthStore()

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  })

  async function onSubmit(data: z.infer<typeof formSchema>) {
    setIsLoading(true)

    // Instrumentation: mark login start to help measure end-to-end time-to-dashboard
    const start = typeof performance !== 'undefined' ? performance.now() : Date.now()
    sessionStorage.setItem('loginStart', String(start))

    // Call backend and measure latency
    const apiCallStart = typeof performance !== 'undefined' ? performance.now() : Date.now()
    try {
      console.log('Login request:', data.username)
      const resp = await login({ username: data.username, password: data.password })
      const apiCallEnd = typeof performance !== 'undefined' ? performance.now() : Date.now()
      console.log('login api latency (ms):', apiCallEnd - apiCallStart)

      if (!resp || resp.code !== 0) {
        toast.error(resp?.msg || '登录失败')
        return
      }

      const payload = resp.data

      // accessToken 存在性检查
      const accessToken = payload?.accessToken
      if (!accessToken) {
        console.error('Login response missing accessToken', payload)
        toast.error('未收到访问令牌，请重试')
        return
      }

      // Helper: decode base64url JWT payload
      function decodeJwt(token: string): any | null {
        try {
          const parts = token.split('.')
          if (parts.length < 2) return null
          const base64Url = parts[1]
          // base64url -> base64
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
          // pad base64 string
          const pad = base64.length % 4
          const padded = base64 + (pad ? '='.repeat(4 - pad) : '')
          const json = decodeURIComponent(
            atob(padded)
              .split('')
              .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
              .join(''),
          )
          return JSON.parse(json)
        } catch (e) {
          console.error('Failed to decode JWT', e)
          return null
        }
      }

      const tokenPayload = decodeJwt(accessToken) || {}

      // Build local user object from token payload (with fallbacks)
      const user = {
        accountNo: tokenPayload.name ?? tokenPayload.username ?? tokenPayload.id ?? '',
        email: '',
        role: [],
        exp: typeof tokenPayload.exp === 'number' ? tokenPayload.exp : Date.now() + 24 * 60 * 60,
      }

      // Persist token for both auth store and axios interceptor (localStorage)
      auth.setUser(user)
      auth.setAccessToken(accessToken)
      try {
        localStorage.setItem('access_token', accessToken)
      } catch (e) {
        // ignore localStorage errors
      }

      // Clear sidebar cache since user has changed
      auth.clearSidebarCache()

      // Fetch complete profile information
      try {
        await auth.fetchProfile()
      } catch (error) {
        console.warn('Failed to fetch profile after login:', error)
        // Continue with login even if profile fetch fails
      }

      // Force reload menu data for the new user
      try {
        const { menuService } = await import('@/services/menu-service')
        await menuService.reloadMenuData()
        console.log('Menu data reloaded for new user')
      } catch (error) {
        console.warn('Failed to reload menu data after login:', error)
      }

      toast.success(`欢迎回来，${tokenPayload.name ?? data.username ?? '用户'}！`)

      const targetPath = redirectTo || '/'
      navigate({ to: targetPath, replace: true })
    } catch (error: any) {
      console.error('Login error', error)
      toast.error(error?.message || '网络错误，请稍后重试')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className={cn('grid gap-3', className)}
        {...props}
      >
        <FormField
          control={form.control}
          name='username'
          render={({ field }) => (
            <FormItem>
              <FormLabel>用户名</FormLabel>
              <FormControl>
                <Input placeholder='用户名或邮箱或手机号' {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name='password'
          render={({ field }) => (
            <FormItem className='relative'>
              <FormLabel>密码</FormLabel>
              <FormControl>
                <PasswordInput placeholder='密码' {...field} />
              </FormControl>
              <FormMessage />
              <Link
                to='/forgot-password'
                className='text-muted-foreground absolute end-0 -top-0.5 text-sm font-medium hover:opacity-75'
              >
                忘记密码?
              </Link>
            </FormItem>
          )}
        />
        <Button className='mt-2' disabled={isLoading}>
          {isLoading ? <Loader2 className='animate-spin' /> : <LogIn />}
          登入
        </Button>
      </form>
    </Form>
  )
}
