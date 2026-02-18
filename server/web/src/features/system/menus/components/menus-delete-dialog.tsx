import {useMutation, useQueryClient} from '@tanstack/react-query'
import {menuService} from '@/services/menu-service'
import {Button} from '@/components/ui/button'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {deleteMenu} from '@/services/menuApi'
import type {Menu} from '@/types/menu'
import {toast} from 'sonner'

interface MenusDeleteDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    menu: Menu | null
}

export function MenusDeleteDialog({
                                      open,
                                      onOpenChange,
                                      menu,
                                  }: MenusDeleteDialogProps) {
    const queryClient = useQueryClient()

    const deleteMenuMutation = useMutation({
        mutationFn: deleteMenu,
        onSuccess: () => {
            toast.success('菜单删除成功')
            // Invalidate all menu-related queries to ensure data consistency
            queryClient.invalidateQueries({queryKey: ['menu-tree']})
            queryClient.invalidateQueries({queryKey: ['menus']})
            queryClient.invalidateQueries({queryKey: ['parent-menus']})
            // Clear sidebar menu cache to refresh navigation menu
            menuService.clearCache()
            onOpenChange(false)
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.msg || '删除菜单失败')
        },
    })

    const handleDelete = () => {
        if (menu) {
            deleteMenuMutation.mutate(menu.id)
        }
    }

    if (!menu) return null

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>删除菜单</DialogTitle>
                    <DialogDescription>
                        确定要删除菜单 <strong>"{menu.name}"</strong> 吗？
                        此操作不可撤销，同时也会删除所有子菜单。
                    </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                    <Button
                        variant='outline'
                        onClick={() => onOpenChange(false)}
                        disabled={deleteMenuMutation.isPending}
                    >
                        取消
                    </Button>
                    <Button
                        variant='destructive'
                        onClick={handleDelete}
                        disabled={deleteMenuMutation.isPending}
                    >
                        {deleteMenuMutation.isPending ? '删除中...' : '删除菜单'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}