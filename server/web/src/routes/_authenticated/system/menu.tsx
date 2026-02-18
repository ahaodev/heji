import { createFileRoute } from '@tanstack/react-router'
import z from 'zod'
import { Menus } from '@/features/system/menus'

const menuSearchSchema = z.object({
  page: z.number().min(1).optional(),
  page_size: z.number().min(1).max(1000).optional(),
  type: z.string().optional(),
  status: z.string().optional(),
  search: z.string().optional(),
})

export const Route = createFileRoute('/_authenticated/system/menu')({
  component: Menus,
  validateSearch: menuSearchSchema,
})
