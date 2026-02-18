import {useState} from 'react'
import {type Table} from '@tanstack/react-table'
import {Mail, Trash2, UserCheck, UserX} from 'lucide-react'
import {useBulkUpdateUsers} from '../hooks/use-users'
import {Button} from '@/components/ui/button'
import {Tooltip, TooltipContent, TooltipTrigger,} from '@/components/ui/tooltip'
import {DataTableBulkActions as BulkActionsToolbar} from '@/components/data-table'
import {type User} from '../data/schema'
import {UsersMultiDeleteDialog} from './users-multi-delete-dialog'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'

type DataTableBulkActionsProps<TData> = {
    table: Table<TData>
}

export function DataTableBulkActions<TData>({
                                                table,
                                            }: DataTableBulkActionsProps<TData>) {
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
    const selectedRows = table.getFilteredSelectedRowModel().rows
    const bulkUpdateUsers = useBulkUpdateUsers()
    const {hasPermission} = usePermission()

    const canInvite = hasPermission(PERMISSIONS.SYSTEM.USER.INVITE)
    const canEdit = hasPermission(PERMISSIONS.SYSTEM.USER.EDIT)
    const canDelete = hasPermission(PERMISSIONS.SYSTEM.USER.DELETE)

    const handleBulkStatusChange = async (status: 'active' | 'inactive') => {
        const selectedUsers = selectedRows.map((row) => row.original as User)
        const userIds = selectedUsers.map(user => user.id)

        try {
            await bulkUpdateUsers.mutateAsync({userIds, status})
            table.resetRowSelection()
        } catch (error) {
            // Error handling is done in the hook
            console.error('Error updating user status:', error)
        }
    }

    const handleBulkInvite = () => {
        // This would require a bulk invite API endpoint
        // For now, keeping the mock implementation
        const selectedUsers = selectedRows.map((row) => row.original as User)
        console.log('Bulk invite not yet implemented for:', selectedUsers)
        // TODO: Implement bulk invite API
        table.resetRowSelection()
    }

    return (
        <>
            <BulkActionsToolbar table={table} entityName='user'>
                {canInvite && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant='outline'
                                size='icon'
                                onClick={handleBulkInvite}
                                className='size-8'
                                aria-label='邀请选中的用户'
                                title='邀请选中的用户'
                            >
                                <Mail/>
                                <span className='sr-only'>邀请选中的用户</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>邀请选中的用户</p>
                        </TooltipContent>
                    </Tooltip>
                )}

                {canEdit && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant='outline'
                                size='icon'
                                onClick={() => handleBulkStatusChange('active')}
                                className='size-8'
                                disabled={bulkUpdateUsers.isPending}
                                aria-label='激活选中的用户'
                                title='激活选中的用户'
                            >
                                <UserCheck/>
                                <span className='sr-only'>激活选中的用户</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>激活选中的用户</p>
                        </TooltipContent>
                    </Tooltip>
                )}

                {canEdit && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant='outline'
                                size='icon'
                                onClick={() => handleBulkStatusChange('inactive')}
                                className='size-8'
                                disabled={bulkUpdateUsers.isPending}
                                aria-label='停用选中的用户'
                                title='停用选中的用户'
                            >
                                <UserX/>
                                <span className='sr-only'>停用选中的用户</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>停用选中的用户</p>
                        </TooltipContent>
                    </Tooltip>
                )}

                {canDelete && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant='destructive'
                                size='icon'
                                onClick={() => setShowDeleteConfirm(true)}
                                className='size-8'
                                aria-label='删除选中的用户'
                                title='删除选中的用户'
                            >
                                <Trash2/>
                                <span className='sr-only'>删除选中的用户</span>
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>删除选中的用户</p>
                        </TooltipContent>
                    </Tooltip>
                )}
            </BulkActionsToolbar>

            {canDelete && (
                <UsersMultiDeleteDialog
                    table={table}
                    open={showDeleteConfirm}
                    onOpenChange={setShowDeleteConfirm}
                />
            )}
        </>
    )
}
