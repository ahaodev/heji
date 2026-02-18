import {useMutation, useQueryClient} from '@tanstack/react-query'
import {Button} from '@/components/ui/button'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {deleteRole} from '@/services/roleApi'
import type {Role} from '@/types/role'
import {toast} from 'sonner'

interface RolesDeleteDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    roleAssignment: Role | null
}

export function RolesDeleteDialog({
                                      open,
                                      onOpenChange,
                                      roleAssignment,
                                  }: RolesDeleteDialogProps) {
    const queryClient = useQueryClient()

    const deleteRoleMutation = useMutation({
        mutationFn: deleteRole,
        onSuccess: () => {
            toast.success('角色删除成功')
            queryClient.invalidateQueries({queryKey: ['roles']})
            onOpenChange(false)
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.msg || '角色删除失败')
        },
    })

    const handleDelete = () => {
        if (roleAssignment) {
            deleteRoleMutation.mutate(roleAssignment.id)
        }
    }

    if (!roleAssignment) return null

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>删除角色</DialogTitle>
                    <DialogDescription>
                        确定要删除角色 <strong>{roleAssignment.name}</strong> 吗？
                        此操作不可撤销。
                    </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                    <Button
                        variant='outline'
                        onClick={() => onOpenChange(false)}
                        disabled={deleteRoleMutation.isPending}
                    >
                        取消
                    </Button>
                    <Button
                        variant='destructive'
                        onClick={handleDelete}
                        disabled={deleteRoleMutation.isPending}
                    >
                        {deleteRoleMutation.isPending ? '删除中...' : '删除角色'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}