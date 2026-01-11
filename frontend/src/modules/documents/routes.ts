import type { RouteRecordRaw } from 'vue-router'

export const documentsRoutes: RouteRecordRaw[] = [
  {
    path: 'documents',
    children: [
      {
        path: '',
        name: 'documents',
        component: () => import('./views/DocumentsList.vue'),
        meta: {
          title: 'Documents',
          requiresAuth: true,
        },
      },
      {
        path: 'invoices',
        name: 'documents-invoices',
        component: () => import('./views/InvoicesList.vue'),
        meta: {
          title: 'Factures',
          requiresAuth: true,
        },
      },
      {
        path: 'quotes',
        name: 'documents-quotes',
        component: () => import('./views/QuotesList.vue'),
        meta: {
          title: 'Devis',
          requiresAuth: true,
        },
      },
      {
        path: 'contracts',
        name: 'documents-contracts',
        component: () => import('./views/ContractsList.vue'),
        meta: {
          title: 'Contrats',
          requiresAuth: true,
        },
      },
      {
        path: 'shared',
        name: 'documents-shared',
        component: () => import('./views/SharedWithMe.vue'),
        meta: {
          title: 'Partages avec moi',
          requiresAuth: true,
        },
      },
      {
        path: 'categories',
        name: 'documents-categories',
        component: () => import('./views/CategoriesManager.vue'),
        meta: {
          title: 'Categories',
          requiresAuth: true,
        },
      },
      {
        path: 'tags',
        name: 'documents-tags',
        component: () => import('./views/TagsManager.vue'),
        meta: {
          title: 'Tags',
          requiresAuth: true,
        },
      },
    ],
  },
]
