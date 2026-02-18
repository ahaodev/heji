import {useQuery} from '@tanstack/react-query'
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@/components/ui/table'
import {Button} from '@/components/ui/button'
import {Badge} from '@/components/ui/badge'
import {Edit, MoreHorizontal, Star, StarOff, Trash2} from 'lucide-react'
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from '@/components/ui/dropdown-menu'
import {getDictItemsByTypeId, setDictItemAsDefault, toggleDictItemStatus} from '@/services/dictApi'
import {useDicts} from './dicts-provider'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'
import {toast} from 'sonner'
import type {DictItem} from '@/types/dict'

interface ItemsTableProps {
    search?: any
    navigate?: any
}

export function ItemsTable({}: ItemsTableProps) {
    const {
        selectedType,
        setCurrentItemRow,
        setShowItemEditDialog,
        setShowItemDeleteDialog,
        refreshItems,
        setRefreshItems,
    } = useDicts()
    const {hasPermission} = usePermission()

    const canEdit = hasPermission(PERMISSIONS.SYSTEM.DICT.EDIT_ITEM)
    const canDelete = hasPermission(PERMISSIONS.SYSTEM.DICT.DELETE_ITEM)

    const {data: itemsData, isLoading, error} = useQuery({
        queryKey: ['dictItems', selectedType?.id, refreshItems],
        queryFn: () => {
            if (!selectedType?.id) return {list: [], total: 0, page: 1, page_size: 10, total_pages: 0}
            return getDictItemsByTypeId(selectedType.id, {page: 1, page_size: 50})
        },
        enabled: !!selectedType?.id,
    })

    const handleEdit = (item: DictItem) => {
        setCurrentItemRow(item)
        setShowItemEditDialog(true)
    }

    const handleDelete = (item: DictItem) => {
        setCurrentItemRow(item)
        setShowItemDeleteDialog(true)
    }

    const handleToggleDefault = async (item: DictItem) => {
        if (item.is_default) {
            toast.error('当前项已是默认项')
            return
        }
        try {
            await setDictItemAsDefault(item.id)
            setRefreshItems((prev) => prev + 1)
            toast.success('设置默认项成功')
        } catch {
            toast.error('设置默认项失败')
        }
    }

    const handleToggleStatus = async (item: DictItem) => {
        try {
            await toggleDictItemStatus(item.id, item.status)
            setRefreshItems((prev) => prev + 1)
            toast.success(`${item.status === 'active' ? '禁用' : '启用'}成功`)
        } catch {
            toast.error(`${item.status === 'active' ? '禁用' : '启用'}失败`)
        }
    }

    if (!selectedType) {
        return <div className="p-8 text-center text-muted-foreground">请先选择一个字典类型</div>
    }
    if (isLoading) {
        return <div className="p-4 text-center">加载中...</div>
    }
    if (error) {
        return <div className="p-4 text-center text-red-500">加载失败</div>
    }

    const items = itemsData?.list || []

    return (
        <div className="space-y-4">
            <div className="rounded-md border">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>标签</TableHead>
                            <TableHead>值</TableHead>
                            <TableHead>排序</TableHead>
                            <TableHead>默认</TableHead>
                            <TableHead>状态</TableHead>
                            <TableHead>颜色</TableHead>
                            <TableHead className="w-[70px]">操作</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {items.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                                    暂无字典项，请创建字典项
                                </TableCell>
                            </TableRow>
                        ) : (
                            items.map((item) => (
                                <TableRow key={item.id}>
                                    <TableCell className="font-medium">{item.label}</TableCell>
                                    <TableCell className="font-mono text-sm">{item.value}</TableCell>
                                    <TableCell>{item.sort}</TableCell>
                                    <TableCell>
                                        {item.is_default ? (
                                            <Badge variant="default">
                                                <Star className="mr-1 h-3 w-3"/> 默认
                                            </Badge>
                                        ) : (
                                            <span className="text-muted-foreground">-</span>
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={item.status === 'active' ? 'default' : 'secondary'}>
                                            {item.status === 'active' ? '启用' : '禁用'}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {item.color ? (
                                            <div className="flex items-center space-x-2">
                                                <div className="w-4 h-4 rounded border border-border"
                                                     style={{backgroundColor: item.color}}/>
                                                <span className="text-xs font-mono">{item.color}</span>
                                            </div>
                                        ) : (
                                            <span className="text-muted-foreground">-</span>
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        <DropdownMenu>
                                            <DropdownMenuTrigger asChild>
                                                <Button variant="ghost" className="h-8 w-8 p-0">
                                                    <MoreHorizontal className="h-4 w-4"/>
                                                </Button>
                                            </DropdownMenuTrigger>
                                            <DropdownMenuContent align="end">
                                                {canEdit && (
                                                    <>
                                                        <DropdownMenuItem onClick={() => handleEdit(item)}>
                                                            <Edit className="mr-2 h-4 w-4"/> 编辑
                                                        </DropdownMenuItem>
                                                        {!item.is_default && (
                                                            <DropdownMenuItem onClick={() => handleToggleDefault(item)}>
                                                                <Star className="mr-2 h-4 w-4"/> 设为默认
                                                            </DropdownMenuItem>
                                                        )}
                                                        <DropdownMenuItem onClick={() => handleToggleStatus(item)}>
                                                            {item.status === 'active' ? (
                                                                <>
                                                                    <StarOff className="mr-2 h-4 w-4"/> 禁用
                                                                </>
                                                            ) : (
                                                                <>
                                                                    <Star className="mr-2 h-4 w-4"/> 启用
                                                                </>
                                                            )}
                                                        </DropdownMenuItem>
                                                    </>
                                                )}
                                                {canDelete && (
                                                    <DropdownMenuItem onClick={() => handleDelete(item)}>
                                                        <Trash2 className="mr-2 h-4 w-4"/> 删除
                                                    </DropdownMenuItem>
                                                )}
                                            </DropdownMenuContent>
                                        </DropdownMenu>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    )
}
