import z from 'zod'
import {createFileRoute} from '@tanstack/react-router'
import {LoginLogs} from '@/features/system/login-logs'

const loginLogsSearchSchema = z.object({
    page: z.number().optional().catch(1),
    pageSize: z.number().optional().catch(10),
    // Per-column text filter
    username: z.string().optional().catch(''),
    // Status filter
    status: z
        .array(
            z.union([
                z.literal('success'),
                z.literal('failed'),
            ])
        )
        .optional()
        .catch([]),
})

export const Route = createFileRoute('/_authenticated/system/login-logs')({
    validateSearch: loginLogsSearchSchema,
    component: LoginLogs,
})