import type { RouteRecordRaw } from 'vue-router'

export const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'dashboard',
    component: () => import('./views/DashboardHome.vue'),
    meta: {
      title: 'Tableau de bord',
      requiresAuth: true,
    },
  },
]
