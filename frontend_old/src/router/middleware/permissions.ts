import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useOAuth2AuthStore } from '@/stores/oauth2Auth'

/**
 * Middleware to check user permissions/roles
 */
export function permissionsMiddleware(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const requiredRoles = to.meta.roles as string[] | undefined
  const requiredPermissions = to.meta.permissions as string[] | undefined

  // If no roles or permissions required, allow access
  if (!requiredRoles && !requiredPermissions) {
    return next()
  }

  const authStore = useOAuth2AuthStore()
  const userInfo = authStore.getUserInfo()

  // User not logged in - this should be handled by the auth guard, but check anyway
  if (!userInfo) {
    // Don't redirect here to avoid infinite loops - let the auth guard handle it
    return next()
  }

  // Check roles
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRole = requiredRoles.some((role) => userInfo.roles.includes(role))
    if (!hasRole) {
      console.warn(`Access denied. Required roles: ${requiredRoles.join(', ')}`)
      return next({ name: 'forbidden' })
    }
  }

  // Check permissions (if you have a permissions system)
  if (requiredPermissions && requiredPermissions.length > 0) {
    // TODO: Implement permissions check
    // const hasPermissions = requiredPermissions.every(permission =>
    //   userInfo.permissions?.includes(permission)
    // )
    // if (!hasPermissions) {
    //   return next({ name: 'forbidden' })
    // }
  }

  next()
}
