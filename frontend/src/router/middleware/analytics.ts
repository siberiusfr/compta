import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

/**
 * Middleware for tracking page views and analytics
 */
export function analyticsMiddleware(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  // Skip analytics in development
  if (import.meta.env.DEV) {
    return next()
  }

  // Track page view
  trackPageView(to)

  next()
}

function trackPageView(route: RouteLocationNormalized) {
  const pageTitle = route.meta.title || route.name || 'Unknown Page'
  const pagePath = route.fullPath

  // Example: Google Analytics
  if (typeof window !== 'undefined' && (window as any).gtag) {
    ;(window as any).gtag('config', 'GA_MEASUREMENT_ID', {
      page_title: pageTitle,
      page_path: pagePath,
    })
  }

  // Example: Custom analytics
  console.log('[Analytics] Page view:', {
    title: pageTitle,
    path: pagePath,
    timestamp: new Date().toISOString(),
  })

  // TODO: Replace with your actual analytics service
  // analyticsService.trackPageView({ title: pageTitle, path: pagePath })
}
