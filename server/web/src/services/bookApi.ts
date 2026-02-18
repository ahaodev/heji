import type { Book } from '@/types/book'
import { apiClient } from './config'

// GET /book - 获取当前用户的账本列表
export const getBooks = async (): Promise<Book[]> => {
  const response = await apiClient.get('/api/v1/book')
  return response.data.data as Book[]
}

// GET /book/:id - 获取账本详情
export const getBook = async (id: string): Promise<Book> => {
  const response = await apiClient.get(`/api/v1/book/${id}`)
  return response.data.data as Book
}
