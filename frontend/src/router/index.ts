import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/pages/HomePage.vue'),
  },
  {
    path: '/authorized',
    name: 'authorized',
    component: () => import('@/pages/AuthorizedPage.vue'),
    meta: { isCallback: true },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/pages/DashboardPage.vue'),
    meta: { requiresAuth: true },
  },
  // Ajouter d'autres routes protegees ici
  // {
  //   path: '/settings',
  //   name: 'settings',
  //   component: () => import('@/pages/SettingsPage.vue'),
  //   meta: { requiresAuth: true },
  // },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard pour proteger les routes
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // Attendre l'initialisation si necessaire
  if (authStore.isLoading) {
    await new Promise<void>((resolve) => {
      const unwatch = authStore.$subscribe(() => {
        if (!authStore.isLoading) {
          unwatch()
          resolve()
        }
      })
      // Timeout de securite
      setTimeout(() => {
        unwatch()
        resolve()
      }, 5000)
    })
  }

  // Ne pas bloquer la page de callback
  if (to.meta.isCallback) {
    next()
    return
  }

  // Verifier l'authentification pour les routes protegees
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Sauvegarder l'URL de retour
    sessionStorage.setItem('auth_return_url', to.fullPath)

    // Rediriger vers login ou page d'accueil
    next({ name: 'home' })
    return
  }

  next()
})

export default router
