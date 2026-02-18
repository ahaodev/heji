import {useQuery} from '@tanstack/react-query'
import {getMenuTree} from '@/services/menuApi'
import type {MenuTreeNode} from '@/types/menu'


interface UseMenuDataProps {
    open: boolean
}

export function useMenuData({open}: UseMenuDataProps) {

    const {data: parentMenuOptions} = useQuery({
        queryKey: ['parent-menus'],
        queryFn: async () => {
            const menuTree = await getMenuTree('active')

            const flattenMenus = (nodes: MenuTreeNode[], result: Array<{ id: string; name: string }> = []): Array<{
                id: string;
                name: string
            }> => {
                nodes.forEach((node: MenuTreeNode) => {
                    if (node.type === 'menu') {
                        result.push({
                            id: node.id,
                            name: node.name
                        })
                        if (node.children) {
                            flattenMenus(node.children, result)
                        }
                    }
                })
                return result
            }

            const parentOptions = flattenMenus(menuTree)
            return [{id: 'ROOT', name: '主类目'}, ...parentOptions]
        },
        enabled: open,
        staleTime: 5 * 60 * 1000,
    })


    return {
        parentMenuOptions,
    }
}