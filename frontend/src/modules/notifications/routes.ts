import type { RouteRecordRaw } from 'vue-router'

export const notificationsRoutes: RouteRecordRaw[] = [
  {
    path: 'notifications',
    children: [
      {
        path: '',
        redirect: { name: 'notifications-inbox' },
      },
      {
        path: 'inbox',
        name: 'notifications-inbox',
        component: () => import('./views/NotificationsInbox.vue'),
        meta: {
          title: 'Boite de reception',
          requiresAuth: true,
        },
      },
      {
        path: 'sent',
        name: 'notifications-sent',
        component: () => import('./views/NotificationsSent.vue'),
        meta: {
          title: 'Notifications envoyees',
          requiresAuth: true,
        },
      },
      {
        path: 'settings',
        name: 'notifications-settings',
        component: () => import('./views/NotificationsSettings.vue'),
        meta: {
          title: 'Parametres de notification',
          requiresAuth: true,
        },
      },
      {
        path: 'templates',
        name: 'notifications-templates',
        component: () => import('./views/NotificationsTemplates.vue'),
        meta: {
          title: 'Modeles de notification',
          requiresAuth: true,
        },
      },
    ],
  },
]
