import {useState} from 'react'
import {Check, ChevronDown} from 'lucide-react'
import {cn} from '@/lib/utils'
import {Button} from '@/components/ui/button'
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,} from '@/components/ui/dropdown-menu'
import {Checkbox} from '@/components/ui/checkbox'

type MultiSelectDropdownProps = {
    onValueChange?: (values: string[]) => void
    value?: string[]
    placeholder?: string
    items: { label: string; value: string }[]
    disabled?: boolean
    className?: string
}

export function MultiSelectDropdown({
                                        value = [],
                                        onValueChange,
                                        items,
                                        placeholder = '选择选项',
                                        disabled,
                                        className = '',
                                    }: MultiSelectDropdownProps) {
    const [open, setOpen] = useState(false)

    const handleToggleItem = (itemValue: string) => {
        const isSelected = value.includes(itemValue)
        const newValue = isSelected
            ? value.filter((v) => v !== itemValue)
            : [...value, itemValue]
        onValueChange?.(newValue)
    }

    const getDisplayValue = () => {
        if (value.length === 0) return placeholder
        const selectedLabels = value
            .map((v) => items.find((item) => item.value === v)?.label || v)
            .join(', ')
        return selectedLabels
    }

    return (
        <DropdownMenu open={open} onOpenChange={setOpen}>
            <DropdownMenuTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    className={cn(
                        'w-full justify-between font-normal',
                        value.length === 0 && 'text-muted-foreground',
                        className
                    )}
                    disabled={disabled}
                >
                    <span className="truncate">{getDisplayValue()}</span>
                    <ChevronDown className="ml-2 h-4 w-4 shrink-0 opacity-50"/>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56" align="start">
                {items.map((item) => {
                    const isSelected = value.includes(item.value)
                    return (
                        <DropdownMenuItem
                            key={item.value}
                            className="flex items-center space-x-2 cursor-pointer"
                            onSelect={(e) => {
                                e.preventDefault()
                                handleToggleItem(item.value)
                            }}
                        >
                            <Checkbox
                                checked={isSelected}
                                onCheckedChange={() => handleToggleItem(item.value)}
                                className="pointer-events-none"
                            />
                            <span className="flex-1">{item.label}</span>
                            {isSelected && (
                                <Check className="h-4 w-4 text-primary"/>
                            )}
                        </DropdownMenuItem>
                    )
                })}
            </DropdownMenuContent>
        </DropdownMenu>
    )
}