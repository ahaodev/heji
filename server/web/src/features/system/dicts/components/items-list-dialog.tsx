import {Dialog, DialogContent, DialogHeader, DialogTitle} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import {Plus} from 'lucide-react'
import {useDicts} from './dicts-provider'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'
import {ItemsTable} from './items-table'

export function ItemsListDialog() {
    const {
        selectedType,
        showItemsListDialog,
        setShowItemsListDialog,
        setShowItemCreateDialog,
    } = useDicts()
    const {hasPermission} = usePermission()

    const canAddItem = hasPermission(PERMISSIONS.SYSTEM.DICT.ADD_ITEM)

    return (
        <Dialog open={showItemsListDialog} onOpenChange={setShowItemsListDialog}>
            <DialogContent className='max-w-4xl'>
                <DialogHeader>
                    <DialogTitle>
                        字典项列表{selectedType ? `（${selectedType.name}｜编码：${selectedType.code}）` : ''}
                    </DialogTitle>
                </DialogHeader>
                <div className='mb-2 flex items-center justify-between'>
                    <div className='text-sm text-muted-foreground'>
                        {selectedType ? '仅展示该字典类型下的字典项' : '请选择字典类型'}
                    </div>
                    {canAddItem && (
                        <Button size='sm' onClick={() => setShowItemCreateDialog(true)} className='space-x-1'>
                            新增字典项
                            <Plus className='ml-1 h-4 w-4'/>
                        </Button>
                    )}
                </div>
                <div className='max-h-[60vh] overflow-auto'>
                    {/* 字典项列表 */}
                    <ItemsTable/>
                </div>
            </DialogContent>
        </Dialog>
    )
}

