import {memo, useEffect, useMemo, useState} from 'react'
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
    type Table as ReactTable,
    type HeaderGroup,
    type Header,
    type Row,
    type Cell,
} from '@tanstack/react-table'
import {cn} from '@/lib/utils'
import {type NavigateFn, useTableUrlState} from '@/hooks/use-table-url-state'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table'
import {DataTablePagination, DataTableToolbar} from '@/components/data-table'
import type {ApiResource} from '@/types/api-resource'
import {apiResourcesColumns as columns} from './api-resources-columns'
import {METHOD_OPTIONS} from '../constants/api-constants'

type DataTableProps = {
    data: ApiResource[]
    search: Record<string, unknown>
    navigate: NavigateFn
    totalCount?: number
    onReset?: () => void
}

// Memoized table content component to prevent unnecessary re-renders
const TableContent = memo(({table}: { table: ReactTable<ApiResource> }) => (
    <div className='overflow-hidden rounded-md border'>
        <Table>
            <TableHeader>
                {table.getHeaderGroups().map((headerGroup: HeaderGroup<ApiResource>) => (
                    <TableRow key={headerGroup.id} className='group/row'>
                        {headerGroup.headers.map((header: Header<ApiResource, unknown>) => {
                            return (
                                <TableHead
                                    key={header.id}
                                    colSpan={header.colSpan}
                                    className={cn(
                                        'bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted',
                                        (header.column.columnDef.meta as { className?: string })?.className ?? ''
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
                    table.getRowModel().rows.map((row: Row<ApiResource>) => (
                        <TableRow
                            key={row.id}
                            data-state={row.getIsSelected() && 'selected'}
                            className='group/row'
                        >
                            {row.getVisibleCells().map((cell: Cell<ApiResource, unknown>) => (
                                <TableCell
                                    key={cell.id}
                                    className={cn(
                                        'bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted',
                                        (cell.column.columnDef.meta as { className?: string })?.className ?? ''
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
))

TableContent.displayName = 'TableContent'

export function ApiResourcesTable({data, search, navigate, totalCount = 0, onReset}: DataTableProps) {
    // Local UI-only states
    const [rowSelection, setRowSelection] = useState({})
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
    const [sorting, setSorting] = useState<SortingState>([])

    // Use data as-is since filtering is done server-side
    const filteredData = data

    // Synced with URL states (keys/defaults mirror api-resources route search schema)
    const {
        columnFilters,
        onColumnFiltersChange,
        pagination,
        onPaginationChange,
        ensurePageInRange,
    } = useTableUrlState({
        search,
        navigate,
        pagination: {defaultPage: 1, defaultPageSize: 10, pageKey: 'page', pageSizeKey: 'page_size'},
        globalFilter: {enabled: false},
        columnFilters: [
            // module per-column filter
            {columnId: 'module', searchKey: 'module', type: 'array'},
            {columnId: 'method', searchKey: 'method', type: 'string'},
            {columnId: 'path', searchKey: 'path', type: 'string'},
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
        // Only run ensurePageInRange if we have data (totalCount > 0)
        // This prevents resetting to page 1 during data loading
        if (totalCount > 0) {
            ensurePageInRange(table.getPageCount())
        }
    }, [table.getPageCount(), ensurePageInRange, totalCount])

    // Get unique modules for filter options (memoized to prevent unnecessary re-renders)
    const moduleOptions = useMemo(() =>
            Array.from(
                new Set(data.map(item => item.module).filter(Boolean))
            ).map(module => ({label: module!, value: module!})),
        [data]
    )

    // Memoize filter configuration to prevent toolbar re-renders
    const filterConfig = useMemo(() => [
        {
            columnId: 'module',
            title: '模块',
            options: moduleOptions,
        },
        {
            columnId: 'method',
            title: '方法',
            options: METHOD_OPTIONS,
            singleSelect: true,
        }
    ], [moduleOptions])

    return (
        <div className='space-y-4 max-sm:has-[div[role="toolbar"]]:mb-16'>
            <DataTableToolbar
                table={table}
                searchPlaceholder='筛选API路径...'
                searchKey='path'
                filters={filterConfig}
                onReset={onReset}
            />
            <TableContent table={table}/>
            <DataTablePagination table={table}/>
        </div>
    )
}