import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@stores'

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

  const authStore = useAuthStore()
  const user = authStore.user

  // User not logged in
  if (!user) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  // Check roles
  if (requiredRoles && requiredRoles.length > 0) {
    if (!requiredRoles.includes(user.role)) {
      console.warn(`Access denied. Required roles: ${requiredRoles.join(', ')}`)
      return next({ name: 'forbidden' })
    }
  }

  // Check permissions (if you have a permissions system)
  if (requiredPermissions && requiredPermissions.length > 0) {
    // TODO: Implement permissions check
    // const hasPermissions = requiredPermissions.every(permission =>
    //   user.permissions?.includes(permission)
    // )
    // if (!hasPermissions) {
    //   return next({ name: 'forbidden' })
    // }
  }

  next()
}
