import {RefreshCw, Trash2} from 'lucide-react'
import {Button} from '@/components/ui/button'
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger
} from '@/components/ui/alert-dialog'
import {useClearAllLoginLogs, useRefreshLoginLogs} from '../hooks/use-login-logs'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'
import {toast} from 'sonner'

export function LogsPrimaryButtons() {
    const {hasPermission} = usePermission()
    const clearAllMutation = useClearAllLoginLogs()
    const refreshLogs = useRefreshLoginLogs()

    const canCleanLogs = hasPermission(PERMISSIONS.SYSTEM.LOGIN_LOGS.CLEAN)

    const handleRefresh = () => {
        refreshLogs()
        toast.success('数据已刷新')
    }

    const handleClearAll = () => {
        clearAllMutation.mutate()
    }

    return (
        <div className='flex gap-2'>
            <Button
                variant='outline'
                className='space-x-1'
                onClick={handleRefresh}
            >
                <span>刷新</span> <RefreshCw className='h-4 w-4'/>
            </Button>

            {canCleanLogs && (
                <AlertDialog>
                    <AlertDialogTrigger asChild>
                        <Button
                            variant='destructive'
                            className='space-x-1'
                            disabled={clearAllMutation.isPending}
                        >
                            <span>清空全部</span> <Trash2 className='h-4 w-4'/>
                        </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                        <AlertDialogHeader>
                            <AlertDialogTitle>确认清空所有日志</AlertDialogTitle>
                            <AlertDialogDescription>
                                此操作将永久删除所有登录日志记录，无法恢复。确定要继续吗？
                            </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                            <AlertDialogCancel>取消</AlertDialogCancel>
                            <AlertDialogAction
                                onClick={handleClearAll}
                                className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
                            >
                                确认清空
                            </AlertDialogAction>
                        </AlertDialogFooter>
                    </AlertDialogContent>
                </AlertDialog>
            )}
        </div>
    )
}