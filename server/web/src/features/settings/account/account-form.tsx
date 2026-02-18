import {z} from 'zod'
import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {Button} from '@/components/ui/button'
import {Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage,} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {updatePassword} from "@/services"
import {toast} from 'sonner'

const accountFormSchema = z.object({
    oldPassword: z
        .string()
        .min(1, '请输入当前密码。'),
    newPassword: z
        .string()
        .min(8, '新密码至少需要8个字符。')
        .max(100, '密码不能超过100个字符。'),
    confirmPassword: z
        .string()
        .min(1, '请确认新密码。'),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: '两次输入的密码不一致。',
    path: ['confirmPassword'],
})

type AccountFormValues = z.infer<typeof accountFormSchema>

const defaultValues: Partial<AccountFormValues> = {}

export function AccountForm() {
    const form = useForm<AccountFormValues>({
        resolver: zodResolver(accountFormSchema),
        defaultValues,
    })

    async function onSubmit(data: AccountFormValues) {
        try {
            // Map form data to API request format
            await updatePassword({
                current_password: data.oldPassword,
                new_password: data.newPassword,
            })

            toast.success('密码修改成功！')

            // Reset form after successful password change
            form.reset()
        } catch (error) {
            const errorMessage = error instanceof Error && 'response' in error
                ? (error as { response?: { data?: { message?: string } } }).response?.data?.message
                : '密码修改失败，请重试。'
            toast.error(errorMessage || '密码修改失败，请重试。')
        }
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-8'>
                <FormField
                    control={form.control}
                    name='oldPassword'
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>当前密码</FormLabel>
                            <FormControl>
                                <Input type='password' placeholder='请输入当前密码' {...field} />
                            </FormControl>
                            <FormDescription>
                                请输入您的当前密码以验证身份。
                            </FormDescription>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name='newPassword'
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>新密码</FormLabel>
                            <FormControl>
                                <Input type='password' placeholder='请输入新密码' {...field} />
                            </FormControl>
                            <FormDescription>
                                新密码至少需要8个字符。
                            </FormDescription>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name='confirmPassword'
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>确认新密码</FormLabel>
                            <FormControl>
                                <Input type='password' placeholder='请再次输入新密码' {...field} />
                            </FormControl>
                            <FormDescription>
                                请再次输入新密码以确认。
                            </FormDescription>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <Button type='submit'>修改密码</Button>
            </form>
        </Form>
    )
}
