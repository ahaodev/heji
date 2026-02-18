export interface ApiResource {
  id: string
  method: string
  path: string
  handler: string
  module?: string
  is_public: boolean
  created_at: string
  updated_at: string
}