import { useQuery } from '@tanstack/react-query'
import { getBooks } from '@/services/bookApi'

const BOOKS_QUERY_KEY = 'books'

export function useBooks() {
  return useQuery({
    queryKey: [BOOKS_QUERY_KEY],
    queryFn: async () => {
      const books = await getBooks()
      return books
    },
    staleTime: 5 * 60 * 1000,
  })
}
