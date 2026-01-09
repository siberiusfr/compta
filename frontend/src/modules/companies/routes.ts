import type { RouteRecordRaw } from 'vue-router'

export const companiesRoutes: RouteRecordRaw[] = [
  {
    path: 'companies',
    children: [
      {
        path: '',
        name: 'companies',
        component: () => import('./views/CompaniesList.vue'),
        meta: {
          title: 'Entreprises',
          requiresAuth: true
        }
      },
      {
        path: 'new',
        name: 'companies-new',
        component: () => import('./views/CompanyNew.vue'),
        meta: {
          title: 'Nouvelle entreprise',
          requiresAuth: true
        }
      }
    ]
  }
]
