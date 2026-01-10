import type { RouteRecordRaw } from 'vue-router'

export const documentsRoutes: RouteRecordRaw[] = [
  {
    path: '/documents',
    name: 'documents',
    component: () => import('./views/DocumentsView.vue'),
    meta: {
      requiresAuth: true,
      title: 'Documents',
    },
  },
]
