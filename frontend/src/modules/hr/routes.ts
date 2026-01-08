import type { RouteRecordRaw } from 'vue-router'

export const hrRoutes: RouteRecordRaw[] = [
  {
    path: 'hr',
    children: [
      {
        path: 'employees',
        name: 'hr-employees',
        component: () => import('./views/HrEmployees.vue'),
        meta: { title: 'Employés', requiresAuth: true }
      },
      {
        path: 'contracts',
        name: 'hr-contracts',
        component: () => import('./views/HrContracts.vue'),
        meta: { title: 'Contrats', requiresAuth: true }
      },
      {
        path: 'leaves',
        name: 'hr-leaves',
        component: () => import('./views/HrLeaves.vue'),
        meta: { title: 'Congés', requiresAuth: true }
      },
      {
        path: 'payroll',
        name: 'hr-payroll',
        component: () => import('./views/HrPayroll.vue'),
        meta: { title: 'Paie', requiresAuth: true }
      }
    ]
  }
]
