import {z} from 'zod'

export const createMenuSchema = z.object({
    name: z.string().min(1, '名称是必填的'),
    sequence: z.number().min(0, '序号必须大于等于0'),
    type: z.enum(['menu', 'button']),
    path: z.string().optional(),
    icon: z.string().optional(),
    component: z.string().optional(),
    route_name: z.string().optional(),
    query: z.string().optional(),
    is_frame: z.boolean(),
    visible: z.enum(['show', 'hide']),
    permissions: z.string().optional(),
    status: z.enum(['active', 'inactive']),
    parent_id: z.string().optional(),
    apiResources: z.array(z.string()).optional(),
})

export type CreateMenuFormData = z.infer<typeof createMenuSchema>

export const defaultFormValues: CreateMenuFormData = {
    name: '',
    sequence: 0,
    type: 'menu' as const,
    path: '',
    icon: '',
    component: '',
    route_name: '',
    query: '',
    is_frame: false,
    visible: 'show' as const,
    permissions: '',
    status: 'active' as const,
    parent_id: '',
    apiResources: [],
}