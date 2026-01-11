import type { RouteRecordRaw } from 'vue-router'

export const oauthRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('./views/LoginPage.vue'),
    meta: {
      title: 'Connexion',
      guestOnly: true,
      layout: 'auth',
    },
  },
  {
    path: '/authorized',
    name: 'authorized',
    component: () => import('./views/CallbackPage.vue'),
    meta: {
      title: 'Authentification',
      isCallback: true,
    },
  },
  {
    path: '/unauthorized',
    name: 'unauthorized',
    component: () => import('./views/UnauthorizedPage.vue'),
    meta: {
      title: 'Acces refuse',
    },
  },
]
