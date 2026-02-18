import {createContext, type Dispatch, type ReactNode, type SetStateAction, useState,} from 'react'
import type {ApiResource} from '@/types/api-resource'

interface ApiResourcesContext {
    currentRow: ApiResource | null
    setCurrentRow: Dispatch<SetStateAction<ApiResource | null>>
    showUpdateDialog: boolean
    setShowUpdateDialog: Dispatch<SetStateAction<boolean>>
    selectedIds: string[]
    setSelectedIds: Dispatch<SetStateAction<string[]>>
}

const ApiResourcesContext = createContext<ApiResourcesContext | null>(null)

interface ApiResourcesProviderProps {
    children: ReactNode
}

export function ApiResourcesProvider({children}: ApiResourcesProviderProps) {
    const [currentRow, setCurrentRow] = useState<ApiResource | null>(null)
    const [showUpdateDialog, setShowUpdateDialog] = useState(false)
    const [selectedIds, setSelectedIds] = useState<string[]>([])

    return (
        <ApiResourcesContext.Provider
            value={{
                currentRow,
                setCurrentRow,
                showUpdateDialog,
                setShowUpdateDialog,
                selectedIds,
                setSelectedIds,
            }}
        >
            {children}
        </ApiResourcesContext.Provider>
    )
}