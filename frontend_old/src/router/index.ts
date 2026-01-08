import { createRouter, createWebHistory } from 'vue-router'
import { authRoutes } from '@modules/auth/routes'
import { accountingRoutes } from '@modules/accounting/routes'
import { hrRoutes } from '@modules/hr/routes'
import { useOAuth2AuthStore } from '@/stores/oauth2Auth'
import {
  loggingMiddleware,
  analyticsMiddleware,
  permissionsMiddleware,
  progressMiddleware,
  finishProgress,
  errorProgress,
} from './middleware'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/accounting',
  },
  {
    path: '/authorized',
    name: 'authorized',
    component: () => import('@/views/AuthorizedView.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  ...authRoutes,
  ...accountingRoutes,
  ...hrRoutes,
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('@/views/Forbidden.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/500',
    name: 'server-error',
    component: () => import('@/views/ServerError.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// Apply middlewares in order
router.beforeEach(loggingMiddleware)
router.beforeEach(progressMiddleware)

// Navigation guard for authentication
router.beforeEach(async (to, from, next) => {
  const authStore = useOAuth2AuthStore()
  const requiresAuth = to.meta.requiresAuth ?? true

  if (requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Vérifier si on a un token valide
      const isAuth = await authStore.checkAuth()
      if (!isAuth) {
        // Rediriger vers login avec l'URL de retour
        next({
          name: 'login',
          query: { redirect: to.fullPath }
        })
        return
      }
    }
  } else if (to.name === 'login' && authStore.isAuthenticated) {
    next({ name: 'accounting' })
    return
  }

  next()
})

// Permission/role check
router.beforeEach(permissionsMiddleware)

// Analytics tracking
router.beforeEach(analyticsMiddleware)

// After navigation
router.afterEach(() => {
  finishProgress()
})

// Navigation error handler
router.onError(() => {
  errorProgress()
})

export default router
