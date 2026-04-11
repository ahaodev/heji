import { Plus } from 'lucide-react'
import { PERMISSIONS } from '@/constants/permissions'
import { usePermission } from '@/hooks/usePermission'
import { Button } from '@/components/ui/button'
import { useMenus } from './menus-provider'

export function MenusPrimaryButtons() {
  const { setShowCreateDialog, setCurrentRow } = useMenus()
  const { hasPermission } = usePermission()

  const handleCreateClick = () => {
    setCurrentRow(null)
    setShowCreateDialog(true)
  }

  return (
    <div className='flex space-x-2'>
      {hasPermission(PERMISSIONS.SYSTEM.MENU.ADD) && (
        <Button onClick={handleCreateClick} className='space-x-1'>
          <span>创建菜单</span>
          <Plus className='ml-1 h-4 w-4' />
        </Button>
      )}
    </div>
  )
}
