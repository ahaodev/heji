import { createFileRoute, redirect } from '@tanstack/react-router'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { useAuthStore } from '@/stores/auth-store'

export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ location }) => {
    const { auth } = useAuthStore.getState()
    
    if (!auth.accessToken) {
      throw redirect({
        to: '/sign-in',
        search: {
          redirect: location.href,
        },
      })
    }

    // Fetch profile if not already loaded
    if (!auth.profile && auth.accessToken) {
      try {
        await auth.fetchProfile()
      } catch (error) {
        console.error('Failed to fetch profile on route load:', error)
        // Don't redirect on profile fetch failure, user can still navigate
      }
    }
  },
  component: AuthenticatedLayout,
})
