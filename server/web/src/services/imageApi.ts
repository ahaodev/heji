import type { BillImage } from '@/types/bill'
import { apiClient } from './config'

// GET /image/list?bill_id=xxx - 获取账单图片列表
export const getBillImages = async (billId: string): Promise<BillImage[]> => {
  const response = await apiClient.get('/api/v1/image/list', {
    params: { bill_id: billId },
  })
  return (response.data.data as BillImage[]) || []
}

// 获取图片URL（带认证）
export const getImageUrl = (imageId: string): string => {
  return `/api/v1/image/${imageId}`
}
