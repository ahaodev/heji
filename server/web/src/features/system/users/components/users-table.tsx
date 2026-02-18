import {useEffect, useState} from 'react'
import {
    flexRender,
    getCoreRowModel,
    getFacetedRowModel,
    getFacetedUniqueValues,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    type SortingState,
    useReactTable,
    type VisibilityState,
} from '@tanstack/react-table'
import {cn} from '@/lib/utils'
import {type NavigateFn, useTableUrlState} from '@/hooks/use-table-url-state'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table'
import {DataTablePagination, DataTableToolbar} from '@/components/data-table'
import {type User} from '../data/schema'
import {DataTableBulkActions} from './data-table-bulk-actions'
import {usersColumns as columns} from './users-columns'

type DataTableProps = {
    data: User[]
    search: Record<string, unknown>
    navigate: NavigateFn
    totalCount?: number
}

export function UsersTable({data, search, navigate, totalCount = 0}: DataTableProps) {
    // Local UI-only states
    const [rowSelection, setRowSelection] = useState({})
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
    const [sorting, setSorting] = useState<SortingState>([])

    // Use data as-is since filtering is done server-side
    const filteredData = data

    // Local state management for table (uncomment to use local-only state, not synced with URL)
    // const [columnFilters, onColumnFiltersChange] = useState<ColumnFiltersState>([])
    // const [pagination, onPaginationChange] = useState<PaginationState>({ pageIndex: 0, pageSize: 10 })

    // Synced with URL states (keys/defaults mirror users route search schema)
    const {
        columnFilters,
        onColumnFiltersChange,
        pagination,
        onPaginationChange,
        ensurePageInRange,
    } = useTableUrlState({
        search,
        navigate,
        pagination: {defaultPage: 1, defaultPageSize: 10},
        globalFilter: {enabled: false},
        columnFilters: [
            // username per-column text filter
            {columnId: 'username', searchKey: 'username', type: 'string'},
            {columnId: 'status', searchKey: 'status', type: 'array'},
        ],
    })

    const table = useReactTable({
        data: filteredData,
        columns,
        state: {
            sorting,
            pagination,
            rowSelection,
            columnFilters,
            columnVisibility,
        },
        enableRowSelection: true,
        manualPagination: true, // Enable server-side pagination
        manualFiltering: true,  // Enable server-side filtering
        pageCount: Math.ceil(totalCount / (pagination.pageSize || 10)), // Calculate total pages
        onPaginationChange,
        onColumnFiltersChange,
        onRowSelectionChange: setRowSelection,
        onSortingChange: setSorting,
        onColumnVisibilityChange: setColumnVisibility,
        getPaginationRowModel: getPaginationRowModel(),
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFacetedRowModel: getFacetedRowModel(),
        getFacetedUniqueValues: getFacetedUniqueValues(),
    })

    useEffect(() => {
        ensurePageInRange(table.getPageCount())
    }, [table, ensurePageInRange])

    return (
        <div className='space-y-4 max-sm:has-[div[role="toolbar"]]:mb-16'>
            <DataTableToolbar
                table={table}
                searchPlaceholder='筛选用户...'
                searchKey='username'
                filters={[
                    {
                        columnId: 'status',
                        title: '状态',
                        options: [
                            {label: '激活', value: 'active'},
                            {label: '停用', value: 'inactive'},
                            {label: '已邀请', value: 'invited'},
                            {label: '暂停', value: 'suspended'},
                        ],
                    },
                ]}
            />
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
                        {table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={row.getIsSelected() && 'selected'}
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
                                    没有结果。
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <DataTablePagination table={table}/>
            <DataTableBulkActions table={table}/>
        </div>
    )
}
