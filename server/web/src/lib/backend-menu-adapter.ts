import {type MenuTreeNode} from '@/types/menu'
import {type NavGroup, type NavItem} from '@/components/layout/types'


/**
 * Convert a single MenuTreeNode to NavItem
 */
function convertMenuNodeToNavItem(menu: MenuTreeNode): NavItem | null {

    // Filter out hidden or inactive menu items
    if (menu.visible === 'hide' || menu.status === 'inactive') {
        console.log('Skipping menu item:', menu.name, 'due to visibility/status')
        return null
    }

    // Filter out button types - buttons should not be rendered in navigation menu
    if (menu.type === 'button') {
        console.log('Skipping button type:', menu.name, 'buttons are handled via permissions on pages')
        return null
    }


    // If menu has children, create a collapsible nav item
    if (menu.children && menu.children.length > 0) {
        const visibleChildren = menu.children
            .map(convertMenuNodeToNavItem)
            .filter((item): item is NavItem => item !== null)

        // If this menu item has a path and no visible children (all children are buttons),
        // render it as a single menu item instead of a group
        if (visibleChildren.length === 0 && menu.path) {
            console.log('Rendering menu item with no visible children as single item:', menu.name)
            return {
                title: menu.name,
                icon: menu.icon,
                url: menu.path,
            }
        }

        // Skip parent only if no visible children and no path
        if (visibleChildren.length === 0) {
            console.log('Skipping menu group:', menu.name, 'due to no visible children and no path')
            return null
        }

        return {
            title: menu.name,
            items: visibleChildren,
            icon: menu.icon,
        }
    }

    // Single menu item with URL
    return {
        title: menu.name,
        icon: menu.icon,
        url: menu.path,
    }
}

/**
 * BackendMenuAdapter class for converting backend menu data to frontend navigation structure
 */
export class BackendMenuAdapter {
    /**
     * Transform backend MenuTreeNode array to frontend NavGroup array
     */
    transformToNavGroups(menuNodes: MenuTreeNode[]): NavGroup[] {
        if (!menuNodes || menuNodes.length === 0) {
            console.log('BackendMenuAdapter: No menu nodes to transform')
            return []
        }

        console.log('BackendMenuAdapter: Transforming', menuNodes.length, 'menu nodes')

        const result = menuNodes
            .map(this.convertToNavGroup)
            .filter((group): group is NavGroup => group !== null)
        console.log('BackendMenuAdapter: Transformed to', result.length, 'nav groups')

        return result
    }

    /**
     * Convert a single MenuTreeNode to NavGroup
     */
    private convertToNavGroup(menu: MenuTreeNode): NavGroup | null {
        // Filter out hidden or inactive menu groups
        if (menu.visible === 'hide' || menu.status === 'inactive') {
            console.log('Skipping menu group:', menu.name, 'due to visibility/status')
            return null
        }

        // Filter out button types - buttons should not be rendered as navigation groups
        if (menu.type === 'button') {
            console.log('Skipping button type group:', menu.name, 'buttons are handled via permissions on pages')
            return null
        }

        // If this menu has children, treat them as nav items
        if (menu.children && menu.children.length > 0) {
            const visibleItems = menu.children
                .map(convertMenuNodeToNavItem)
                .filter((item): item is NavItem => item !== null)

            // If this menu item has a path and no visible children (all children are buttons),
            // render it as a single nav group item instead of a group with items
            if (visibleItems.length === 0 && menu.path) {
                console.log('Rendering menu group with no visible children as single item:', menu.name)
                return {
                    title: menu.name,
                    icon: menu.icon,
                    url: menu.path,
                }
            }

            // Skip group only if no visible items and no path
            if (visibleItems.length === 0) {
                console.log('Skipping menu group:', menu.name, 'due to no visible items and no path')
                return null
            }

            return {
                title: menu.name,
                icon: menu.icon,
                items: visibleItems,
            }
        }

        return {
            title: menu.name,
            icon: menu.icon,
            url: menu.path,
        }
    }
}