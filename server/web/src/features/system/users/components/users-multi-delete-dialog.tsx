'use client'

import {useState} from 'react'
import {type Table} from '@tanstack/react-table'
import {AlertTriangle} from 'lucide-react'
import {toast} from 'sonner'
import {useDeleteUsers} from '../hooks/use-users'
import {type User} from '../data/schema'
import {Alert, AlertDescription, AlertTitle} from '@/components/ui/alert'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {ConfirmDialog} from '@/components/confirm-dialog'

type UserMultiDeleteDialogProps<TData> = {
    open: boolean
    onOpenChange: (open: boolean) => void
    table: Table<TData>
}

const CONFIRM_WORD = '删除'

export function UsersMultiDeleteDialog<TData>({
                                                  open,
                                                  onOpenChange,
                                                  table,
                                              }: UserMultiDeleteDialogProps<TData>) {
    const [value, setValue] = useState('')
    const deleteUsers = useDeleteUsers()

    const selectedRows = table.getFilteredSelectedRowModel().rows

    const handleDelete = async () => {
        if (value.trim() !== CONFIRM_WORD) {
            toast.error(`请输入 "${CONFIRM_WORD}" 以确认。`)
            return
        }

        try {
            // Extract user IDs from selected rows
            const userIds = selectedRows.map((row) => (row.original as User).id)

            await deleteUsers.mutateAsync(userIds)

            table.resetRowSelection()
            onOpenChange(false)
            setValue('') // Reset input
        } catch (error) {
            // Error handling is done in the hook
            console.error('Error deleting users:', error)
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
            disabled={value.trim() !== CONFIRM_WORD || deleteUsers.isPending}
            confirmText={deleteUsers.isPending ? '删除中...' : '删除'}
            title={
                <span className='text-destructive'>
          <AlertTriangle
              className='stroke-destructive me-1 inline-block'
              size={18}
          />{' '}
                    删除 {selectedRows.length} 个用户
        </span>
            }
            desc={
                <div className='space-y-4'>
                    <p className='mb-2'>
                        您确定要删除所选用户吗？ <br/>
                        此操作不可撤销。
                    </p>

                    <Label className='my-4 flex flex-col items-start gap-1.5'>
                        <span className=''>输入 "{CONFIRM_WORD}" 以确认：</span>
                        <Input
                            value={value}
                            onChange={(e) => setValue(e.target.value)}
                            placeholder={`输入 "${CONFIRM_WORD}" 以确认。`}
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
