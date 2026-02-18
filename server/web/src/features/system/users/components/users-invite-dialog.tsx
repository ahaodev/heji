import {z} from 'zod'
import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {MailPlus, Send} from 'lucide-react'
import {useQuery} from '@tanstack/react-query'
import {useInviteUser} from '../hooks/use-users'
import {getRoles} from '@/services/roleApi'
import {Button} from '@/components/ui/button'
import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage,} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {Textarea} from '@/components/ui/textarea'
import {SelectDropdown} from '@/components/select-dropdown'

const formSchema = z.object({
    email: z.email({
        error: (iss) =>
            iss.input === '' ? '请输入要邀请的邮箱。' : undefined,
    }),
    role: z.string().min(1, '角色为必填项。'),
    message: z.string().optional(),
})

type UserInviteForm = z.infer<typeof formSchema>

type UserInviteDialogProps = {
    open: boolean
    onOpenChange: (open: boolean) => void
}

export function UsersInviteDialog({
                                      open,
                                      onOpenChange,
                                  }: UserInviteDialogProps) {
    const inviteUser = useInviteUser()

    // Fetch roles from API
    const {data: roles = [], isLoading: rolesLoading} = useQuery({
        queryKey: ['roles'],
        queryFn: getRoles,
        enabled: open,
    })

    const form = useForm<UserInviteForm>({
        resolver: zodResolver(formSchema),
        defaultValues: {email: '', role: '', message: ''},
    })

    // Convert roles data to options format for SelectDropdown
    const roleOptions = roles.map(role => ({
        label: role.name,
        value: role.id,
    }))

    const onSubmit = async (values: UserInviteForm) => {
        try {
            await inviteUser.mutateAsync({
                email: values.email,
                role_ids: [values.role],
                message: values.message,
            })

            form.reset()
            onOpenChange(false)
        } catch (error) {
            // Error handling is done in the hook
            console.error('Error inviting user:', error)
        }
    }

    return (
        <Dialog
            open={open}
            onOpenChange={(state) => {
                form.reset()
                onOpenChange(state)
            }}
        >
            <DialogContent className='sm:max-w-md'>
                <DialogHeader className='text-start'>
                    <DialogTitle className='flex items-center gap-2'>
                        <MailPlus/> 邀请用户
                    </DialogTitle>
                    <DialogDescription>
                        邀请新用户加入系统并担任指定角色。
                        他们将收到一封邮件邀请来设置其账户。
                    </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                    <form
                        id='user-invite-form'
                        onSubmit={form.handleSubmit(onSubmit)}
                        className='space-y-4'
                    >
                        <FormField
                            control={form.control}
                            name='email'
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>邮箱</FormLabel>
                                    <FormControl>
                                        <Input
                                            type='email'
                                            placeholder='例如：john.doe@gmail.com'
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name='role'
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>角色</FormLabel>
                                    <SelectDropdown
                                        defaultValue={field.value}
                                        onValueChange={field.onChange}
                                        placeholder={rolesLoading ? '加载角色中...' : '选择角色'}
                                        items={roleOptions}
                                        disabled={rolesLoading}
                                    />
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name='message'
                            render={({field}) => (
                                <FormItem className=''>
                                    <FormLabel>消息（可选）</FormLabel>
                                    <FormControl>
                                        <Textarea
                                            className='resize-none'
                                            placeholder='在您的邀请中添加个人留言（可选）'
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage/>
                                </FormItem>
                            )}
                        />
                    </form>
                </Form>
                <DialogFooter className='gap-y-2'>
                    <DialogClose asChild>
                        <Button variant='outline'>取消</Button>
                    </DialogClose>
                    <Button
                        type='submit'
                        form='user-invite-form'
                        disabled={inviteUser.isPending || rolesLoading}
                    >
                        {inviteUser.isPending ? '邀请中...' : '邀请'} <Send/>
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}
