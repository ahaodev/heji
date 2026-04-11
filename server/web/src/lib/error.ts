import { AxiosError } from 'axios'

/**
 * Extract error message from an unknown error (typically from API calls).
 * Supports AxiosError with `response.data.msg` and standard Error objects.
 */
export function getErrorMessage(
  error: unknown,
  fallback: string = '操作失败'
): string {
  if (error instanceof AxiosError) {
    return (
      ((error.response?.data as Record<string, unknown>)?.msg as string) ??
      fallback
    )
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}
