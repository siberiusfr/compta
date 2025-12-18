import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

/**
 * Middleware for logging navigation events
 */
export function loggingMiddleware(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  // Only log in development
  if (!import.meta.env.DEV) {
    return next()
  }

  const timestamp = new Date().toISOString()
  const fromPath = from.fullPath || 'initial'
  const toPath = to.fullPath

  console.group(`[Router] ${timestamp}`)
  console.log('From:', fromPath)
  console.log('To:', toPath)
  console.log('Meta:', to.meta)
  console.groupEnd()

  next()
}
