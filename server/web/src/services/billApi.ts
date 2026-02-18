import type { Bill } from '@/types/bill'
import type { PagedResult, QueryParams } from '@/types/api'
import { apiClient } from './config'

// GET /bill?book_id=xxx - 查询账单列表（分页）
export const getBills = async (
  bookId: string,
  params?: QueryParams
): Promise<PagedResult<Bill>> => {
  const response = await apiClient.get('/api/v1/bill', {
    params: { book_id: bookId, ...params },
  })
  return response.data.data as PagedResult<Bill>
}

// GET /bill/:id - 获取账单详情
export const getBill = async (id: string): Promise<Bill> => {
  const response = await apiClient.get(`/api/v1/bill/${id}`)
  return response.data.data as Bill
}
