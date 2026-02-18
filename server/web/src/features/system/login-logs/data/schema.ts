import {z} from 'zod'

const loginLogStatusSchema = z.union([
    z.literal('success'),
    z.literal('failed'),
])

const loginLogSchema = z.object({
    id: z.string(),
    username: z.string(),
    login_ip: z.string(),
    user_agent: z.string(),
    browser: z.string().optional(),
    os: z.string().optional(),
    device: z.string().optional(),
    status: loginLogStatusSchema,
    failure_reason: z.string().optional(),
    login_time: z.coerce.date(),
})
export type LoginLog = z.infer<typeof loginLogSchema>

export const loginLogListSchema = z.array(loginLogSchema)

// 分页响应schema
const paginatedLoginLogsResponseSchema = z.object({
    list: loginLogListSchema.nullable(),
    total: z.number(),
    page: z.number(),
    page_size: z.number(),
    total_pages: z.number(),
})
export type PaginatedLoginLogsResponse = z.infer<typeof paginatedLoginLogsResponseSchema>

// 查询过滤器schema
export const loginLogFilterSchema = z.object({
    page: z.number().optional(),
    page_size: z.number().optional(),
    username: z.string().optional(),
    login_ip: z.string().optional(),
    status: loginLogStatusSchema.optional(),
    browser: z.string().optional(),
    os: z.string().optional(),
    start_time: z.string().optional(),
    end_time: z.string().optional(),
    sort_by: z.string().optional(),
    order: z.union([z.literal('asc'), z.literal('desc')]).optional(),
})
export type LoginLogFilter = z.infer<typeof loginLogFilterSchema>