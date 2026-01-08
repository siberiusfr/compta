import type { RouteRecordRaw } from 'vue-router'

export const companiesRoutes: RouteRecordRaw[] = [
  {
    path: 'companies',
    children: [
      {
        path: 'all',
        name: 'companies-all',
        component: () => import('./views/CompaniesAll.vue'),
        meta: { title: 'Toutes les entreprises', requiresAuth: true }
      },
      {
        path: 'create',
        name: 'companies-create',
        component: () => import('./views/CompaniesCreate.vue'),
        meta: { title: 'Créer une entreprise', requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'companies-settings',
        component: () => import('./views/CompaniesSettings.vue'),
        meta: { title: 'Paramètres', requiresAuth: true }
      }
    ]
  }
]
