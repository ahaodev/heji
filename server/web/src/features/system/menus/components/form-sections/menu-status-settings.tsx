import type {UseFormReturn} from 'react-hook-form'
import {FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {RadioGroup, RadioGroupItem} from '@/components/ui/radio-group'
import {Label} from '@/components/ui/label'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'

interface MenuStatusSettingsProps {
    form: UseFormReturn<CreateMenuFormData>
}

export function MenuStatusSettings({form}: MenuStatusSettingsProps) {
    return (
        <div className="grid grid-cols-2 gap-4">
            <FormField
                control={form.control}
                name="visible"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>显示状态</FormLabel>
                        <RadioGroup onValueChange={field.onChange} value={field.value}
                                    className="flex space-x-4">
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="show" id="show"/>
                                <Label htmlFor="show">显示</Label>
                            </div>
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="hide" id="hide"/>
                                <Label htmlFor="hide">隐藏</Label>
                            </div>
                        </RadioGroup>
                        <FormMessage/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name="status"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>菜单状态</FormLabel>
                        <RadioGroup onValueChange={field.onChange} value={field.value}
                                    className="flex space-x-4">
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="active" id="active"/>
                                <Label htmlFor="active">正常</Label>
                            </div>
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="inactive" id="inactive"/>
                                <Label htmlFor="inactive">停用</Label>
                            </div>
                        </RadioGroup>
                        <FormMessage/>
                    </FormItem>
                )}
            />
        </div>
    )
}