import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

/**
 * Middleware to check if app is in maintenance mode
 */
export function maintenanceMiddleware(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  // Check if maintenance mode is enabled
  const isMaintenanceMode = import.meta.env.VITE_MAINTENANCE_MODE === 'true'

  // Allow certain routes even in maintenance mode
  const allowedRoutes = ['maintenance', 'login']

  if (isMaintenanceMode && !allowedRoutes.includes(to.name as string)) {
    // Redirect to maintenance page
    return next({ name: 'maintenance' })
  }

  next()
}
