import {useState} from 'react'
import {useMutation, useQueryClient} from '@tanstack/react-query'
import {updateMenu} from '@/services/menuApi'
import {menuService} from '@/services/menu-service'
import type {Menu} from '@/types/menu'
import type {TableMenuItem} from '@/lib/menu-utils'

export function useMenuDrag(tableData: TableMenuItem[], menuData?: { list: Menu[] }) {
    const [draggingId, setDraggingId] = useState<string | null>(null)
    const [dragOverId, setDragOverId] = useState<string | null>(null)
    const [dragOverPosition, setDragOverPosition] = useState<'above' | 'below' | null>(null)

    const queryClient = useQueryClient()

    const reorderMutation = useMutation({
        mutationFn: async (payload: { parentId?: string; ordered: Menu[] }) => {
            const {ordered} = payload
            for (let i = 0; i < ordered.length; i++) {
                const m = ordered[i]
                const newSeq = i + 1
                if (m.sequence === newSeq) continue

                await updateMenu(m.id, {
                    name: m.name,
                    sequence: newSeq,
                    type: m.type,
                    path: m.path,
                    icon: m.icon,
                    component: m.component,
                    route_name: m.route_name,
                    query: m.query,
                    is_frame: m.is_frame,
                    visible: m.visible,
                    permissions: m.permissions,
                    status: m.status,
                    parent_id: m.parent_id,
                    apiResources: m.apiResources || []
                })
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['menus']})
            menuService.clearCache()
        }
    })

    const handleDragStart = (e: React.DragEvent<HTMLTableRowElement>, item: TableMenuItem) => {
        e.dataTransfer.effectAllowed = 'move'
        setDraggingId(item.id)
    }

    const handleDragOver = (e: React.DragEvent<HTMLTableRowElement>, target: TableMenuItem) => {
        if (!draggingId || draggingId === target.id) return

        const draggingItem = tableData.find(d => d.id === draggingId)
        if (!draggingItem) return
        if (draggingItem.level !== target.level) return
        if ((draggingItem.parent_id || '') !== (target.parent_id || '')) return

        e.preventDefault()
        const bounds = (e.currentTarget as HTMLElement).getBoundingClientRect()
        const offsetY = e.clientY - bounds.top
        const position = offsetY < bounds.height / 2 ? 'above' : 'below'
        setDragOverId(target.id)
        setDragOverPosition(position)
    }

    const handleDragLeave = (_: React.DragEvent<HTMLTableRowElement>, target: TableMenuItem) => {
        setDragOverId(prev => (prev === target.id ? null : prev))
        setDragOverPosition(null)
    }

    const handleDrop = (e: React.DragEvent<HTMLTableRowElement>, target: TableMenuItem) => {
        e.preventDefault()
        if (!draggingId || draggingId === target.id) return

        const draggingItem = tableData.find(d => d.id === draggingId)
        if (!draggingItem) return
        if (draggingItem.level !== target.level) return
        if ((draggingItem.parent_id || '') !== (target.parent_id || '')) return

        const siblings = (menuData?.list || []).filter(m => (m.parent_id || '') === (draggingItem.parent_id || ''))
            .sort((a, b) => a.sequence - b.sequence)

        const draggingIndex = siblings.findIndex(s => s.id === draggingItem.id)
        const targetIndex = siblings.findIndex(s => s.id === target.id)
        if (draggingIndex === -1 || targetIndex === -1) return

        const newOrder = [...siblings]
        const [removed] = newOrder.splice(draggingIndex, 1)
        let insertIndex = targetIndex

        if (dragOverPosition === 'below' && targetIndex < newOrder.length) {
            insertIndex = targetIndex + (draggingIndex < targetIndex ? 0 : 1)
        } else if (dragOverPosition === 'above') {
            insertIndex = targetIndex + (draggingIndex < targetIndex ? -1 : 0)
        }

        if (insertIndex < 0) insertIndex = 0
        if (insertIndex > newOrder.length) insertIndex = newOrder.length
        newOrder.splice(insertIndex, 0, removed)

        reorderMutation.mutate({parentId: draggingItem.parent_id, ordered: newOrder})

        // Optimistic update
        queryClient.setQueryData(['menus'], (old: unknown) => {
            if (!old || typeof old !== 'object' || !('list' in old)) return old
            const oldData = old as { list: Menu[] }
            const updatedList = oldData.list.map((m: Menu) => {
                const idx = newOrder.findIndex(n => n.id === m.id)
                if (idx >= 0) {
                    return {...m, sequence: idx + 1}
                }
                return m
            })
            return {...oldData, list: updatedList}
        })

        handleDragEnd()
    }

    const handleDragEnd = () => {
        setDraggingId(null)
        setDragOverId(null)
        setDragOverPosition(null)
    }

    return {
        draggingId,
        dragOverId,
        dragOverPosition,
        handleDragStart,
        handleDragOver,
        handleDragLeave,
        handleDrop,
        handleDragEnd
    }
}