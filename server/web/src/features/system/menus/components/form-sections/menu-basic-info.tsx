import type {UseFormReturn} from 'react-hook-form'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select'
import {RadioGroup, RadioGroupItem} from '@/components/ui/radio-group'
import {Label} from '@/components/ui/label'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'

interface MenuBasicInfoProps {
    form: UseFormReturn<CreateMenuFormData>
    parentMenuOptions: Array<{ id: string; name: string }> | undefined
}

export function MenuBasicInfo({form, parentMenuOptions}: MenuBasicInfoProps) {
    return (
        <div className="grid grid-cols-2 gap-4">
            <FormField
                control={form.control}
                name="parent_id"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>上级菜单</FormLabel>
                        <Select onValueChange={field.onChange} value={field.value}>
                            <FormControl>
                                <SelectTrigger>
                                    <SelectValue placeholder="主类目"/>
                                </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                                {parentMenuOptions?.map((menu: { id: string; name: string }) => (
                                    <SelectItem key={menu.id} value={menu.id}>
                                        {menu.name}
                                    </SelectItem>
                                )) || []}
                            </SelectContent>
                        </Select>
                        <FormMessage/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name="type"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>菜单类型</FormLabel>
                        <RadioGroup onValueChange={field.onChange} value={field.value}
                                    className="flex space-x-4">
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="menu" id="menu"/>
                                <Label htmlFor="menu">菜单</Label>
                            </div>
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="button" id="button"/>
                                <Label htmlFor="button">按钮</Label>
                            </div>
                        </RadioGroup>
                        <FormMessage/>
                    </FormItem>
                )}
            />
        </div>
    )
}