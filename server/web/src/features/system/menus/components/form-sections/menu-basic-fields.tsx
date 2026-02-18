import type {UseFormReturn} from 'react-hook-form'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'

interface MenuBasicFieldsProps {
    form: UseFormReturn<CreateMenuFormData>
}

export function MenuBasicFields({form}: MenuBasicFieldsProps) {
    const selectedType = form.watch('type')
    const isMenu = (selectedType == 'menu')

    return (
        <div className="grid grid-cols-2 gap-4">
            <FormField
                control={form.control}
                name="name"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>
                            <span className="text-red-500">*</span>
                            {isMenu ? ' 菜单名称' : ' 按钮名称'}
                        </FormLabel>
                        <FormControl>
                            <Input
                                placeholder={isMenu ? "请输入菜单名称" : "请输入按钮名称"}
                                {...field}
                            />
                        </FormControl>
                        <FormMessage/>
                    </FormItem>
                )}
            />
            {isMenu && (
                <FormField
                    control={form.control}
                    name="path"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel><span className="text-red-500">*</span> 路由地址</FormLabel>
                            <FormControl>
                                <Input placeholder="请输入路由地址" {...field} />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
            )}
        </div>
    )
}