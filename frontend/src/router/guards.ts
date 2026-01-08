import type { RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export function authGuard(to: RouteLocationNormalized) {
  const authStore = useAuthStore()
  
  // Ne pas bloquer la page de callback OAuth2
  if (to.meta.isCallback) {
    return true
  }
  
  // Vérifier l'authentification pour les routes protégées
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Stocker l'URL de retour pour rediriger après login
    sessionStorage.setItem('auth_return_url', to.fullPath)
    return { name: 'login' }
  }
  
  // Rediriger vers dashboard si déjà connecté sur la page login
  if (to.name === 'login' && authStore.isAuthenticated) {
    return { name: 'dashboard' }
  }
  
  return true
}
