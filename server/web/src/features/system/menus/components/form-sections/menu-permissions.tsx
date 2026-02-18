import type {UseFormReturn} from 'react-hook-form'
import {useState} from 'react'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Popover, PopoverContent, PopoverTrigger} from '@/components/ui/popover'
import {Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList} from '@/components/ui/command'
import {CheckIcon, ChevronsUpDownIcon} from 'lucide-react'
import {Button} from '@/components/ui/button'
import {cn} from '@/lib/utils'
import {PERMISSIONS} from '@/constants/permissions'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'

interface MenuPermissionsProps {
    form: UseFormReturn<CreateMenuFormData>
    selectedType: string
}

// Extract all permission values from the nested PERMISSIONS object
function getAllPermissions(): string[] {
    const extractPermissions = (obj: any): string[] => {
        const result: string[] = []
        for (const value of Object.values(obj)) {
            if (typeof value === 'string') {
                result.push(value)
            } else if (typeof value === 'object' && value !== null) {
                result.push(...extractPermissions(value))
            }
        }
        return result
    }
    return extractPermissions(PERMISSIONS)
}

export function MenuPermissions({form, selectedType}: MenuPermissionsProps) {
    const [open, setOpen] = useState(false)
    const allPermissions = getAllPermissions()

    if (selectedType !== 'button') {
        return null
    }

    return (
        <div className="space-y-4">
            <FormField
                control={form.control}
                name="permissions"
                render={({field}) => {
                    const isValidPermission = !field.value || allPermissions.includes(field.value)

                    return (
                        <FormItem className="flex flex-col">
                            <FormLabel>权限标识</FormLabel>
                            <Popover open={open} onOpenChange={setOpen}>
                                <PopoverTrigger asChild>
                                    <FormControl>
                                        <Button
                                            variant="outline"
                                            role="combobox"
                                            aria-expanded={open}
                                            className={cn(
                                                "w-full justify-between",
                                                !field.value && "text-muted-foreground",
                                                !isValidPermission && "border-red-500 focus-visible:ring-red-500"
                                            )}
                                        >
                                            {field.value || "选择权限标识..."}
                                            <ChevronsUpDownIcon className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                        </Button>
                                    </FormControl>
                                </PopoverTrigger>
                                <PopoverContent className="w-full p-0" align="start">
                                    <Command>
                                        <CommandInput
                                            placeholder="搜索权限标识..."
                                            value={field.value}
                                            onValueChange={(value) => {
                                                field.onChange(value)
                                                // Auto-close popover when exact match is selected
                                                if (allPermissions.includes(value)) {
                                                    setOpen(false)
                                                }
                                            }}
                                        />
                                        <CommandList>
                                            <CommandEmpty>未找到权限标识</CommandEmpty>
                                            <CommandGroup>
                                                {allPermissions
                                                    .filter(permission =>
                                                        permission.toLowerCase().includes((field.value || '').toLowerCase())
                                                    )
                                                    .map((permission) => (
                                                        <CommandItem
                                                            key={permission}
                                                            value={permission}
                                                            onSelect={(value) => {
                                                                field.onChange(value)
                                                                setOpen(false)
                                                            }}
                                                        >
                                                            <CheckIcon
                                                                className={cn(
                                                                    "mr-2 h-4 w-4",
                                                                    field.value === permission ? "opacity-100" : "opacity-0"
                                                                )}
                                                            />
                                                            {permission}
                                                        </CommandItem>
                                                    ))}
                                            </CommandGroup>
                                        </CommandList>
                                    </Command>
                                </PopoverContent>
                            </Popover>
                            {!isValidPermission && field.value && (
                                <p className="text-sm text-red-500">权限标识不存在于预定义列表中</p>
                            )}
                            <FormMessage />
                        </FormItem>
                    )
                }}
            />
        </div>
    )
}