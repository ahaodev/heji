// Book 账本实体
export interface Book {
  _id: string
  name: string
  type?: string
  banner?: string
  crt_user_id: string
  is_initial: boolean
  members?: string[]
  deleted_at?: string
  crt_time: string
  upd_time: string
}
