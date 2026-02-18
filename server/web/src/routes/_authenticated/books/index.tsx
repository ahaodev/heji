import { createFileRoute } from '@tanstack/react-router'
import { Books } from '@/features/books'

export const Route = createFileRoute('/_authenticated/books/')({
  component: Books,
})
