/**
 * Build URLSearchParams from a flat params object, skipping null/undefined/empty values.
 */
export function buildSearchParams(params?: object): URLSearchParams {
  const searchParams = new URLSearchParams()
  if (!params) return searchParams

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.append(key, String(value))
    }
  }
  return searchParams
}
