import type { RouteRecordRaw } from 'vue-router'

export const hrRoutes: RouteRecordRaw[] = [
  {
    path: '/hr',
    name: 'hr',
    component: () => import('./views/HRDashboard.vue'),
    meta: {
      requiresAuth: true,
      title: 'Ressources Humaines',
    },
  },
  {
    path: '/hr/employees',
    name: 'hr-employees',
    component: () => import('./views/EmployeesView.vue'),
    meta: {
      requiresAuth: true,
      title: 'Employés',
    },
  },
  {
    path: '/hr/payroll',
    name: 'hr-payroll',
    component: () => import('./views/PayrollView.vue'),
    meta: {
      requiresAuth: true,
      title: 'Paie',
    },
  },
]
