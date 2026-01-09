import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export async function authGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> {
  const authStore = useAuthStore()

  // Wait for auth initialization with timeout
  const maxWait = 5000
  const startTime = Date.now()

  while (authStore.isLoading && Date.now() - startTime < maxWait) {
    await new Promise(resolve => setTimeout(resolve, 50))
  }

  // Skip auth check for callback routes
  if (to.meta.isCallback) {
    return next()
  }

  // Check if route requires authentication
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Store the return URL for post-login redirect
    sessionStorage.setItem('returnUrl', to.fullPath)
    return next({ name: 'home' })
  }

  // Check if route requires specific roles
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    const userRoles = authStore.userProfile?.roles as string[] ?? []
    const hasRequiredRole = to.meta.roles.some(role => userRoles.includes(role))

    if (!hasRequiredRole) {
      return next({ name: 'unauthorized' })
    }
  }

  // Redirect authenticated users away from auth pages
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return next({ name: 'dashboard' })
  }

  next()
}
