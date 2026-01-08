import type { RouteRecordRaw } from 'vue-router'

export const permissionsRoutes: RouteRecordRaw[] = [
  {
    path: 'permissions',
    children: [
      {
        path: 'roles',
        name: 'permissions-roles',
        component: () => import('./views/PermissionsRoles.vue'),
        meta: { title: 'Rôles', requiresAuth: true }
      },
      {
        path: 'users',
        name: 'permissions-users',
        component: () => import('./views/PermissionsUsers.vue'),
        meta: { title: 'Utilisateurs', requiresAuth: true }
      },
      {
        path: 'audit',
        name: 'permissions-audit',
        component: () => import('./views/PermissionsAudit.vue'),
        meta: { title: 'Audit', requiresAuth: true }
      }
    ]
  }
]
