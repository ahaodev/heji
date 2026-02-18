import z from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { BookBills } from '@/features/books/bills'

const billsSearchSchema = z.object({
  page: z.number().optional().catch(1),
  pageSize: z.number().optional().catch(10),
  type: z
    .array(z.union([z.literal('1'), z.literal('-1')]))
    .optional()
    .catch([]),
})

export const Route = createFileRoute('/_authenticated/books/$bookId')({
  validateSearch: billsSearchSchema,
  component: BookBills,
})
