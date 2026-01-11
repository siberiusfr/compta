import type { RouteRecordRaw } from 'vue-router'

export const accountingRoutes: RouteRecordRaw[] = [
  {
    path: 'accounting',
    children: [
      {
        path: '',
        redirect: { name: 'accounting-chart-of-accounts' },
      },
      {
        path: 'chart-of-accounts',
        name: 'accounting-chart-of-accounts',
        component: () => import('./views/ChartOfAccounts.vue'),
        meta: {
          title: 'Plan comptable',
          requiresAuth: true,
        },
      },
      {
        path: 'journal-entries',
        name: 'accounting-journal-entries',
        component: () => import('./views/JournalEntries.vue'),
        meta: {
          title: 'Ecritures comptables',
          requiresAuth: true,
        },
      },
      {
        path: 'general-ledger',
        name: 'accounting-general-ledger',
        component: () => import('./views/GeneralLedger.vue'),
        meta: {
          title: 'Grand livre',
          requiresAuth: true,
        },
      },
      {
        path: 'trial-balance',
        name: 'accounting-trial-balance',
        component: () => import('./views/TrialBalance.vue'),
        meta: {
          title: 'Balance generale',
          requiresAuth: true,
        },
      },
      {
        path: 'balance-sheet',
        name: 'accounting-balance-sheet',
        component: () => import('./views/BalanceSheet.vue'),
        meta: {
          title: 'Bilan',
          requiresAuth: true,
        },
      },
      {
        path: 'income-statement',
        name: 'accounting-income-statement',
        component: () => import('./views/IncomeStatement.vue'),
        meta: {
          title: 'Compte de resultat',
          requiresAuth: true,
        },
      },
    ],
  },
]
