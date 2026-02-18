import { z } from 'zod'

const bookSchema = z.object({
  _id: z.string(),
  name: z.string(),
  type: z.string().optional(),
  banner: z.string().optional(),
  crt_user_id: z.string(),
  is_initial: z.boolean(),
  members: z.array(z.string()).optional(),
  deleted_at: z.string().optional(),
  crt_time: z.string(),
  upd_time: z.string(),
})
export type Book = z.infer<typeof bookSchema>

export const bookListSchema = z.array(bookSchema)

const billSchema = z.object({
  _id: z.string(),
  book_id: z.string(),
  money: z.number(),
  type: z.number(),
  category: z.string().optional(),
  crt_user: z.string(),
  crt_user_name: z.string().optional(),
  time: z.string(),
  remark: z.string().optional(),
  images: z.array(z.string()).optional(),
  deleted_at: z.string().optional(),
  crt_time: z.string(),
  upd_time: z.string(),
})
export type Bill = z.infer<typeof billSchema>

export const billListSchema = z.array(billSchema)
