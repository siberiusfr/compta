import type { RouteRecordRaw } from 'vue-router'

export const permissionsRoutes: RouteRecordRaw[] = [
  {
    path: 'permissions',
    children: [
      {
        path: '',
        redirect: { name: 'permissions-users' },
      },
      {
        path: 'users',
        name: 'permissions-users',
        component: () => import('./views/UsersList.vue'),
        meta: {
          title: 'Utilisateurs',
          requiresAuth: true,
          roles: ['Administrateur'],
        },
      },
      {
        path: 'roles',
        name: 'permissions-roles',
        component: () => import('./views/RolesList.vue'),
        meta: {
          title: 'Roles',
          requiresAuth: true,
          roles: ['Administrateur'],
        },
      },
      {
        path: 'groups',
        name: 'permissions-groups',
        component: () => import('./views/GroupsList.vue'),
        meta: {
          title: 'Groupes',
          requiresAuth: true,
          roles: ['Administrateur'],
        },
      },
    ],
  },
]
