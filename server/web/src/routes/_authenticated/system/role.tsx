import { createFileRoute } from '@tanstack/react-router'
import z from 'zod'
import { Roles } from '@/features/system/roles'

const roleSearchSchema = z.object({
  page: z.number().min(1).optional().default(1),
  page_size: z.number().min(1).max(100).optional().default(20),
  role: z.string().optional(),
})

export const Route = createFileRoute('/_authenticated/system/role')({
  component: Roles,
  validateSearch: roleSearchSchema,
})
