import type {UseFormReturn} from 'react-hook-form'
import {FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {RadioGroup, RadioGroupItem} from '@/components/ui/radio-group'
import {Label} from '@/components/ui/label'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'

interface MenuFrameSettingsProps {
    form: UseFormReturn<CreateMenuFormData>
}

export function MenuFrameSettings({form}: MenuFrameSettingsProps) {
    return (
        <div className="grid grid-cols-2 gap-4">
            <FormField
                control={form.control}
                name="is_frame"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>是否外链</FormLabel>
                        <RadioGroup
                            onValueChange={(value) => field.onChange(value === 'true')}
                            value={field.value ? 'true' : 'false'}
                            className="flex space-x-4"
                        >
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="true" id="frame-yes"/>
                                <Label htmlFor="frame-yes">是</Label>
                            </div>
                            <div className="flex items-center space-x-2">
                                <RadioGroupItem value="false" id="frame-no"/>
                                <Label htmlFor="frame-no">否</Label>
                            </div>
                        </RadioGroup>
                        <FormMessage/>
                    </FormItem>
                )}
            />
        </div>
    )
}