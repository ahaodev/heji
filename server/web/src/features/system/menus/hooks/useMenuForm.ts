import {useMutation, useQueryClient} from '@tanstack/react-query'
import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {useEffect} from 'react'
import {toast} from 'sonner'
import {createMenu, updateMenu} from '@/services/menuApi'
import {menuService} from '@/services/menu-service'
import {type CreateMenuFormData, createMenuSchema, defaultFormValues} from '../schemas/menu-form-schema'
import type {Menu} from '@/types/menu'


interface UseMenuFormProps {
    menu?: Menu | null
    mode?: 'create' | 'edit'
    onSuccess?: () => void
}

export function useMenuForm({menu, mode = 'create', onSuccess}: UseMenuFormProps) {
    const queryClient = useQueryClient()
    const isEditMode = mode === 'edit' && menu

    const form = useForm<CreateMenuFormData>({
        resolver: zodResolver(createMenuSchema),
        defaultValues: defaultFormValues,
    })

    const selectedType = form.watch('type')

    useEffect(() => {
        if (isEditMode && menu) {
            form.reset({
                name: menu.name,
                sequence: menu.sequence,
                type: menu.type,
                path: menu.path || '',
                icon: menu.icon,
                component: menu.component || '',
                route_name: menu.route_name || '',
                query: menu.query || '',
                is_frame: menu.is_frame,
                visible: menu.visible as 'show' | 'hide',
                permissions: menu.permissions || '',
                status: menu.status,
                parent_id: menu.parent_id || 'ROOT',
                apiResources: menu.apiResources || [],
            })
        } else if (!isEditMode) {
            form.reset({
                ...defaultFormValues,
                parent_id: menu?.id || 'ROOT',
            })
        }
    }, [isEditMode, menu, form])

    const menuMutation = useMutation({
        mutationFn: (data: CreateMenuFormData) => {
            // sanitize inputs: trim leading/trailing spaces for route path
            const trimmedPath = (data.path || '').trim()

            const apiData = {
                name: data.name,
                sequence: data.sequence,
                type: data.type,
                path: trimmedPath,
                icon: data.icon || '',
                component: data.component || '',
                route_name: data.route_name || '',
                query: data.query || '',
                is_frame: data.is_frame,
                visible: data.visible,
                permissions: data.permissions || '',
                status: data.status,
                parent_id: data.parent_id === 'ROOT' ? undefined : data.parent_id || undefined,
                apiResources: data.apiResources || [],
            }

            if (isEditMode && menu) {
                const updateData = {
                    name: apiData.name,
                    sequence: apiData.sequence,
                    type: apiData.type,
                    path: apiData.path,
                    icon: apiData.icon,
                    component: apiData.component,
                    route_name: apiData.route_name,
                    query: apiData.query,
                    is_frame: apiData.is_frame,
                    visible: apiData.visible,
                    permissions: apiData.permissions,
                    status: apiData.status,
                    parent_id: apiData.parent_id,
                    apiResources: apiData.apiResources,
                }
                return updateMenu(menu.id, updateData)
            } else {
                return createMenu(apiData)
            }
        },
        onSuccess: () => {
            toast.success(isEditMode ? '菜单更新成功' : '菜单创建成功')
            queryClient.invalidateQueries({queryKey: ['menus']})
            queryClient.invalidateQueries({queryKey: ['parent-menus']})
            menuService.clearCache()
            onSuccess?.()
            if (!isEditMode) {
                form.reset()
            }
        },
        onError: (error: unknown) => {
            const errorMessage = error && typeof error === 'object' && 'response' in error &&
            error.response && typeof error.response === 'object' && 'data' in error.response &&
            error.response.data && typeof error.response.data === 'object' && 'msg' in error.response.data
                ? (error.response.data as { msg: string }).msg
                : `${isEditMode ? '更新' : '创建'}菜单失败`
            toast.error(errorMessage)
        },
    })

    const onSubmit = (data: CreateMenuFormData) => {
        menuMutation.mutate(data)
    }

    return {
        form,
        selectedType,
        isEditMode,
        isLoading: menuMutation.isPending,
        onSubmit,
    }
}
