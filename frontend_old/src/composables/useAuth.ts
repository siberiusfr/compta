import { computed } from 'vue'
import { useOAuth2AuthStore } from '@/stores/oauth2Auth'
import type { OAuth2User } from '@/stores/oauth2Auth'

export function useAuth() {
  const authStore = useOAuth2AuthStore()

  const user = computed(() => authStore.user)
  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const isLoading = computed(() => authStore.isLoading)
  const error = computed(() => authStore.error)

  const login = () => authStore.login()
  const logout = () => authStore.logout()
  const refreshSession = () => authStore.refreshSession()

  // Extraire les informations utilisateur du token
  const userInfo = computed<OAuth2User | null>(() => {
    return authStore.getUserInfo()
  })

  const hasRole = (role: string) => {
    return userInfo.value?.roles.includes(role) ?? false
  }

  const hasAnyRole = (roles: string[]) => {
    if (!userInfo.value) return false
    return roles.some(role => userInfo.value!.roles.includes(role))
  }

  const hasAllRoles = (roles: string[]) => {
    if (!userInfo.value) return false
    return roles.every(role => userInfo.value!.roles.includes(role))
  }

  return {
    user,
    userInfo,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    refreshSession,
    hasRole,
    hasAnyRole,
    hasAllRoles,
  }
}
