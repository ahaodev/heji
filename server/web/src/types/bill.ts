// Bill 账单实体
export interface Bill {
  _id: string
  book_id: string
  money: number
  type: number // 1=收入, -1=支出
  category?: string
  crt_user: string
  crt_user_name?: string
  time: string
  remark?: string
  images?: string[]
  deleted_at?: string
  crt_time: string
  upd_time: string
}

// BillImage 账单图片实体
export interface BillImage {
  _id: string
  bill_id: string
  key: string
  ext?: string
  length?: number
  md5?: string
}
