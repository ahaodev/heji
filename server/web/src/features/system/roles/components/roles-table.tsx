import {useCallback, useMemo, useState} from 'react'
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query'
import {
    type ColumnDef,
    flexRender,
    getCoreRowModel,
    getFacetedRowModel,
    getFacetedUniqueValues,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    type PaginationState,
    type SortingState,
    useReactTable
} from '@tanstack/react-table'
import {cn} from '@/lib/utils'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table'
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,} from '@/components/ui/dropdown-menu'
import {Button} from '@/components/ui/button'
import {Badge} from '@/components/ui/badge'
import {Input} from '@/components/ui/input'
import {DataTablePagination} from '@/components/data-table'
import {Edit, MoreHorizontal, Trash2} from 'lucide-react'
import {deleteRole, getRolesPaged} from '@/services/roleApi'
import {type Role} from '@/types/role'
import {useRoles} from './roles-provider'
import {toast} from 'sonner'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'

interface RolesTableProps {
    data: any[]
    search: any
    navigate: any
}

export function RolesTable(_props: RolesTableProps) {
    const [pagination, setPagination] = useState<PaginationState>({
        pageIndex: 0,
        pageSize: 10,
    })
    const [sorting, setSorting] = useState<SortingState>([])
    const [searchValue, setSearchValue] = useState('')

    const {setCurrentRow, setShowEditDialog} = useRoles()
    const queryClient = useQueryClient()
    const {hasPermission} = usePermission()

    const canEdit = hasPermission(PERMISSIONS.SYSTEM.ROLE.EDIT)
    const canDelete = hasPermission(PERMISSIONS.SYSTEM.ROLE.DELETE)

    // Fetch roles
    const {data: rolesData, isLoading} = useQuery({
        queryKey: ['roles', pagination.pageIndex + 1, pagination.pageSize, searchValue],
        queryFn: () => getRolesPaged({
            page: pagination.pageIndex + 1,
            page_size: pagination.pageSize,
        }),
    })

    // Filter data based on search
    const filteredData = useMemo(() => {
        return rolesData?.list?.filter((role: Role) => {
            if (!searchValue) return true
            const searchLower = searchValue.toLowerCase()
            return (
                role.name?.toLowerCase().includes(searchLower)
            )
        }) || []
    }, [rolesData?.list, searchValue])

    // Delete role mutation
    const deleteRoleMutation = useMutation({
        mutationFn: deleteRole,
        onSuccess: () => {
            toast.success('角色删除成功')
            queryClient.invalidateQueries({queryKey: ['roles']})
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.msg || '角色删除失败')
        },
    })

    const handleDeleteClick = useCallback((role: Role) => {
        if (window.confirm(`确定要删除角色"${role.name}"吗？此操作不可撤销。`)) {
            deleteRoleMutation.mutate(role.id)
        }
    }, [deleteRoleMutation.mutate])

    const handleEditClick = useCallback((role: Role) => {
        setCurrentRow(role)
        setShowEditDialog(true)
    }, [setCurrentRow, setShowEditDialog])

    const getStatusBadgeVariant = useCallback((status: string) => {
        switch (status) {
            case 'active':
                return 'default'
            case 'inactive':
                return 'secondary'
            default:
                return 'secondary'
        }
    }, [])

    const columns = useMemo<ColumnDef<Role, any>[]>(() => [
        {
            accessorKey: 'name',
            header: '角色名称',
            cell: ({row}) => (
                <div className="font-medium">{row.getValue('name')}</div>
            ),
        },
        {
            accessorKey: 'status',
            header: '状态',
            cell: ({row}) => {
                const status = row.getValue('status') as string
                return (
                    <Badge variant={getStatusBadgeVariant(status)}>
                        {status === 'active' ? '正常' : '停用'}
                    </Badge>
                )
            },
        },
        {
            accessorKey: 'sequence',
            header: '显示顺序',
            cell: ({row}) => (
                <div className="text-center">{row.getValue('sequence')}</div>
            ),
        },
        {
            id: 'actions',
            header: '操作',
            cell: ({row}) => {
                const role = row.original

                // If no permissions, don't render the dropdown
                if (!canEdit && !canDelete) {
                    return null
                }

                return (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                                <MoreHorizontal className="h-4 w-4"/>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            {canEdit && (
                                <DropdownMenuItem onClick={() => handleEditClick(role)}>
                                    <Edit className="mr-2 h-4 w-4"/>
                                    编辑
                                </DropdownMenuItem>
                            )}
                            {canDelete && (
                                <DropdownMenuItem
                                    onClick={() => handleDeleteClick(role)}
                                    className="text-destructive"
                                >
                                    <Trash2 className="mr-2 h-4 w-4"/>
                                    删除
                                </DropdownMenuItem>
                            )}
                        </DropdownMenuContent>
                    </DropdownMenu>
                )
            },
        },
    ], [getStatusBadgeVariant, handleEditClick, handleDeleteClick, canEdit, canDelete])

    const table = useReactTable({
        data: filteredData,
        columns,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getFacetedRowModel: getFacetedRowModel(),
        getFacetedUniqueValues: getFacetedUniqueValues(),
        onSortingChange: setSorting,
        onPaginationChange: setPagination,
        manualPagination: false,
        pageCount: Math.ceil(filteredData.length / pagination.pageSize),
        state: {
            sorting,
            pagination,
        },
    })

    return (
        <div className='space-y-4'>
            {/* Search Filter */}
            <div className="flex items-center justify-between">
                <Input
                    placeholder="搜索角色..."
                    value={searchValue}
                    onChange={(e) => setSearchValue(e.target.value)}
                    className='h-8 w-[150px] lg:w-[250px]'
                />
                <div className="text-sm text-muted-foreground">
                    共 {filteredData.length} 个角色
                </div>
            </div>

            {/* Table */}
            <div className='overflow-hidden rounded-md border'>
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id} className='group/row'>
                                {headerGroup.headers.map((header) => {
                                    return (
                                        <TableHead
                                            key={header.id}
                                            colSpan={header.colSpan}
                                            className={cn(
                                                'bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted',
                                                header.column.columnDef.meta?.className ?? ''
                                            )}
                                        >
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                        </TableHead>
                                    )
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    加载中...
                                </TableCell>
                            </TableRow>
                        ) : table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={row.getIsSelected() && "selected"}
                                    className='group/row'
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell
                                            key={cell.id}
                                            className={cn(
                                                'bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted',
                                                cell.column.columnDef.meta?.className ?? ''
                                            )}
                                        >
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className='h-24 text-center'
                                >
                                    没有数据。
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>

            {/* Pagination */}
            <DataTablePagination table={table}/>
        </div>
    )
}