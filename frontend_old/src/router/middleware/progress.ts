import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

let loadingBar: any = null

/**
 * Middleware to show loading bar during navigation
 */
export function progressMiddleware(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  // Start loading bar
  if (loadingBar) {
    loadingBar.start()
  }

  next()
}

/**
 * Initialize loading bar reference
 */
export function initProgressBar(bar: any) {
  loadingBar = bar
}

/**
 * Finish loading bar
 */
export function finishProgress() {
  if (loadingBar) {
    loadingBar.finish()
  }
}

/**
 * Error loading bar
 */
export function errorProgress() {
  if (loadingBar) {
    loadingBar.error()
  }
}
