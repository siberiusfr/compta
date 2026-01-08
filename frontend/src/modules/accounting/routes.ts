import type { RouteRecordRaw } from 'vue-router'

export const accountingRoutes: RouteRecordRaw[] = [
  {
    path: 'accounting',
    children: [
      {
        path: 'journal',
        name: 'accounting-journal',
        component: () => import('./views/AccountingJournal.vue'),
        meta: { title: 'Journal', requiresAuth: true }
      },
      {
        path: 'ledger',
        name: 'accounting-ledger',
        component: () => import('./views/AccountingLedger.vue'),
        meta: { title: 'Grand livre', requiresAuth: true }
      },
      {
        path: 'balance-sheet',
        name: 'accounting-balance-sheet',
        component: () => import('./views/AccountingBalanceSheet.vue'),
        meta: { title: 'Bilan', requiresAuth: true }
      },
      {
        path: 'income-statement',
        name: 'accounting-income-statement',
        component: () => import('./views/AccountingIncomeStatement.vue'),
        meta: { title: 'Compte de résultat', requiresAuth: true }
      },
      {
        path: 'invoices',
        name: 'accounting-invoices',
        component: () => import('./views/AccountingInvoices.vue'),
        meta: { title: 'Factures', requiresAuth: true }
      },
      {
        path: 'expenses',
        name: 'accounting-expenses',
        component: () => import('./views/AccountingExpenses.vue'),
        meta: { title: 'Dépenses', requiresAuth: true }
      },
      {
        path: 'reports',
        name: 'accounting-reports',
        component: () => import('./views/AccountingReports.vue'),
        meta: { title: 'Rapports', requiresAuth: true }
      }
    ]
  }
]
