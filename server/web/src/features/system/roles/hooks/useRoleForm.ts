import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {z} from 'zod'
import {useCallback, useEffect} from 'react'
import type {Role} from '@/types/role'

const roleSchema = z.object({
    name: z.string().min(1, '角色名称是必填的').max(100, '角色名称最多100个字符'),
    sequence: z.number().int().min(0, '序号必须是非负整数'),
    status: z.enum(['active', 'inactive'])
})

export type RoleFormData = z.infer<typeof roleSchema>

interface UseRoleFormProps {
    open: boolean
    isEditMode: boolean
    role?: Role | null
    roleMenus?: string[]
    onMenuSelectionChange: (menus: Set<string>) => void
}

export function useRoleForm({open, isEditMode, role, roleMenus, onMenuSelectionChange}: UseRoleFormProps) {
    const form = useForm<RoleFormData>({
        resolver: zodResolver(roleSchema),
        defaultValues: {
            name: '',
            sequence: 0,
            status: 'active'
        }
    })

    const resetForm = useCallback(() => {
        if (open && isEditMode && role) {
            form.reset({
                name: role.name,
                sequence: role.sequence,
                status: role.status
            })
            if (roleMenus) {
                onMenuSelectionChange(new Set(roleMenus))
            }
        } else if (open && !isEditMode) {
            form.reset({
                name: '',
                sequence: 0,
                status: 'active'
            })
            onMenuSelectionChange(new Set())
        }

        if (!open) {
            form.reset()
            onMenuSelectionChange(new Set())
        }
    }, [open, isEditMode, role, roleMenus, form, onMenuSelectionChange])

    useEffect(() => {
        resetForm()
    }, [resetForm])

    return {form, resetForm}
}