import type { RouteRecordRaw } from 'vue-router'

export const accountingRoutes: RouteRecordRaw[] = [
  {
    path: '/accounting',
    name: 'accounting',
    component: () => import('./views/AccountingDashboard.vue'),
    meta: {
      requiresAuth: true,
      title: 'Comptabilité',
    },
  },
  {
    path: '/accounting/invoices',
    name: 'accounting-invoices',
    component: () => import('./views/InvoicesView.vue'),
    meta: {
      requiresAuth: true,
      title: 'Factures',
    },
  },
  {
    path: '/accounting/expenses',
    name: 'accounting-expenses',
    component: () => import('./views/ExpensesView.vue'),
    meta: {
      requiresAuth: true,
      title: 'Dépenses',
    },
  },
]
