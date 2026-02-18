import { createFileRoute } from '@tanstack/react-router'
import z from 'zod'
import { ApiResources } from '@/features/system/api-resources'

const apiResourcesSearchSchema = z.object({
  page: z.number().min(1).optional(),
  page_size: z.number().min(1).max(1000).optional(),
  method: z.string().optional(),
  module: z.union([z.string(), z.array(z.string())]).optional(),
  status: z.string().optional(),
  path: z.string().optional(),
})

export const Route = createFileRoute('/_authenticated/system/api-resources')({
  component: ApiResources,
  validateSearch: apiResourcesSearchSchema,
})
