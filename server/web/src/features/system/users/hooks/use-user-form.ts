import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import {type UserFormData, userFormSchema} from '../lib/user-form-schema'
import {type User} from '../data/schema'
import {useCreateUser, useUpdateUser} from './use-users'
import {getRoles} from '@/services/roleApi'
import {getUserRoleList} from '@/services/userApi'

interface UseUserFormProps {
    currentRow?: User
    onSuccess: () => void
}

export function useUserForm({currentRow, onSuccess}: UseUserFormProps) {
    const isEdit = !!currentRow
    const createUser = useCreateUser()
    const updateUser = useUpdateUser()
    const queryClient = useQueryClient()

    // Fetch user roles if editing
    const {data: userRoles = []} = useQuery({
        queryKey: ['userRoles', currentRow?.id],
        queryFn: () => getUserRoleList(currentRow!.id) as Promise<string[]>,
        enabled: !!currentRow?.id,
    })

    // Fetch all roles for selection
    const {data: allRoles = []} = useQuery({
        queryKey: ['roles'],
        queryFn: getRoles,
    })

    const form = useForm<UserFormData>({
        resolver: zodResolver(userFormSchema),
        defaultValues: {
            username: isEdit ? currentRow.username : '',
            email: isEdit ? currentRow.email : '',
            phone: isEdit ? (currentRow.phone || '') : '',
            status: isEdit ? currentRow.status : 'active',
            password: '',
            confirmPassword: '',
            roles: [],
            isEdit,
        },
    })

    // Update form roles when userRoles data is loaded
    if (isEdit && userRoles && userRoles.length > 0 && form.getValues('roles').length === 0) {
        form.setValue('roles', userRoles)
    }

    const onSubmit = async (values: UserFormData) => {
        try {
            if (isEdit) {
                const updateData = {
                    username: values.username,
                    email: values.email,
                    phone: values.phone || undefined,
                    status: values.status as any,
                }

                if (values.password) {
                    (updateData as any).password = values.password
                }

                // Create mapping from role name to role ID for the new roles
                const roleNameToIdMap = allRoles.reduce((map, role) => {
                    map[role.name] = role.id
                    return map
                }, {} as Record<string, string>)

                // Convert role names to role IDs
                const roleIds = values.roles.map(roleName => roleNameToIdMap[roleName]).filter(Boolean)

                // Update user with roles included
                const updateDataWithRoles = {
                    ...updateData,
                    role_ids: roleIds
                }

                await updateUser.mutateAsync({
                    id: currentRow!.id,
                    data: updateDataWithRoles,
                })

                // Invalidate queries
                queryClient.invalidateQueries({queryKey: ['userRoles', currentRow!.id]})
                queryClient.invalidateQueries({queryKey: ['users']})
            } else {
                // Create mapping from role name to role ID
                const roleNameToIdMap = allRoles.reduce((map, role) => {
                    map[role.name] = role.id
                    return map
                }, {} as Record<string, string>)

                // Convert role names to role IDs
                const roleIds = values.roles.map(roleName => roleNameToIdMap[roleName]).filter(Boolean)

                // Create user with roles
                await createUser.mutateAsync({
                    username: values.username,
                    email: values.email,
                    phone: values.phone || undefined,
                    password: values.password,
                    status: values.status as any,
                    role_ids: roleIds,
                })

                queryClient.invalidateQueries({queryKey: ['users']})
            }

            form.reset()
            onSuccess()
        } catch (error) {
            console.error('Error submitting user form:', error)
        }
    }

    const isPasswordTouched = !!form.formState.dirtyFields.password
    const isSubmitting = createUser.isPending || updateUser.isPending

    return {
        form,
        onSubmit,
        isEdit,
        isPasswordTouched,
        isSubmitting,
        allRoles,
        userRoles,
    }
}