import { Plus } from 'lucide-react'
import { PERMISSIONS } from '@/constants/permissions'
import { usePermission } from '@/hooks/usePermission'
import { Button } from '@/components/ui/button'
import { useRoles } from './roles-provider'

export function RolesPrimaryButtons() {
  const { setShowCreateDialog } = useRoles()
  const { hasPermission } = usePermission()

  const canAdd = hasPermission(PERMISSIONS.SYSTEM.ROLE.ADD)

  const handleCreateClick = () => {
    setShowCreateDialog(true)
  }

  return (
    <div className='flex space-x-2'>
      {canAdd && (
        <Button onClick={handleCreateClick} className='space-x-1'>
          创建角色
          <Plus className='ml-1 h-4 w-4' />
        </Button>
      )}
    </div>
  )
}
