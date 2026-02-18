import type {ColumnDef} from '@tanstack/react-table'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import {Copy} from 'lucide-react'
import type {ApiResource} from '@/types/api-resource'
import {toast} from 'sonner'
import {type HttpMethod, METHOD_COLORS} from '../constants/api-constants'

const handleCopyPath = (apiResource: ApiResource) => {
    const pathText = `${apiResource.method} ${apiResource.path}`
    navigator.clipboard.writeText(pathText)
    toast.success('API路径已复制到剪贴板')
}

export const apiResourcesColumns: ColumnDef<ApiResource>[] = [
    {
        accessorKey: 'module',
        header: '模块',
        cell: ({row}) => {
            const module = row.getValue('module') as string
            return module ? (
                <Badge variant="outline">{module}</Badge>
            ) : (
                <span className="text-muted-foreground">-</span>
            )
        },
        filterFn: (row, id, value) => {
            const module = row.getValue(id) as string
            if (!value || value.length === 0) return true
            return value.includes(module || '')
        },
    },
    {
        accessorKey: 'method',
        header: '方法',
        cell: ({row}) => {
            const method = row.getValue('method') as string
            return (
                <Badge
                    variant="secondary"
                    className={METHOD_COLORS[method as HttpMethod]}
                >
                    {method}
                </Badge>
            )
        },
        meta: {
            className: 'w-[100px]',
        },
    },
    {
        accessorKey: 'path',
        header: '路径',
        cell: ({row}) => {
            const path = row.getValue('path') as string
            return <span className="font-mono text-sm">{path}</span>
        },
    },
    {
        id: 'actions',
        header: '操作',
        cell: ({row}) => (
            <Button
                variant="ghost"
                size="sm"
                onClick={() => handleCopyPath(row.original)}
            >
                <Copy className="mr-2 h-4 w-4"/>
            </Button>
        ),
        meta: {
            className: 'w-[100px]',
        },
    },
]