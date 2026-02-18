import type {Menu} from '@/types/menu'
import type {TableMenuItem} from '@/lib/menu-utils'

export function tableMenuItemToMenu(item: TableMenuItem): Menu {
    return {
        id: item.id,
        name: item.name,
        sequence: item.sequence,
        type: item.type,
        path: item.path,
        icon: item.icon,
        component: item.component,
        route_name: item.route_name,
        query: item.query,
        is_frame: item.is_frame,
        visible: item.visible,
        permissions: item.permissions,
        status: item.status,
        parent_id: item.parent_id
    }
}