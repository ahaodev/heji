import {z} from 'zod'

const userStatusSchema = z.union([
    z.literal('active'),
    z.literal('inactive'),
    z.literal('invited'),
    z.literal('suspended'),
])
export type UserStatus = z.infer<typeof userStatusSchema>

const userSchema = z.object({
    id: z.string(),
    username: z.string(),
    email: z.string(),
    phone: z.string().optional(),
    avatar: z.string().optional(),
    status: userStatusSchema,
    created_at: z.coerce.date(),
    updated_at: z.coerce.date(),
    invited_at: z.coerce.date().optional(),
    invited_by: z.string().optional(),
    roles: z.array(z.string()).optional(),
})
export type User = z.infer<typeof userSchema>

export const userListSchema = z.array(userSchema)
