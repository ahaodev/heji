import {
  createContext,
  type Dispatch,
  type ReactNode,
  type SetStateAction,
  useContext,
  useMemo,
  useState,
} from 'react'
import type { DictItem, DictType } from '@/types/dict'

interface DictsContext {
  // 选中的字典类型
  selectedType: DictType | null
  setSelectedType: Dispatch<SetStateAction<DictType | null>>

  // 字典类型相关状态
  currentTypeRow: DictType | null
  setCurrentTypeRow: Dispatch<SetStateAction<DictType | null>>
  showTypeCreateDialog: boolean
  setShowTypeCreateDialog: Dispatch<SetStateAction<boolean>>
  showTypeEditDialog: boolean
  setShowTypeEditDialog: Dispatch<SetStateAction<boolean>>
  showTypeDeleteDialog: boolean
  setShowTypeDeleteDialog: Dispatch<SetStateAction<boolean>>

  // 字典项相关状态
  currentItemRow: DictItem | null
  setCurrentItemRow: Dispatch<SetStateAction<DictItem | null>>
  showItemCreateDialog: boolean
  setShowItemCreateDialog: Dispatch<SetStateAction<boolean>>
  showItemEditDialog: boolean
  setShowItemEditDialog: Dispatch<SetStateAction<boolean>>
  showItemDeleteDialog: boolean
  setShowItemDeleteDialog: Dispatch<SetStateAction<boolean>>
  // 列表对话框：显示选中类型下的字典项列表
  showItemsListDialog: boolean
  setShowItemsListDialog: Dispatch<SetStateAction<boolean>>

  // 通用状态
  refreshTypes: number
  setRefreshTypes: Dispatch<SetStateAction<number>>
  refreshItems: number
  setRefreshItems: Dispatch<SetStateAction<number>>
}

const DictsContext = createContext<DictsContext | null>(null)

interface DictsProviderProps {
  children: ReactNode
}

export function DictsProvider({ children }: DictsProviderProps) {
  // 选中的字典类型
  const [selectedType, setSelectedType] = useState<DictType | null>(null)

  // 字典类型相关状态
  const [currentTypeRow, setCurrentTypeRow] = useState<DictType | null>(null)
  const [showTypeCreateDialog, setShowTypeCreateDialog] = useState(false)
  const [showTypeEditDialog, setShowTypeEditDialog] = useState(false)
  const [showTypeDeleteDialog, setShowTypeDeleteDialog] = useState(false)

  // 字典项相关状态
  const [currentItemRow, setCurrentItemRow] = useState<DictItem | null>(null)
  const [showItemCreateDialog, setShowItemCreateDialog] = useState(false)
  const [showItemEditDialog, setShowItemEditDialog] = useState(false)
  const [showItemDeleteDialog, setShowItemDeleteDialog] = useState(false)
  const [showItemsListDialog, setShowItemsListDialog] = useState(false)

  // 通用状态
  const [refreshTypes, setRefreshTypes] = useState(0)
  const [refreshItems, setRefreshItems] = useState(0)

  const contextValue: DictsContext = useMemo(
    () => ({
      selectedType,
      setSelectedType,
      currentTypeRow,
      setCurrentTypeRow,
      showTypeCreateDialog,
      setShowTypeCreateDialog,
      showTypeEditDialog,
      setShowTypeEditDialog,
      showTypeDeleteDialog,
      setShowTypeDeleteDialog,
      currentItemRow,
      setCurrentItemRow,
      showItemCreateDialog,
      setShowItemCreateDialog,
      showItemEditDialog,
      setShowItemEditDialog,
      showItemDeleteDialog,
      setShowItemDeleteDialog,
      showItemsListDialog,
      setShowItemsListDialog,
      refreshTypes,
      setRefreshTypes,
      refreshItems,
      setRefreshItems,
    }),
    [
      selectedType,
      currentTypeRow,
      showTypeCreateDialog,
      showTypeEditDialog,
      showTypeDeleteDialog,
      currentItemRow,
      showItemCreateDialog,
      showItemEditDialog,
      showItemDeleteDialog,
      showItemsListDialog,
      refreshTypes,
      refreshItems,
    ]
  )

  if (process.env.NODE_ENV !== 'production') {
    console.debug('🧪 DictsProvider render', {
      selectedType: selectedType?.name,
      showTypeCreateDialog,
      showTypeEditDialog,
      showTypeDeleteDialog,
      showItemCreateDialog,
      showItemEditDialog,
      showItemDeleteDialog,
      showItemsListDialog,
    })
  }

  return (
    <DictsContext.Provider value={contextValue}>
      {children}
    </DictsContext.Provider>
  )
}

export const useDicts = () => {
  const dictsContext = useContext(DictsContext)

  if (!dictsContext) {
    throw new Error('useDicts has to be used within <DictsProvider>')
  }

  return dictsContext
}
