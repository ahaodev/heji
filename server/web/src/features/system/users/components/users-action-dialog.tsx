'use client'

import {Button} from '@/components/ui/button'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {Form} from '@/components/ui/form'
import {type User} from '../data/schema'
import {useUserForm} from '../hooks/use-user-form'
import {UserFormFields} from './user-form-fields'


type UserActionDialogProps = {
    currentRow?: User
    open: boolean
    onOpenChange: (open: boolean) => void
}

export function UsersActionDialog({
                                      currentRow,
                                      open,
                                      onOpenChange,
                                  }: UserActionDialogProps) {
    const {form, onSubmit, isEdit, isPasswordTouched, isSubmitting, allRoles} = useUserForm({
        currentRow,
        onSuccess: () => onOpenChange(false)
    })

    return (
        <Dialog
            open={open}
            onOpenChange={(state) => {
                form.reset()
                onOpenChange(state)
            }}
        >
            <DialogContent className='sm:max-w-lg'>
                <DialogHeader className='text-start'>
                    <DialogTitle>{isEdit ? '编辑用户' : '添加新用户'}</DialogTitle>
                    <DialogDescription>
                        {isEdit ? '在此更新用户。' : '在此创建新用户。'}
                        完成后点击保存。
                    </DialogDescription>
                </DialogHeader>
                <div className='h-[26.25rem] w-[calc(100%+0.75rem)] overflow-y-auto py-1 pe-3'>
                    <Form {...form}>
                        <form
                            id='user-form'
                            onSubmit={form.handleSubmit(onSubmit)}
                            className='space-y-4 px-0.5'
                        >
                            <UserFormFields
                                form={form}
                                isEdit={isEdit}
                                isPasswordTouched={isPasswordTouched}
                                allRoles={allRoles}
                            />
                        </form>
                    </Form>
                </div>
                <DialogFooter>
                    <Button
                        type='submit'
                        form='user-form'
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '保存中...' : '保存更改'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}
