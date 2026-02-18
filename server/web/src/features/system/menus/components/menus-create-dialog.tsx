import {Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle} from '@/components/ui/dialog'
import {Form} from '@/components/ui/form'
import {Button} from '@/components/ui/button'
import {useMenuForm} from '../hooks/useMenuForm'
import {useMenuData} from '../hooks/useMenuData'
import {MenuBasicInfo} from './form-sections/menu-basic-info'
import {MenuDisplaySettings} from './form-sections/menu-display-settings'
import {MenuBasicFields} from './form-sections/menu-basic-fields'
import {MenuFrameSettings} from './form-sections/menu-frame-settings'
import {MenuPermissions} from './form-sections/menu-permissions'
import {MenuStatusSettings} from './form-sections/menu-status-settings'

import type {Menu} from '@/types/menu'
import {MenuApiResources} from "@/features/system/menus/components/form-sections/menu-api-resources.tsx";

interface MenusCreateDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    menu?: Menu | null // Optional menu for edit mode
    mode?: 'create' | 'edit' // Dialog mode
}

export function MenusCreateDialog({open, onOpenChange, menu, mode = 'create'}: MenusCreateDialogProps) {
    const {form, selectedType, isEditMode, isLoading, onSubmit} = useMenuForm({
        menu, mode, onSuccess: () => onOpenChange(false),
    })
    const {parentMenuOptions} = useMenuData({open})

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>{isEditMode ? '编辑菜单' : '添加菜单'}</DialogTitle>
                    <DialogDescription>
                        {isEditMode ? '修改菜单的配置信息' : '创建新的菜单项并配置其属性'}
                    </DialogDescription>
                </DialogHeader>

                <Form {...form}>
                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                        <MenuBasicInfo form={form} parentMenuOptions={parentMenuOptions}/>

                        <MenuDisplaySettings form={form}/>

                        <MenuBasicFields form={form}/>

                        <MenuFrameSettings form={form}/>

                        <MenuPermissions form={form} selectedType={selectedType}/>
                        <MenuApiResources form={form} isEditMode={mode === 'edit'}/>
                        <MenuStatusSettings form={form}/>


                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => onOpenChange(false)}
                            >
                                取消
                            </Button>
                            <Button
                                type="submit"
                                disabled={isLoading}
                            >
                                {isEditMode ? '更新' : '确定'}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    )
}
