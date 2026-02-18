import {useCallback, useEffect, useMemo} from 'react'
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query'
import {toast} from 'sonner'

import {Button} from '@/components/ui/button'
import {Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle} from '@/components/ui/sheet'
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select'
import {createRole, getRoleMenus, updateRole} from '@/services/roleApi'
import {type CreateRoleRequest, type Role, type UpdateRoleRequest} from '@/types/role'
import {getMenus} from '@/services/menuApi'
import {buildMenuHierarchy, transformMenusForRoleSelection} from '@/lib/menu-utils'
import {useMenuSelection} from '../hooks/useMenuSelection'
import {type RoleFormData, useRoleForm} from '../hooks/useRoleForm'
import {MenuTreeSection} from './MenuTreeSection'


interface CreateRoleDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    role?: Role | null // Optional role for edit mode
}

export function CreateRoleDialog({open, onOpenChange, role}: CreateRoleDialogProps) {
    const isEditMode = !!role
    const queryClient = useQueryClient()

    const menuSelection = useMenuSelection()

    // Fetch menus for role selection
    const {data: menuData, isLoading: menusLoading} = useQuery({
        queryKey: ['menus-for-role'],
        queryFn: () => {
            return getMenus({
                status: 'active',
                page_size: 1000
            })
        },
        enabled: open,
        staleTime: 5 * 60 * 1000,
    })

    // Fetch role menus when in edit mode
    const {data: roleMenus} = useQuery({
        queryKey: ['roleMenus', role?.id],
        queryFn: () => getRoleMenus(role!.id),
        enabled: !!(open && isEditMode && role?.id),
    })

    const {form} = useRoleForm({
        open,
        isEditMode,
        role,
        roleMenus,
        onMenuSelectionChange: menuSelection.setSelectedMenus
    })

    // Mutation for both create and update
    const roleMutation = useMutation({
        mutationFn: (data: RoleFormData) => {
            if (isEditMode && role) {
                const payload: UpdateRoleRequest = {
                    name: data.name,
                    sequence: data.sequence,
                    status: data.status,
                    menu_ids: Array.from(menuSelection.selectedMenus)
                }
                return updateRole(role.id, payload)
            } else {
                const payload: CreateRoleRequest = {
                    name: data.name,
                    sequence: data.sequence,
                    status: data.status,
                    menu_ids: Array.from(menuSelection.selectedMenus)
                }
                return createRole(payload)
            }
        },
        onSuccess: () => {
            toast.success(isEditMode ? '角色更新成功' : '角色创建成功')
            queryClient.invalidateQueries({queryKey: ['roles']})
            closeDialog()
        },
        onError: (e: any) => {
            const msg = e?.response?.data?.message || e?.message || (isEditMode ? '角色更新失败' : '角色创建失败')
            toast.error(msg)
        }
    })

    const closeDialog = useCallback(() => {
        onOpenChange(false)
    }, [onOpenChange])

    const onSubmit = useCallback((data: RoleFormData) => {
        roleMutation.mutate(data)
    }, [roleMutation])

    // Process menu data when it changes
    useEffect(() => {
        if (menuData?.list) {
            const hierarchicalMenus = buildMenuHierarchy(menuData.list)
            const transformedMenus = transformMenusForRoleSelection(hierarchicalMenus)
            menuSelection.setMenusData(transformedMenus)
        }
    }, [menuData, menuSelection.setMenusData])


    // Reset menu selection when dialog closes
    useEffect(() => {
        if (!open) {
            menuSelection.resetSelection()
        }
    }, [open, menuSelection.resetSelection])

    const renderStatusOverlay = useMemo(() => {
        if (!open) return null
        if (menusLoading) return <div className="p-4 text-sm text-muted-foreground">数据加载中...</div>
        return null
    }, [open, menusLoading])

    return (
        <Sheet open={open} onOpenChange={onOpenChange}>
            <SheetContent className="flex flex-col min-w-1/4">
                <SheetHeader>
                    <SheetTitle>{isEditMode ? '编辑角色' : '创建角色'}</SheetTitle>
                    <SheetDescription>
                        {isEditMode ? '修改角色信息并分配相应的菜单权限' : '创建新的角色并分配菜单权限'}
                    </SheetDescription>
                </SheetHeader>

                {renderStatusOverlay}

                <div className="flex flex-col flex-1 min-h-0">
                    {open && !menusLoading && (
                        <Form {...form}>
                            <form onSubmit={form.handleSubmit(onSubmit)}
                                  className="flex flex-col h-full mx-4 space-y-4">

                                <FormField control={form.control} name="name" render={({field}) => (
                                    <FormItem>
                                        <FormLabel>角色名称 *</FormLabel>
                                        <FormControl>
                                            <Input placeholder="请输入角色名称" {...field}/>
                                        </FormControl>
                                        <FormMessage/>
                                    </FormItem>
                                )}/>

                                <FormField control={form.control} name="sequence" render={({field}) => (
                                    <FormItem>
                                        <FormLabel>角色顺序</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number"
                                                {...field}
                                                value={field.value?.toString() || '0'}
                                                onChange={e => {
                                                    const v = e.target.value
                                                    const n = v === '' ? 0 : parseInt(v, 10)
                                                    field.onChange(Number.isNaN(n) ? 0 : n)
                                                }}
                                            />
                                        </FormControl>
                                        <FormMessage/>
                                    </FormItem>
                                )}/>

                                <FormField control={form.control} name="status" render={({field}) => (
                                    <FormItem>
                                        <FormLabel>状态</FormLabel>
                                        <Select onValueChange={field.onChange} value={field.value}>
                                            <FormControl>
                                                <SelectTrigger>
                                                    <SelectValue/>
                                                </SelectTrigger>
                                            </FormControl>
                                            <SelectContent>
                                                <SelectItem value="active">正常</SelectItem>
                                                <SelectItem value="inactive">停用</SelectItem>
                                            </SelectContent>
                                        </Select>
                                        <FormMessage/>
                                    </FormItem>
                                )}/>

                                <MenuTreeSection
                                    menusData={menuSelection.menusData}
                                    expandedNodes={menuSelection.expandedNodes}
                                    selectedMenus={menuSelection.selectedMenus}
                                    onToggle={menuSelection.toggleNode}
                                    onSelect={menuSelection.toggleSelect}
                                    onSelectAll={menuSelection.toggleSelectAll}
                                    onExpandRoot={menuSelection.toggleExpandRoot}
                                    className="h-full overflow-hidden"
                                />

                                <SheetFooter>
                                    <div className="flex justify-center w-full">
                                        <Button
                                            type="submit"
                                            disabled={roleMutation.isPending}
                                            className="max-w-xs w-full mx-4"
                                        >
                                            {roleMutation.isPending ? (isEditMode ? '更新中...' : '创建中...') : (isEditMode ? '更新' : '创建')}
                                        </Button>
                                    </div>
                                </SheetFooter>
                            </form>
                        </Form>
                    )}
                </div>
            </SheetContent>
        </Sheet>
    )
}
