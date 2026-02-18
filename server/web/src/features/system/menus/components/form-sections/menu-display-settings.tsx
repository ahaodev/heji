import type {UseFormReturn} from 'react-hook-form'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {IconPicker} from '@/components/icon-picker.tsx'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'

interface MenuDisplaySettingsProps {
    form: UseFormReturn<CreateMenuFormData>
}

export function MenuDisplaySettings({form}: MenuDisplaySettingsProps) {
    return (
        <div className="grid grid-cols-2 gap-4">
            <FormField
                control={form.control}
                name="icon"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>菜单图标</FormLabel>
                        <FormControl>
                            <IconPicker
                                value={field.value}
                                onChange={field.onChange}
                                placeholder="点击选择图标"
                            />
                        </FormControl>
                        <FormMessage/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name="sequence"
                render={({field}) => (
                    <FormItem>
                        <FormLabel><span className="text-red-500">*</span> 显示排序</FormLabel>
                        <FormControl>
                            <Input
                                type="number"
                                placeholder="输入显示排序"
                                {...field}
                                onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                            />
                        </FormControl>
                        <FormMessage/>
                    </FormItem>
                )}
            />
        </div>
    )
}