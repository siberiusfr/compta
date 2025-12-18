import { createRouter, createWebHistory } from 'vue-router'
import { authRoutes } from '@modules/auth/routes'
import { accountingRoutes } from '@modules/accounting/routes'
import { hrRoutes } from '@modules/hr/routes'
import { documentsRoutes } from '@modules/documents/routes'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/accounting',
  },
  ...authRoutes,
  ...accountingRoutes,
  ...hrRoutes,
  ...documentsRoutes,
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

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth ?? true
  const isAuthenticated = localStorage.getItem('auth_token')

  if (requiresAuth && !isAuthenticated) {
    next({ name: 'login' })
  } else if (!requiresAuth && isAuthenticated && to.name === 'login') {
    next({ name: 'accounting' })
  } else {
    next()
  }
})

export default router
