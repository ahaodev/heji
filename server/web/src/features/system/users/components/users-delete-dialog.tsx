'use client'

import {useState} from 'react'
import {AlertTriangle} from 'lucide-react'
import {useDeleteUser} from '../hooks/use-users'
import {Alert, AlertDescription, AlertTitle} from '@/components/ui/alert'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {ConfirmDialog} from '@/components/confirm-dialog'
import {type User} from '../data/schema'

type UserDeleteDialogProps = {
    open: boolean
    onOpenChange: (open: boolean) => void
    currentRow: User
}

export function UsersDeleteDialog({
                                      open,
                                      onOpenChange,
                                      currentRow,
                                  }: UserDeleteDialogProps) {
    const [value, setValue] = useState('')
    const deleteUser = useDeleteUser()

    const handleDelete = async () => {
        if (value.trim() !== currentRow.username) return

        try {
            await deleteUser.mutateAsync(currentRow.id)
            onOpenChange(false)
            setValue('') // Reset input
        } catch (error) {
            // Error handling is done in the hook
            console.error('Error deleting user:', error)
        }
    }

    return (
        <ConfirmDialog
            open={open}
            onOpenChange={(open) => {
                onOpenChange(open)
                if (!open) setValue('') // Reset input when dialog closes
            }}
            handleConfirm={handleDelete}
            disabled={value.trim() !== currentRow.username || deleteUser.isPending}
            confirmText={deleteUser.isPending ? '删除中...' : '删除'}
            title={
                <span className='text-destructive'>
          <AlertTriangle
              className='stroke-destructive me-1 inline-block'
              size={18}
          />{' '}
                    删除用户
        </span>
            }
            desc={
                <div className='space-y-4'>
                    <p className='mb-2'>
                        您确定要删除用户{' '}
                        <span className='font-bold'>{currentRow.username}</span> 吗？
                        <br/>
                        此操作将永久从系统中删除用户 <span className='font-bold'>{currentRow.username}</span>。此操作不可撤销。
                    </p>

                    <Label className='my-2'>
                        用户名：
                        <Input
                            value={value}
                            onChange={(e) => setValue(e.target.value)}
                            placeholder='输入用户名以确认删除。'
                        />
                    </Label>

                    <Alert variant='destructive'>
                        <AlertTitle>警告！</AlertTitle>
                        <AlertDescription>
                            请谨慎操作，此操作不可回滚。
                        </AlertDescription>
                    </Alert>
                </div>
            }
            destructive
        />
    )
}
