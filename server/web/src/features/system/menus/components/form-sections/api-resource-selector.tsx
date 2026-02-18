import {useState} from 'react'
import {createPortal} from 'react-dom'
import {Button} from '@/components/ui/button'
import {Badge} from '@/components/ui/badge'
import {Checkbox} from '@/components/ui/checkbox'
import {
    CommandDialog,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from '@/components/ui/command'
import {
    DropdownMenu,
    DropdownMenuItem,
    DropdownMenuContent,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {ChevronDown, Filter} from 'lucide-react'
import type {ApiResource} from '@/types/api-resource'
import {METHOD_COLORS, METHOD_OPTIONS} from '@/features/system/api-resources/constants/api-constants'

interface ApiResourceSelectorProps {
    apiResources: ApiResource[]
    isLoading: boolean
    selectedApiResources: string[] // 改为使用菜单中的 apiResources 字段
    onResourceSelect: (resource: ApiResource, checked: boolean) => void
    isResourceSelected: (resource: ApiResource) => boolean
}

export function ApiResourceSelector({
                                        apiResources,
                                        isLoading,
                                        selectedApiResources,
                                        onResourceSelect,
                                        isResourceSelected,
                                    }: ApiResourceSelectorProps) {
    const [open, setOpen] = useState(false)
    const [selectedMethod, setSelectedMethod] = useState<string>('ALL')

    // 辅助函数：检查资源是否被选中
    const checkIsResourceSelected = (resource: ApiResource) => {
        // Check both direct ID match and method:path format
        const methodPathFormat = `${resource.method}:${resource.path}`
        return selectedApiResources.includes(resource.id) ||
            selectedApiResources.includes(methodPathFormat) ||
            isResourceSelected(resource)
    }

    const filteredApiResources = apiResources.filter(resource =>
        selectedMethod === 'ALL' || resource.method === selectedMethod
    )

    const groupedApiResources = filteredApiResources.reduce((acc, resource) => {
        const module = resource.module || '其他'
        if (!acc[module]) {
            acc[module] = []
        }
        acc[module].push(resource)
        return acc
    }, {} as Record<string, ApiResource[]>)

    return (
        <>
            <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={isLoading}
                onClick={() => setOpen(true)}
            >
                {isLoading ? '加载中...' : '选择'}
            </Button>

            {typeof window !== 'undefined' && createPortal(
                <CommandDialog open={open} onOpenChange={setOpen}>
                    <div className="p-4">
                        <CommandInput placeholder="搜索API资源..." className="w-full"/>
                    </div>
                    <div className="flex items-center justify-start p-3">
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button type="button" variant="outline" size="sm"
                                        className="h-8 flex items-center space-x-1">
                                    <Filter className="h-4 w-4"/>
                                    <Badge
                                        className={`text-xs ${selectedMethod === 'ALL' ? 'bg-gray-100 text-gray-800' : METHOD_COLORS[selectedMethod as keyof typeof METHOD_COLORS]}`}
                                    >
                                        {selectedMethod === 'ALL' ? '全部' : selectedMethod}
                                    </Badge>
                                    <ChevronDown className="h-4 w-4"/>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="start" className="w-[200px]">
                                <DropdownMenuItem
                                    onClick={() => setSelectedMethod('ALL')}
                                    className={selectedMethod === 'ALL' ? 'bg-accent' : ''}
                                >
                                    <Badge className="text-xs bg-gray-100 text-gray-800">
                                        全部
                                    </Badge>
                                </DropdownMenuItem>
                                {METHOD_OPTIONS.map((option) => (
                                    <DropdownMenuItem
                                        key={option.value}
                                        onClick={() => setSelectedMethod(option.value)}
                                        className={selectedMethod === option.value ? 'bg-accent' : ''}
                                    >
                                        <Badge
                                            className={`text-xs ${METHOD_COLORS[option.value as keyof typeof METHOD_COLORS]}`}
                                        >
                                            {option.label}
                                        </Badge>
                                    </DropdownMenuItem>
                                ))}
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                    <CommandList>
                        <CommandEmpty>没有找到相关API资源</CommandEmpty>
                        {Object.entries(groupedApiResources).map(([module, moduleResources]) => (
                            <CommandGroup key={module} heading={module}>
                                {(moduleResources as ApiResource[])
                                    .filter((resource) => !resource.is_public)
                                    .map((resource) => (
                                        <CommandItem
                                            key={resource.id}
                                            value={`${resource.path} ${resource.method} ${module}`}
                                            className="flex items-start space-x-2 p-3 cursor-pointer"
                                            onSelect={(e) => {
                                                // Prevent default command item behavior
                                                console.log(e)
                                            }}
                                        >
                                            <Checkbox
                                                className="h-5 w-5"
                                                checked={checkIsResourceSelected(resource)}
                                                onCheckedChange={(checked) => {
                                                    onResourceSelect(resource, !!checked)
                                                }}
                                            />
                                            <div className="flex-1 space-y-1">
                                                <div className="flex items-center space-x-2">
                                                    <span className="text-sm font-medium">{resource.path}</span>
                                                    <Badge
                                                        className={`text-xs ${METHOD_COLORS[resource.method as keyof typeof METHOD_COLORS] || 'bg-gray-100 text-gray-800'}`}
                                                    >
                                                        {resource.method}
                                                    </Badge>
                                                </div>
                                            </div>
                                        </CommandItem>
                                    ))}
                            </CommandGroup>
                        ))}
                    </CommandList>
                </CommandDialog>,
                document.body
            )}
        </>
    )
}