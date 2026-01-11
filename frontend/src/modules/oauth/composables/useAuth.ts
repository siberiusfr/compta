import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/authStore'
import type { LoginOptions, LogoutOptions } from '../types/oauth.types'

export function useAuth() {
  const store = useAuthStore()
  const { user, isLoading, error, isAuthenticated, accessToken, userProfile } = storeToRefs(store)

  const userName = computed(
    () => userProfile.value?.name ?? userProfile.value?.preferred_username ?? 'Utilisateur'
  )
  const userEmail = computed(() => userProfile.value?.email ?? '')
  const userInitials = computed(() => {
    const name = userName.value
    const parts = name.split(' ')
    if (parts.length >= 2 && parts[0] && parts[1]) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase()
    }
    return name.slice(0, 2).toUpperCase()
  })

  const login = async (options?: LoginOptions) => {
    if (options?.returnUrl) {
      sessionStorage.setItem('returnUrl', options.returnUrl)
    }
    await store.login()
  }

  const logout = async (options?: LogoutOptions) => {
    if (options?.localOnly) {
      await store.logoutLocal()
    } else {
      await store.logout()
    }
  }

  const handleCallback = async () => {
    await store.handleCallback()
  }

  const refreshToken = async () => {
    await store.refreshToken()
  }

  const getReturnUrl = (): string => {
    return sessionStorage.getItem('returnUrl') ?? '/dashboard'
  }

  const clearReturnUrl = () => {
    sessionStorage.removeItem('returnUrl')
  }

  return {
    user,
    isLoading,
    error,
    isAuthenticated,
    accessToken,
    userProfile,
    userName,
    userEmail,
    userInitials,
    login,
    logout,
    handleCallback,
    refreshToken,
    getReturnUrl,
    clearReturnUrl,
  }
}
