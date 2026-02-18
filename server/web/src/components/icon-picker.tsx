import {useState} from 'react'
import {Search, X} from 'lucide-react'
import {availableIconNames, availableIcons} from '@/lib/icons.ts'
import {Button} from '@/components/ui/button.tsx'
import {Input} from '@/components/ui/input.tsx'
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from '@/components/ui/dialog.tsx'
import {cn} from '@/lib/utils.ts'

// Use centralized available icons
const allIcons = availableIconNames

interface IconPickerProps {
    value?: string
    onChange: (icon: string) => void
    placeholder?: string
    className?: string
}

export function IconPicker({value, onChange, placeholder = "选择图标", className}: IconPickerProps) {
    const [open, setOpen] = useState(false)
    const [searchTerm, setSearchTerm] = useState('')

    // Filter icons based on search term - add safety check
    const filteredIcons = (allIcons || []).filter(iconName =>
        iconName && iconName.toLowerCase().includes(searchTerm.toLowerCase())
    )

    const handleIconSelect = (iconName: string) => {
        if (iconName && onChange) {
            onChange(iconName)
            setOpen(false)
        }
    }

    const handleClear = () => {
        if (onChange) {
            onChange('')
        }
    }

    // Get the selected icon component - add safety check
    const SelectedIcon = value && availableIcons[value as keyof typeof availableIcons]
        ? availableIcons[value as keyof typeof availableIcons]
        : null

    return (
        <div className={cn("relative", className)}>
            <Dialog open={open} onOpenChange={setOpen}>
                <DialogTrigger asChild>
                    <Button
                        variant="outline"
                        role="combobox"
                        className="w-full justify-between h-10"
                    >
                        <div className="flex items-center space-x-2">
                            {SelectedIcon ? (
                                <>
                                    <SelectedIcon className="h-4 w-4"/>
                                    <span>{value}</span>
                                </>
                            ) : (
                                <span className="text-muted-foreground">{placeholder}</span>
                            )}
                        </div>
                        <Search className="ml-2 h-4 w-4 shrink-0 opacity-50"/>
                    </Button>
                </DialogTrigger>

                <DialogContent className="sm:max-w-[700px] h-[650px] flex flex-col">
                    <DialogHeader className="flex-shrink-0">
                        <DialogTitle>选择图标</DialogTitle>
                    </DialogHeader>

                    <div className="flex flex-col flex-1 min-h-0">
                        {/* Fixed search area */}
                        <div className="flex-shrink-0 space-y-4 pb-4">
                            {/* Search input */}
                            <div className="relative">
                                <Search
                                    className="absolute left-2 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground"/>
                                <Input
                                    placeholder="搜索图标..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-8"
                                />
                            </div>

                            {/* Selected icon display and clear button - fixed height */}
                            <div className="h-16">
                                {value && (
                                    <div className="flex items-center justify-between p-3 bg-muted rounded-lg h-full">
                                        <div className="flex items-center space-x-2">
                                            {SelectedIcon && <SelectedIcon className="h-5 w-5"/>}
                                            <span className="font-medium">已选择: {value}</span>
                                        </div>
                                        <Button
                                            variant="ghost"
                                            size="sm"
                                            onClick={handleClear}
                                            className="h-8 w-8 p-0"
                                        >
                                            <X className="h-4 w-4"/>
                                        </Button>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Scrollable icons grid - takes remaining space */}
                        <div className="flex-1 overflow-y-auto">
                            <div className="grid grid-cols-10 gap-2">
                                {filteredIcons.map((iconName) => {
                                    const IconComponent = availableIcons[iconName as keyof typeof availableIcons]
                                    const isSelected = value === iconName

                                    return (
                                        <Button
                                            key={iconName}
                                            variant={isSelected ? "default" : "ghost"}
                                            className={cn(
                                                "h-12 w-12 p-2 flex flex-col items-center justify-center",
                                                isSelected && "bg-primary text-primary-foreground"
                                            )}
                                            onClick={() => handleIconSelect(iconName)}
                                            title={iconName}
                                        >
                                            <IconComponent className="h-5 w-5"/>
                                        </Button>
                                    )
                                })}
                            </div>

                            {filteredIcons.length === 0 && (
                                <div className="text-center py-8 text-muted-foreground">
                                    未找到匹配的图标
                                </div>
                            )}
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        </div>
    )
}