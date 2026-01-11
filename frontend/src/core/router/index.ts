import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { authGuard } from './guards'

// Import module routes
import { dashboardRoutes } from '@/modules/dashboard/routes'
import { notificationsRoutes } from '@/modules/notifications/routes'
import { oauthRoutes } from '@/modules/oauth/routes'
import { documentsRoutes } from '@/modules/documents/routes'
import { permissionsRoutes } from '@/modules/permissions/routes'
import { companiesRoutes } from '@/modules/companies/routes'
import { hrRoutes } from '@/modules/hr/routes'
import { accountingRoutes } from '@/modules/accounting/routes'

const routes: RouteRecordRaw[] = [
  // Auth routes (outside dashboard layout)
  ...oauthRoutes,

  // Main app routes (with dashboard layout)
  {
    path: '/',
    component: () => import('@/core/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      ...dashboardRoutes,
      ...notificationsRoutes,
      ...documentsRoutes,
      ...permissionsRoutes,
      ...companiesRoutes,
      ...hrRoutes,
      ...accountingRoutes,
    ],
  },

  // Settings page
  {
    path: '/settings',
    component: () => import('@/core/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'settings',
        component: () => import('@/modules/dashboard/views/DashboardHome.vue'),
        meta: {
          title: 'Parametres',
          requiresAuth: true,
        },
      },
    ],
  },

  // 404 Not Found
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/core/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// Global navigation guard
router.beforeEach(authGuard)

// Update document title
router.afterEach((to) => {
  const title = to.meta.title as string
  document.title = title ? `${title} | Compta` : 'Compta'
})

export default router
