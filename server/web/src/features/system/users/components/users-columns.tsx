import {type ColumnDef} from '@tanstack/react-table'
import {cn} from '@/lib/utils'
import {Badge} from '@/components/ui/badge'
import {Checkbox} from '@/components/ui/checkbox'
import {DataTableColumnHeader} from '@/components/data-table'
import {LongText} from '@/components/long-text'
import {type User} from '../data/schema'
import {DataTableRowActions} from './data-table-row-actions'

export const usersColumns: ColumnDef<User>[] = [
    {
        id: 'select',
        header: ({table}) => (
            <Checkbox
                checked={
                    table.getIsAllPageRowsSelected() ||
                    (table.getIsSomePageRowsSelected() && 'indeterminate')
                }
                onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                aria-label='Select all'
                className='translate-y-[2px]'
            />
        ),
        meta: {
            className: cn('sticky md:table-cell start-0 z-10 rounded-tl-[inherit]'),
        },
        cell: ({row}) => (
            <Checkbox
                checked={row.getIsSelected()}
                onCheckedChange={(value) => row.toggleSelected(!!value)}
                aria-label='Select row'
                className='translate-y-[2px]'
            />
        ),
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: 'username',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title='用户名'/>
        ),
        cell: ({row}) => (
            <LongText className='max-w-36 ps-3'>{row.getValue('username')}</LongText>
        ),
        meta: {
            className: cn(
                'drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]',
                'sticky start-6 @4xl/content:table-cell @4xl/content:drop-shadow-none'
            ),
        },
        enableHiding: false,
    },
    {
        accessorKey: 'email',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title='邮箱'/>
        ),
        cell: ({row}) => (
            <div className='w-fit text-nowrap'>{row.getValue('email')}</div>
        ),
    },
    {
        accessorKey: 'phone',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title='手机号码'/>
        ),
        cell: ({row}) => <div>{row.getValue('phone') || '-'}</div>,
        enableSorting: false,
    },
    {
        accessorKey: 'roles',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title='角色'/>
        ),
        cell: ({row}) => {
            const roles = row.getValue('roles') as string[] | undefined
            if (!roles || roles.length === 0) {
                return <div className='text-muted-foreground'>-</div>
            }
            return (
                <div className='flex flex-wrap gap-1'>
                    {roles.map((role, index) => (
                        <Badge key={index} variant='secondary' className='text-xs'>
                            {role}
                        </Badge>
                    ))}
                </div>
            )
        },
        enableSorting: false,
    },
    {
        accessorKey: 'status',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title='状态'/>
        ),
        cell: ({row}) => {
            const status = row.getValue('status') as string
            const getStatusColor = (status: string) => {
                switch (status) {
                    case 'active':
                        return 'bg-green-100 text-green-800 border-green-200'
                    case 'inactive':
                        return 'bg-gray-100 text-gray-800 border-gray-200'
                    case 'invited':
                        return 'bg-blue-100 text-blue-800 border-blue-200'
                    case 'suspended':
                        return 'bg-red-100 text-red-800 border-red-200'
                    default:
                        return 'bg-gray-100 text-gray-800 border-gray-200'
                }
            }
            return (
                <div className='flex space-x-2'>
                    <Badge variant='outline' className={cn('capitalize', getStatusColor(status))}>
                        {status}
                    </Badge>
                </div>
            )
        },
        filterFn: (row, id, value) => {
            return value.includes(row.getValue(id))
        },
        enableHiding: false,
        enableSorting: false,
    },
    {
        id: 'actions',
        cell: DataTableRowActions,
    },
]
