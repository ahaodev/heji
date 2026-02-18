import React from 'react'
import {ChevronDown, ChevronRight} from 'lucide-react'
import {Button} from '@/components/ui/button'
import {FormLabel} from '@/components/ui/form'
import {Checkbox} from '@/components/ui/checkbox'
import {Badge} from '@/components/ui/badge'
import {ScrollArea} from '@/components/ui/scroll-area'
import type {MenuItem} from '@/lib/menu-utils'

interface MenuTreeItemProps {
    item: MenuItem
    level?: number
    expanded: Set<string>
    selected: Set<string>
    onToggle: (id: string) => void
    onSelect: (id: string, checked: boolean) => void
}

const MenuTreeItem = React.memo(({
                                     item,
                                     level = 0,
                                     expanded,
                                     selected,
                                     onToggle,
                                     onSelect
                                 }: MenuTreeItemProps) => {
    const hasChildren = !!item.children?.length
    const isExpanded = expanded.has(item.id)
    const isSelected = selected.has(item.id)

    return (
        <div className="space-y-1">
            <div className="flex items-center space-x-2 py-1 px-2 hover:bg-muted rounded-sm"
                 style={{marginLeft: level * 24}}>
                {hasChildren ? (
                    <Button variant="ghost" size="sm" className="h-4 w-4 p-0" type="button"
                            onClick={() => onToggle(item.id)}>
                        {isExpanded ? <ChevronDown className="h-3 w-3"/> : <ChevronRight className="h-3 w-3"/>}
                    </Button>
                ) : <div className="w-4"/>}
                <Checkbox
                    id={`menu-${item.id}`}
                    checked={isSelected}
                    onCheckedChange={(checked) => onSelect(item.id, !!checked)}
                    disabled={item.disabled}
                />
                <label htmlFor={`menu-${item.id}`}
                       className={`text-sm cursor-pointer ${item.disabled ? 'text-muted-foreground' : ''}`}>
                    {item.label}
                </label>
                {level === 0 && <Badge variant="outline" className="text-xs">L1</Badge>}
                {level === 1 && <Badge variant="outline" className="text-xs">L2</Badge>}
                {level === 2 && <Badge variant="outline" className="text-xs">L3</Badge>}
            </div>
            {hasChildren && isExpanded && item.children!.map(child => (
                <MenuTreeItem
                    key={child.id}
                    item={child}
                    level={level + 1}
                    expanded={expanded}
                    selected={selected}
                    onToggle={onToggle}
                    onSelect={onSelect}
                />
            ))}
        </div>
    )
})

MenuTreeItem.displayName = 'MenuTreeItem'

interface MenuTreeProps {
    items: MenuItem[]
    expanded: Set<string>
    selected: Set<string>
    onToggle: (id: string) => void
    onSelect: (id: string, checked: boolean) => void
}

const MenuTree = React.memo(({items, expanded, selected, onToggle, onSelect}: MenuTreeProps) => {
    return (
        <>
            {items.map(item => (
                <MenuTreeItem
                    key={item.id}
                    item={item}
                    expanded={expanded}
                    selected={selected}
                    onToggle={onToggle}
                    onSelect={onSelect}
                />
            ))}
        </>
    )
})

MenuTree.displayName = 'MenuTree'

interface MenuTreeSectionProps {
    menusData: MenuItem[]
    expandedNodes: Set<string>
    selectedMenus: Set<string>
    onToggle: (id: string) => void
    onSelect: (id: string, checked: boolean) => void
    onSelectAll: () => void
    onExpandRoot: () => void
    className?: string
}

export function MenuTreeSection({
                                    menusData,
                                    expandedNodes,
                                    selectedMenus,
                                    onToggle,
                                    onSelect,
                                    onSelectAll,
                                    onExpandRoot,
                                    className
                                }: MenuTreeSectionProps) {
    return (
        <div className={`flex flex-col h-full ${className || ''}`}>
            <div className="flex items-center justify-between mb-4 flex-shrink-0">
                <div className="flex items-center gap-2">
                    <FormLabel>菜单权限</FormLabel>
                    {selectedMenus.size > 0 && (
                        <Badge variant="secondary" className="text-xs">
                            已选择 {selectedMenus.size} 个
                        </Badge>
                    )}
                </div>
                <div className="flex space-x-2">
                    <Button type="button" variant="outline" size="sm" onClick={onSelectAll}>
                        {selectedMenus.size ? '全不选' : '全选'}
                    </Button>
                    <Button type="button" variant="outline" size="sm" onClick={onExpandRoot}>
                        {expandedNodes.size ? '折叠' : '展开'}
                    </Button>
                </div>
            </div>
            <div className="border rounded-md flex-1 min-h-0 overflow-hidden">
                <ScrollArea className="h-full p-4">
                    <MenuTree
                        items={menusData}
                        expanded={expandedNodes}
                        selected={selectedMenus}
                        onToggle={onToggle}
                        onSelect={onSelect}
                    />
                </ScrollArea>
            </div>
        </div>
    )
}