import {createContext, type Dispatch, type ReactNode, type SetStateAction, useContext, useState,} from 'react'
import type {Menu} from '@/types/menu'

interface MenusContext {
    open: boolean
    setOpen: Dispatch<SetStateAction<boolean>>
    currentRow: Menu | null
    setCurrentRow: Dispatch<SetStateAction<Menu | null>>
    showDeleteDialog: boolean
    setShowDeleteDialog: Dispatch<SetStateAction<boolean>>
    showCreateDialog: boolean
    setShowCreateDialog: Dispatch<SetStateAction<boolean>>
    showEditDialog: boolean
    setShowEditDialog: Dispatch<SetStateAction<boolean>>
    showTreeView: boolean
    setShowTreeView: Dispatch<SetStateAction<boolean>>
}

const MenusContext = createContext<MenusContext | null>(null)

interface MenusProviderProps {
    children: ReactNode
}

export function MenusProvider({children}: MenusProviderProps) {
    const [open, setOpen] = useState(false)
    const [currentRow, setCurrentRow] = useState<Menu | null>(null)
    const [showDeleteDialog, setShowDeleteDialog] = useState(false)
    const [showCreateDialog, setShowCreateDialog] = useState(false)
    const [showEditDialog, setShowEditDialog] = useState(false)
    const [showTreeView, setShowTreeView] = useState(false)

    return (
        <MenusContext.Provider
            value={{
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
                showTreeView,
                setShowTreeView,
            }}
        >
            {children}
        </MenusContext.Provider>
    )
}

export const useMenus = () => {
    const menusContext = useContext(MenusContext)

    if (!menusContext) {
        throw new Error('useMenus has to be used within <MenusProvider>')
    }

    return menusContext
}