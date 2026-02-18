import {DotsHorizontalIcon} from '@radix-ui/react-icons'
import {type Row} from '@tanstack/react-table'
import {Trash2, UserPen} from 'lucide-react'
import {Button} from '@/components/ui/button'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuShortcut,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {type User} from '../data/schema'
import {useUsers} from './users-provider'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'

type DataTableRowActionsProps = {
    row: Row<User>
}

export function DataTableRowActions({row}: DataTableRowActionsProps) {
    const {setOpen, setCurrentRow} = useUsers()
    const {hasPermission} = usePermission()

    const canEdit = hasPermission(PERMISSIONS.SYSTEM.USER.EDIT)
    const canDelete = hasPermission(PERMISSIONS.SYSTEM.USER.DELETE)

    // If no permissions, don't render the dropdown
    if (!canEdit && !canDelete) {
        return null
    }

    return (
        <>
            <DropdownMenu modal={false}>
                <DropdownMenuTrigger asChild>
                    <Button
                        variant='ghost'
                        className='data-[state=open]:bg-muted flex h-8 w-8 p-0'
                    >
                        <DotsHorizontalIcon className='h-4 w-4'/>
                        <span className='sr-only'>打开菜单</span>
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align='end' className='w-[160px]'>
                    {canEdit && (
                        <DropdownMenuItem
                            onClick={() => {
                                setCurrentRow(row.original)
                                setOpen('edit')
                            }}
                        >
                            编辑
                            <DropdownMenuShortcut>
                                <UserPen size={16}/>
                            </DropdownMenuShortcut>
                        </DropdownMenuItem>
                    )}
                    {(canEdit) && canDelete && <DropdownMenuSeparator/>}
                    {canDelete && (
                        <DropdownMenuItem
                            onClick={() => {
                                setCurrentRow(row.original)
                                setOpen('delete')
                            }}
                            className='text-red-500!'
                        >
                            删除
                            <DropdownMenuShortcut>
                                <Trash2 size={16}/>
                            </DropdownMenuShortcut>
                        </DropdownMenuItem>
                    )}
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    )
}
