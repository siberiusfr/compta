import type { RouteRecordRaw } from 'vue-router'

export const hrRoutes: RouteRecordRaw[] = [
  {
    path: 'hr',
    children: [
      {
        path: '',
        redirect: { name: 'hr-employees' },
      },
      {
        path: 'employees',
        name: 'hr-employees',
        component: () => import('./views/EmployeesList.vue'),
        meta: {
          title: 'Employes',
          requiresAuth: true,
        },
      },
      {
        path: 'contracts',
        name: 'hr-contracts',
        component: () => import('./views/ContractsList.vue'),
        meta: {
          title: 'Contrats',
          requiresAuth: true,
        },
      },
      {
        path: 'leaves',
        name: 'hr-leaves',
        component: () => import('./views/LeavesList.vue'),
        meta: {
          title: 'Conges',
          requiresAuth: true,
        },
      },
      {
        path: 'payroll',
        name: 'hr-payroll',
        component: () => import('./views/PayrollList.vue'),
        meta: {
          title: 'Paie',
          requiresAuth: true,
        },
      },
    ],
  },
]
