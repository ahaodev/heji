import type {ColumnDef} from '@tanstack/react-table'
import {Button} from '@/components/ui/button'
import {Badge} from '@/components/ui/badge'
import {Checkbox} from '@/components/ui/checkbox'
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from '@/components/ui/dropdown-menu'
import {DataTableColumnHeader} from '@/components/data-table'
import {Edit, List, MoreHorizontal, Trash2} from 'lucide-react'
import {type DictType} from '../data/schema'
import {useDicts} from './dicts-provider'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'
import {formatDistanceToNow} from 'date-fns'
import {zhCN} from 'date-fns/locale'

// Extract actions cell to a proper React component to follow Hooks rules
function ActionsCell({dictType}: { dictType: DictType }) {
    const {
        setCurrentTypeRow,
        setShowTypeEditDialog,
        setShowTypeDeleteDialog,
        setSelectedType,
        setShowItemsListDialog,
    } = useDicts()
    const {hasPermission} = usePermission()
    const canEdit = hasPermission(PERMISSIONS.SYSTEM.DICT.EDIT_TYPE)
    const canDelete = hasPermission(PERMISSIONS.SYSTEM.DICT.DELETE_TYPE)

    const handleEdit = () => {
        setCurrentTypeRow(dictType)
        setShowTypeEditDialog(true)
    }
    const handleDelete = () => {
        setCurrentTypeRow(dictType)
        setShowTypeDeleteDialog(true)
    }
    const handleViewItems = () => {
        setSelectedType(dictType)
        setShowItemsListDialog(true)
    }

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-8 w-8 p-0">
                    <span className="sr-only">Open menu</span>
                    <MoreHorizontal className="h-4 w-4"/>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={handleViewItems}>
                    <List className="mr-2 h-4 w-4"/> 查看字典项
                </DropdownMenuItem>
                {canEdit && (
                    <DropdownMenuItem onClick={handleEdit}>
                        <Edit className="mr-2 h-4 w-4"/> 编辑
                    </DropdownMenuItem>
                )}
                {canDelete && (
                    <DropdownMenuItem onClick={handleDelete} className="text-destructive">
                        <Trash2 className="mr-2 h-4 w-4"/> 删除
                    </DropdownMenuItem>
                )}
            </DropdownMenuContent>
        </DropdownMenu>
    )
}

export const dictTypesColumns: ColumnDef<DictType>[] = [
    {
        id: 'select',
        header: ({table}) => (
            <Checkbox
                checked={
                    table.getIsAllPageRowsSelected() ||
                    (table.getIsSomePageRowsSelected() && 'indeterminate')
                }
                onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                aria-label="Select all"
                className="translate-y-[2px]"
            />
        ),
        cell: ({row}) => (
            <Checkbox
                checked={row.getIsSelected()}
                onCheckedChange={(value) => row.toggleSelected(!!value)}
                aria-label="Select row"
                className="translate-y-[2px]"
            />
        ),
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: 'code',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title="类型编码"/>
        ),
        cell: ({row}) => (
            <div className="font-mono text-sm font-medium">{row.getValue('code')}</div>
        ),
    },
    {
        accessorKey: 'name',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title="类型名称"/>
        ),
        cell: ({row}) => (
            <div className="font-medium">{row.getValue('name')}</div>
        ),
    },
    {
        accessorKey: 'status',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title="状态"/>
        ),
        cell: ({row}) => {
            const status = row.getValue('status') as string
            return (
                <Badge variant={status === 'active' ? 'default' : 'secondary'}>
                    {status === 'active' ? '启用' : '禁用'}
                </Badge>
            )
        },
        filterFn: (row, id, value) => {
            return value.includes(row.getValue(id))
        },
    },
    {
        accessorKey: 'remark',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title="备注"/>
        ),
        cell: ({row}) => {
            const remark = row.getValue('remark') as string
            return <div className="max-w-[200px] truncate">{remark || '-'}</div>
        },
    },
    {
        accessorKey: 'created_at',
        header: ({column}) => (
            <DataTableColumnHeader column={column} title="创建时间"/>
        ),
        cell: ({row}) => {
            const date = row.getValue('created_at') as Date
            return (
                <div className="text-sm text-muted-foreground">
                    {formatDistanceToNow(date, {addSuffix: true, locale: zhCN})}
                </div>
            )
        },
    },
    {
        id: 'actions',
        header: '操作',
        cell: ({row}) => <ActionsCell dictType={row.original}/>,
    },
]
