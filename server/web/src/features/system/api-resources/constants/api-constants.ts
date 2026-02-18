// HTTP Method configurations
export const HTTP_METHODS = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
  PATCH: 'PATCH',
} as const

export type HttpMethod = typeof HTTP_METHODS[keyof typeof HTTP_METHODS]

// Method color mappings for badges
export const METHOD_COLORS = {
  GET: 'bg-green-100 text-green-800',
  POST: 'bg-blue-100 text-blue-800',
  PUT: 'bg-yellow-100 text-yellow-800',
  DELETE: 'bg-red-100 text-red-800',
  PATCH: 'bg-purple-100 text-purple-800',
} as const

// Filter options for method selection
export const METHOD_OPTIONS = [
  { label: 'GET', value: HTTP_METHODS.GET },
  { label: 'POST', value: HTTP_METHODS.POST },
  { label: 'PUT', value: HTTP_METHODS.PUT },
  { label: 'DELETE', value: HTTP_METHODS.DELETE },
  { label: 'PATCH', value: HTTP_METHODS.PATCH },
]