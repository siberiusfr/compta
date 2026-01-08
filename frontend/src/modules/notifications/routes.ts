import type { RouteRecordRaw } from 'vue-router'

export const notificationsRoutes: RouteRecordRaw[] = [
  {
    path: 'notifications',
    children: [
      {
        path: 'inbox',
        name: 'notifications-inbox',
        component: () => import('./views/NotificationsInbox.vue'),
        meta: { title: 'Boîte de réception', requiresAuth: true }
      },
      {
        path: 'sent',
        name: 'notifications-sent',
        component: () => import('./views/NotificationsSent.vue'),
        meta: { title: 'Notifications envoyées', requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'notifications-settings',
        component: () => import('./views/NotificationsSettings.vue'),
        meta: { title: 'Paramètres', requiresAuth: true }
      },
      {
        path: 'templates',
        name: 'notifications-templates',
        component: () => import('./views/NotificationsTemplates.vue'),
        meta: { title: 'Modèles', requiresAuth: true }
      }
    ]
  }
]
