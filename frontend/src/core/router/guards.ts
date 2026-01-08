import type { RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export function authGuard(to: RouteLocationNormalized) {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Stocker l'URL de retour pour rediriger après login
    if (to.path !== '/login' && to.path !== '/authorized') {
      sessionStorage.setItem('auth_return_url', to.fullPath)
    }
    return { name: 'login' }
  }
  
  if (to.name === 'login' && authStore.isAuthenticated) {
    return { name: 'dashboard' }
  }
}
