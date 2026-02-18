import {type ColumnDef} from '@tanstack/react-table'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import {ChevronDown, ChevronRight, Edit, Plus, Settings, Trash2} from 'lucide-react'
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from '@/components/ui/dropdown-menu'
import {cn} from '@/lib/utils'
import type {TableMenuItem} from '@/lib/menu-utils'
import {getIconByName} from '@/lib/icons'
import {PERMISSIONS} from '@/constants/permissions'
import DOMPurify from 'dompurify'

interface MenuTableColumnsProps {
    expanded: Record<string, boolean>
    onExpandToggle: (hierarchyIndex: string) => void
    onEditClick: (menu: TableMenuItem) => void
    onAddClick: (menu: TableMenuItem) => void
    onDeleteClick: (menu: TableMenuItem) => void
    hasPermission: (permission: string) => boolean
}

export function createMenuTableColumns({
                                           expanded,
                                           onExpandToggle,
                                           onEditClick,
                                           onAddClick,
                                           onDeleteClick,
                                           hasPermission
                                       }: MenuTableColumnsProps): ColumnDef<TableMenuItem, unknown>[] {
    return [
        {
            accessorKey: 'name',
            header: '菜单名称',
            cell: ({row}) => {
                const menu = row.original
                const hasChildren = menu.children && menu.children.length > 0
                const hierarchyIndex = menu.hierarchyIndex

                return (
                    <div className="flex items-center gap-2" style={{paddingLeft: `${menu.level * 24}px`}}>
                        {hasChildren ? (
                            <Button
                                variant="ghost"
                                size="sm"
                                className="h-6 w-6 p-0"
                                onClick={(e) => {
                                    e.stopPropagation()
                                    onExpandToggle(hierarchyIndex)
                                }}
                            >
                                {expanded[hierarchyIndex] ? (
                                    <ChevronDown className="h-4 w-4"/>
                                ) : (
                                    <ChevronRight className="h-4 w-4"/>
                                )}
                            </Button>
                        ) : (
                            <div className="w-6"/>
                        )}

                        <div className="flex items-center gap-2">
                            {menu.icon && (() => {
                                const IconComponent = getIconByName(menu.icon)
                                if (IconComponent) {
                                    return <IconComponent className="h-4 w-4 flex-shrink-0 text-muted-foreground"/>
                                }
                                return <span className="flex-shrink-0 text-muted-foreground"
                                             dangerouslySetInnerHTML={{__html: DOMPurify.sanitize(menu.icon)}}/>
                            })()}
                            <span className={cn(
                                "font-medium",
                                menu.level === 0 && "font-semibold",
                                menu.status === 'inactive' && "line-through opacity-50"
                            )}>
                                {menu.name}
                            </span>
                        </div>
                    </div>
                )
            },
        },
        {
            accessorKey: 'type',
            header: '类型',
            cell: ({row}) => {
                const type = row.original.type
                const typeLabels = {
                    'menu': '菜单',
                    'button': '按钮'
                }
                return (
                    <Badge variant="secondary" className="text-xs">
                        {typeLabels[type as keyof typeof typeLabels] || type}
                    </Badge>
                )
            },
        },
        {
            accessorKey: 'sequence',
            header: '排序',
            cell: ({row}) => {
                const sequence = row.original.sequence
                return (
                    <div className="text-sm">{sequence}</div>
                )
            },
        },
        {
            accessorKey: 'path',
            header: '路径',
            cell: ({row}) => {
                const path = row.original.path
                return path ? (
                    <Badge variant="outline" className="text-xs">
                        {path}
                    </Badge>
                ) : (
                    <span className="text-muted-foreground">-</span>
                )
            },
        },
        {
            accessorKey: 'status',
            header: '状态',
            cell: ({row}) => {
                const status = row.original.status
                return (
                    <Badge variant={status === 'active' ? 'default' : 'secondary'}>
                        {status === 'active' ? '正常' : '停用'}
                    </Badge>
                )
            },
        },
        {
            id: 'actions',
            header: '操作',
            cell: ({row}) => {
                const menu = row.original

                // Check if user has any permissions for these actions
                const canEdit = hasPermission(PERMISSIONS.SYSTEM.MENU.EDIT)
                const canAdd = hasPermission(PERMISSIONS.SYSTEM.MENU.ADD)
                const canDelete = hasPermission(PERMISSIONS.SYSTEM.MENU.DELETE)

                // If no permissions, don't show dropdown
                if (!canEdit && !canAdd && !canDelete) {
                    return null
                }

                return (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                                <span className="sr-only">打开菜单</span>
                                <Settings className="h-4 w-4"/>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            {canEdit && (
                                <DropdownMenuItem onClick={() => onEditClick(menu)}>
                                    <Edit className="mr-2 h-4 w-4"/>
                                    修改
                                </DropdownMenuItem>
                            )}
                            {canAdd && (
                                <DropdownMenuItem onClick={() => onAddClick(menu)}>
                                    <Plus className="mr-2 h-4 w-4"/>
                                    新增
                                </DropdownMenuItem>
                            )}
                            {canDelete && (
                                <DropdownMenuItem
                                    onClick={() => onDeleteClick(menu)}
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
    ]
}