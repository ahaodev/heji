import {useMemo} from 'react'
import {flexRender, getCoreRowModel, getSortedRowModel, useReactTable} from '@tanstack/react-table'
import {useQuery} from '@tanstack/react-query'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table'
import {Input} from '@/components/ui/input'
import {Loader2} from 'lucide-react'
import {cn} from '@/lib/utils'
import type {Menu} from '@/types/menu'
import {useMenus} from './menus-provider'
import {getMenus} from '@/services/menuApi'
import {buildMenuHierarchy, flattenMenus, type TableMenuItem} from '@/lib/menu-utils'
import {tableMenuItemToMenu} from '../utils/menu-converter'
import {useMenuDrag} from '../hooks/use-menu-drag'
import {useMenuTableState} from '../hooks/use-menu-table-state'
import {createMenuTableColumns} from './menu-table-columns'
import {usePermission} from '@/hooks/usePermission'

interface MenusTableProps {
    onMenuSelect?: (item: Menu) => void
}

export function MenusTable({onMenuSelect}: MenusTableProps) {
    const {setCurrentRow, setShowEditDialog, setShowDeleteDialog, setShowCreateDialog} = useMenus()
    const {hasPermission} = usePermission()
    // Fetch menu data from backend (including buttons)
    const {data: menuData, isLoading, error} = useQuery({
        queryKey: ['menus'],
        queryFn: () => {
            return getMenus({
                status: 'active',
                page_size: 1000 // Get all items to build hierarchy
            })
        },
        staleTime: 5 * 60 * 1000, // 5 minutes
    })

    // Initialize table state hook first
    const {
        expanded,
        sorting,
        setSorting,
        searchTerm,
        setSearchTerm,
        handleExpandToggle
    } = useMenuTableState([])

    // Build hierarchical structure from flat menu data and flatten for table display
    const tableData = useMemo(() => {
        if (!menuData?.list || menuData.list.length === 0) {
            return []
        }

        const hierarchicalMenus = buildMenuHierarchy(menuData.list)
        return flattenMenus(hierarchicalMenus, expanded)
    }, [menuData, expanded])

    // Get filtered data
    const filteredData = useMemo(() => {
        if (!searchTerm) return tableData

        return tableData.filter(item => {
            const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                (item.path || '').toLowerCase().includes(searchTerm.toLowerCase())
            return matchesSearch
        })
    }, [tableData, searchTerm])

    const {
        draggingId,
        dragOverId,
        dragOverPosition,
        handleDragStart,
        handleDragOver,
        handleDragLeave,
        handleDrop,
        handleDragEnd
    } = useMenuDrag(tableData, menuData)

    const handleAddClick = (parentMenu?: TableMenuItem) => {
        setCurrentRow(parentMenu ? tableMenuItemToMenu(parentMenu) : null)
        setShowCreateDialog(true)
    }

    const handleEditClick = (menu: TableMenuItem) => {
        setCurrentRow(tableMenuItemToMenu(menu))
        setShowEditDialog(true)
    }

    const handleDeleteClick = (menu: TableMenuItem) => {
        setCurrentRow(tableMenuItemToMenu(menu))
        setShowDeleteDialog(true)
    }

    const handleRowClick = (menu: TableMenuItem) => {
        onMenuSelect?.(menu)
    }

    // Create table columns
    const columns = createMenuTableColumns({
        expanded,
        onExpandToggle: handleExpandToggle,
        onEditClick: handleEditClick,
        onAddClick: handleAddClick,
        onDeleteClick: handleDeleteClick,
        hasPermission
    })

    const table = useReactTable({
        data: filteredData,
        columns,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        onSortingChange: setSorting,
        state: {
            sorting,
        },
    })

    // Show loading state
    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-32">
                <Loader2 className="h-6 w-6 animate-spin"/>
                <span className="ml-2">加载菜单数据...</span>
            </div>
        )
    }

    // Show error state
    if (error) {
        return (
            <div className="flex items-center justify-center h-32 text-muted-foreground">
                <span>加载菜单失败: {error.message}</span>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            {/* Search bar */}
            <div className="flex items-center justify-between">
                <Input
                    placeholder="搜索菜单名称、编码或路径..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className='h-8 w-[150px] lg:w-[250px]'
                />
            </div>

            {/* Hierarchical table */}
            <div className='overflow-hidden rounded-md border'>
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map((header) => (
                                    <TableHead key={header.id}>
                                        {header.isPlaceholder
                                            ? null
                                            : flexRender(
                                                header.column.columnDef.header,
                                                header.getContext()
                                            )}
                                    </TableHead>
                                ))}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    draggable
                                    onDragStart={(e) => handleDragStart(e, row.original)}
                                    onDragOver={(e) => handleDragOver(e, row.original)}
                                    onDragLeave={(e) => handleDragLeave(e, row.original)}
                                    onDrop={(e) => handleDrop(e, row.original)}
                                    onDragEnd={handleDragEnd}
                                    className={cn(
                                        "cursor-pointer hover:bg-muted/50",
                                        row.original.level === 0 && "bg-muted/20 font-medium",
                                        row.original.level === 1 && "bg-muted/10",
                                        row.original.level >= 2 && "bg-muted/5",
                                        draggingId === row.original.id && "opacity-40",
                                        dragOverId === row.original.id && dragOverPosition === 'above' && 'ring-2 ring-primary/70 ring-offset-0 border-t-4 border-primary',
                                        dragOverId === row.original.id && dragOverPosition === 'below' && 'ring-2 ring-primary/70 ring-offset-0 border-b-4 border-primary'
                                    )}
                                    onClick={() => handleRowClick(row.original)}
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key={cell.id}>
                                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    没有找到匹配的菜单项
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    )
}