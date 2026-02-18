import {type ColumnDef} from '@tanstack/react-table'
import {Badge} from '@/components/ui/badge'
import type {LoginLog} from '../data/schema'

export const loginLogsColumns: ColumnDef<LoginLog>[] = [
    {
        accessorKey: 'username',
        header: '用户名',
        meta: {
            displayName: '用户名',
        },
        cell: ({row}) => (
            <div className='font-medium'>{row.getValue('username')}</div>
        ),
    },
    {
        accessorKey: 'login_ip',
        header: 'IP地址',
        meta: {
            displayName: 'IP地址',
        },
        cell: ({row}) => (
            <code className='text-xs bg-muted px-2 py-1 rounded'>
                {row.getValue('login_ip')}
            </code>
        ),
    },
    {
        accessorKey: 'status',
        header: '状态',
        meta: {
            displayName: '状态',
        },
        cell: ({row}) => {
            const status = row.getValue('status') as string
            return (
                <Badge
                    variant={status === 'success' ? 'default' : 'destructive'}
                    className='text-xs'
                >
                    {status === 'success' ? '成功' : '失败'}
                </Badge>
            )
        },
        enableSorting: true,
        filterFn: (row, id, value) => {
            return value.includes(row.getValue(id))
        },
    },
    {
        accessorKey: 'browser',
        header: '浏览器',
        meta: {
            displayName: '浏览器',
        },
        cell: ({row}) => {
            const browser = row.getValue('browser') as string
            return browser ? (
                <span className='text-sm text-muted-foreground'>{browser}</span>
            ) : (
                <span className='text-xs text-muted-foreground italic'>未知</span>
            )
        },
    },
    {
        accessorKey: 'os',
        header: '操作系统',
        meta: {
            displayName: '操作系统',
        },
        cell: ({row}) => {
            const os = row.getValue('os') as string
            return os ? (
                <span className='text-sm text-muted-foreground'>{os}</span>
            ) : (
                <span className='text-xs text-muted-foreground italic'>未知</span>
            )
        },
    },
    {
        accessorKey: 'device',
        header: '设备',
        meta: {
            displayName: '设备',
        },
        cell: ({row}) => {
            const device = row.getValue('device') as string
            return device ? (
                <span className='text-sm text-muted-foreground'>{device}</span>
            ) : (
                <span className='text-xs text-muted-foreground italic'>未知</span>
            )
        },
    },
    {
        accessorKey: 'failure_reason',
        header: '失败原因',
        meta: {
            displayName: '失败原因',
        },
        cell: ({row}) => {
            const status = row.getValue('status') as string
            const failureReason = row.getValue('failure_reason') as string

            if (status === 'success') {
                return <span className='text-xs text-muted-foreground'>-</span>
            }

            return failureReason ? (
                <span className='text-sm text-red-600'>{failureReason}</span>
            ) : (
                <span className='text-xs text-muted-foreground italic'>未知</span>
            )
        },
    },
    {
        accessorKey: 'login_time',
        header: '登录时间',
        meta: {
            displayName: '登录时间',
        },
        cell: ({row}) => {
            const loginTime = new Date(row.getValue('login_time'))
            return (
                <span className='text-sm font-mono'>
                    {loginTime.toLocaleString('zh-CN')}
                </span>
            )
        },
        enableSorting: true,
    },
]