import {createContext, type Dispatch, type ReactNode, type SetStateAction, useContext, useMemo, useState,} from 'react'
import type {Role} from '@/types/role'

interface RolesContext {
    open: boolean
    setOpen: Dispatch<SetStateAction<boolean>>
    currentRow: Role | null
    setCurrentRow: Dispatch<SetStateAction<Role | null>>
    showDeleteDialog: boolean
    setShowDeleteDialog: Dispatch<SetStateAction<boolean>>
    showCreateDialog: boolean
    setShowCreateDialog: Dispatch<SetStateAction<boolean>>
    showEditDialog: boolean
    setShowEditDialog: Dispatch<SetStateAction<boolean>>
}

const RolesContext = createContext<RolesContext | null>(null)

interface RolesProviderProps {
    children: ReactNode
}

export function RolesProvider({children}: RolesProviderProps) {
    const [open, setOpen] = useState(false)
    const [currentRow, setCurrentRow] = useState<Role | null>(null)
    const [showDeleteDialog, setShowDeleteDialog] = useState(false)
    const [showCreateDialog, setShowCreateDialog] = useState(false)
    const [showEditDialog, setShowEditDialog] = useState(false)

    const contextValue: RolesContext = useMemo(
        () => ({
            open,
            setOpen,
            currentRow,
            setCurrentRow,
            showDeleteDialog,
            setShowDeleteDialog,
            showCreateDialog,
            setShowCreateDialog,
            showEditDialog,
            setShowEditDialog,
        }),
        [open, currentRow, showDeleteDialog, showCreateDialog, showEditDialog]
    )

    if (process.env.NODE_ENV !== 'production') {
        // 简单渲染次数日志
        console.debug('🧪 RolesProvider render', {
            open,
            showCreateDialog,
            showEditDialog,
            showDeleteDialog
        })
    }

    return (
        <RolesContext.Provider value={contextValue}>
            {children}
        </RolesContext.Provider>
    )
}

export const useRoles = () => {
    const rolesContext = useContext(RolesContext)

    if (!rolesContext) {
        throw new Error('useRoles has to be used within <RolesProvider>')
    }

    return rolesContext
}