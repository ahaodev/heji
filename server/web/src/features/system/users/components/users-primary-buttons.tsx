import {MailPlus, UserPlus} from 'lucide-react'
import {Button} from '@/components/ui/button'
import {useUsers} from './users-provider'
import {usePermission} from '@/hooks/usePermission'
import {PERMISSIONS} from '@/constants/permissions'

export function UsersPrimaryButtons() {
    const {setOpen} = useUsers()
    const {hasPermission} = usePermission()

    return (
        <div className='flex gap-2'>
            {hasPermission(PERMISSIONS.SYSTEM.USER.INVITE) && (
                <Button
                    variant='outline'
                    className='space-x-1'
                    onClick={() => setOpen('invite')}
                >
                    <span>邀请用户</span> <MailPlus size={18}/>
                </Button>
            )}
            {hasPermission(PERMISSIONS.SYSTEM.USER.ADD) && (
                <Button className='space-x-1' onClick={() => setOpen('add')}>
                    <span>添加用户</span> <UserPlus size={18}/>
                </Button>
            )}
        </div>
    )
}
