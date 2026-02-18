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
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@/components/ui/table'
import {DataTablePagination, DataTableToolbar} from '@/components/data-table'
import {type DictType} from '../data/schema'
import {DataTableBulkActions} from './data-table-bulk-actions'
import {dictTypesColumns as columns} from './dict-types-columns'

type DataTableProps = {
    data: DictType[]
    search: Record<string, unknown>
    navigate: NavigateFn
    totalCount?: number
}

export function DictTypesTable({data, search, navigate, totalCount = 0}: DataTableProps) {
    // Local UI-only states
    const [rowSelection, setRowSelection] = useState({})
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
    const [sorting, setSorting] = useState<SortingState>([])

    // Use data as-is since filtering is done server-side
    const filteredData = data

    // Synced with URL states (keys/defaults mirror dict route search schema)
    const {
        columnFilters,
        onColumnFiltersChange,
        pagination,
        onPaginationChange,
        ensurePageInRange,
    } = useTableUrlState({
        search,
        navigate,
        pagination: {
            pageKey: 'page',
            pageSizeKey: 'page_size',
            defaultPage: 1,
            defaultPageSize: 10
        },
        columnFilters: [
            {columnId: 'status', searchKey: 'status', type: 'string'}
        ]
    })

    // Ensure page is within valid range when data changes
    useEffect(() => {
        ensurePageInRange(totalCount)
    }, [totalCount, ensurePageInRange])

    const table = useReactTable({
        data: filteredData,
        columns,
        state: {
            sorting,
            columnVisibility,
            rowSelection,
            columnFilters,
            pagination,
        },
        pageCount: Math.ceil(totalCount / pagination.pageSize),
        manualPagination: true,
        enableRowSelection: true,
        onRowSelectionChange: setRowSelection,
        onSortingChange: setSorting,
        onColumnFiltersChange,
        onColumnVisibilityChange: setColumnVisibility,
        onPaginationChange,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFacetedRowModel: getFacetedRowModel(),
        getFacetedUniqueValues: getFacetedUniqueValues(),
    })

    const selectedRowIds = Object.keys(rowSelection).filter(key => (rowSelection as Record<string, boolean>)[key])
    const selectedDictTypes = selectedRowIds.map(id => filteredData[parseInt(id)])

    return (
        <div className='space-y-4'>
            <DataTableToolbar
                table={table}
                searchPlaceholder="搜索字典类型..."
                filters={[
                    {
                        columnId: 'status',
                        title: '状态',
                        options: [
                            {label: '启用', value: 'active'},
                            {label: '禁用', value: 'inactive'},
                        ],
                    },
                ]}
            />
            {selectedDictTypes.length > 0 && (
                <DataTableBulkActions selectedDictTypes={selectedDictTypes}/>
            )}
            <div className='rounded-md border'>
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map((header) => {
                                    return (
                                        <TableHead key={header.id} colSpan={header.colSpan}>
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
                                    className={cn(
                                        'cursor-pointer',
                                        row.getIsSelected() && 'bg-muted/50'
                                    )}
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key={cell.id}>
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
                                    暂无字典类型
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <DataTablePagination table={table}/>
        </div>
    )
}