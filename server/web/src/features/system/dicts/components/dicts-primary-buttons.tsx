import { Plus } from 'lucide-react'
import { PERMISSIONS } from '@/constants/permissions'
import { usePermission } from '@/hooks/usePermission'
import { Button } from '@/components/ui/button'
import { useDicts } from './dicts-provider'

export function DictsPrimaryButtons() {
  const { setShowTypeCreateDialog } = useDicts()
  const { hasPermission } = usePermission()

  const canAddType = hasPermission(PERMISSIONS.SYSTEM.DICT.ADD_TYPE)

  const handleCreateTypeClick = () => {
    setShowTypeCreateDialog(true)
  }

  return (
    <div className='flex space-x-2'>
      {canAddType && (
        <Button onClick={handleCreateTypeClick} className='space-x-1'>
          创建字典类型
          <Plus className='ml-1 h-4 w-4' />
        </Button>
      )}
    </div>
  )
}
