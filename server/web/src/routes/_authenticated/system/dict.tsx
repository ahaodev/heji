import { createFileRoute } from '@tanstack/react-router'
import z from 'zod'
import { Dicts } from '@/features/system/dicts'

const dictSearchSchema = z.object({
  page: z.number().min(1).optional().default(1),
  page_size: z.number().min(1).max(100).optional().default(20),
  status: z.enum(['active', 'inactive']).optional(),
  search: z.string().optional(),
  activeTypeId: z.string().optional(),
})

export const Route = createFileRoute('/_authenticated/system/dict')({
  component: Dicts,
  validateSearch: dictSearchSchema,
})
