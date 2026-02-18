import {z} from 'zod'

// API 对齐的基础 Schema
export const dictTypeSchema = z.object({
    id: z.string(),
    code: z.string(),
    name: z.string(),
    status: z.enum(['active', 'inactive']),
    remark: z.string().optional(),
    created_at: z.date(),
    updated_at: z.date(),
})

export const dictItemSchema = z.object({
    id: z.string(),
    type_id: z.string(),
    label: z.string(),
    value: z.string(),
    sort: z.number(),
    is_default: z.boolean(),
    status: z.enum(['active', 'inactive']),
    color: z.string().optional(),
    remark: z.string().optional(),
    created_at: z.date(),
    updated_at: z.date(),
})

export type DictType = z.infer<typeof dictTypeSchema>
export type DictItem = z.infer<typeof dictItemSchema>