import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect } from 'react'
import { showSubmittedData } from '@/lib/show-submitted-data'
import { useAuthStore } from '@/stores/auth-store'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'

const profileFormSchema = z.object({
  username: z
    .string('请输入您的用户名。')
    .min(2, '用户名最少必须包含2个字符。')
    .max(30, '用户名不能超过30个字符。'),
  email: z.string().email('请输入有效的邮箱地址。'),
  bio: z.string().max(160).min(4),
})

type ProfileFormValues = z.infer<typeof profileFormSchema>

const defaultValues: Partial<ProfileFormValues> = {
  username: '',
  email: 'admin@example.com',
  bio: '',
}

export function ProfileForm() {
  const { profile, fetchProfile, isLoadingProfile } = useAuthStore((state) => state.auth)
  
  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileFormSchema),
    defaultValues,
    mode: 'onChange',
  })

  // Fetch profile data and populate form when component loads
  useEffect(() => {
    fetchProfile()
  }, [fetchProfile])

  // Update form values when profile data is available
  useEffect(() => {
    if (profile) {
      form.reset({
        username: profile.username || '',
        email: profile.email || '',
        bio: '', // Keep bio empty as it's not part of the Profile type
      })
    }
  }, [profile, form])

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((data) => showSubmittedData(data))}
        className='space-y-8'
      >
        <FormField
          control={form.control}
          name='username'
          render={({ field }) => (
            <FormItem>
              <FormLabel>用户名</FormLabel>
              <FormControl>
                <Input 
                  placeholder='请输入用户名' 
                  disabled
                  {...field} 
                />
              </FormControl>
              <FormDescription>
                这是您的公开显示名称。
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name='email'
          render={({ field }) => (
            <FormItem>
              <FormLabel>邮箱</FormLabel>
              <FormControl>
                <Input 
                  type='email' 
                  placeholder='请输入邮箱地址' 
                  disabled
                  {...field} 
                />
              </FormControl>
              <FormDescription>
                当前账户绑定的邮箱地址。
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name='bio'
          render={({ field }) => (
            <FormItem>
              <FormLabel>个人简介</FormLabel>
              <FormControl>
                <Textarea
                  placeholder='简单介绍一下您自己'
                  className='resize-none'
                  {...field}
                />
              </FormControl>
              <FormDescription>
                个人简介最多160个字符。
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <Button type='submit' disabled={isLoadingProfile}>
          {isLoadingProfile ? '加载中...' : '更新个人资料'}
        </Button>
      </form>
    </Form>
  )
}
