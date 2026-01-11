import type { RouteRecordRaw } from 'vue-router'

export const invoicesRoutes: RouteRecordRaw[] = [
  {
    path: 'invoices',
    children: [
      {
        path: '',
        name: 'invoices-list',
        component: () => import('./views/InvoicesList.vue'),
        meta: {
          title: 'Factures',
          requiresAuth: true,
        },
      },
      {
        path: 'create',
        name: 'invoices-create',
        component: () => import('./views/InvoiceCreate.vue'),
        meta: {
          title: 'Nouvelle facture',
          requiresAuth: true,
        },
      },
      {
        path: ':id',
        name: 'invoices-detail',
        component: () => import('./views/InvoiceDetail.vue'),
        meta: {
          title: 'Détails facture',
          requiresAuth: true,
        },
      },
      {
        path: ':id/edit',
        name: 'invoices-edit',
        component: () => import('./views/InvoiceEdit.vue'),
        meta: {
          title: 'Modifier facture',
          requiresAuth: true,
        },
      },
    ],
  },
]
