import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

import { dashboardRoutes } from '@/modules/dashboard/routes'
import { notificationsRoutes } from '@/modules/notifications/routes'
import { oauthRoutes } from '@/modules/oauth/routes'
import { documentsRoutes } from '@/modules/documents/routes'
import { permissionsRoutes } from '@/modules/permissions/routes'
import { companiesRoutes } from '@/modules/companies/routes'
import { hrRoutes } from '@/modules/hr/routes'
import { accountingRoutes } from '@/modules/accounting/routes'
import { authGuard } from './guards'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/core/views/LoginView.vue'),
    meta: { title: 'Connexion' }
  },
  {
    path: '/authorized',
    name: 'authorized',
    component: () => import('@/core/views/AuthorizedCallback.vue'),
    meta: { title: 'Autorisation' }
  },
  {
    path: '/',
    component: () => import('@/core/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      ...dashboardRoutes,
      ...notificationsRoutes,
      ...oauthRoutes,
      ...documentsRoutes,
      ...permissionsRoutes,
      ...companiesRoutes,
      ...hrRoutes,
      ...accountingRoutes,
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/core/views/NotFoundView.vue'),
    meta: { title: 'Page non trouvée' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(authGuard)

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} | Compta` : 'Compta'
})

export default router
