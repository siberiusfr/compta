import type { RouteRecordRaw } from 'vue-router'

export const oauthRoutes: RouteRecordRaw[] = [
  {
    path: 'oauth',
    children: [
      {
        path: 'applications',
        name: 'oauth-applications',
        component: () => import('./views/OAuthApplications.vue'),
        meta: { title: 'Applications OAuth', requiresAuth: true }
      },
      {
        path: 'tokens',
        name: 'oauth-tokens',
        component: () => import('./views/OAuthTokens.vue'),
        meta: { title: 'Tokens OAuth', requiresAuth: true }
      },
      {
        path: 'consents',
        name: 'oauth-consents',
        component: () => import('./views/OAuthConsents.vue'),
        meta: { title: 'Consentements', requiresAuth: true }
      }
    ]
  }
]
