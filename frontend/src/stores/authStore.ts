import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService, type User } from '@/services/authService'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const isLoading = ref(true)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!user.value && !user.value.expired)
  const accessToken = computed(() => user.value?.access_token ?? null)
  const userProfile = computed(() => user.value?.profile ?? null)

  // Actions
  async function initialize(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      user.value = await authService.getUser()
    } catch (e) {
      error.value = "Erreur lors de l'initialisation de l'authentification"
      console.error('[AuthStore] Initialize error:', e)
    } finally {
      isLoading.value = false
    }

    // Ecouter les changements d'utilisateur
    authService.onUserLoaded((loadedUser) => {
      user.value = loadedUser
    })

    authService.onUserUnloaded(() => {
      user.value = null
    })

    authService.onAccessTokenExpired(() => {
      user.value = null
    })
  }

  async function login(): Promise<void> {
    error.value = null
    try {
      await authService.login()
    } catch (e) {
      error.value = 'Erreur lors de la connexion'
      console.error('[AuthStore] Login error:', e)
      throw e
    }
  }

  async function handleCallback(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      user.value = await authService.handleCallback()
    } catch (e) {
      error.value = "Erreur lors du callback d'authentification"
      console.error('[AuthStore] Callback error:', e)
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function logout(): Promise<void> {
    error.value = null
    try {
      await authService.logout()
    } catch (e) {
      error.value = 'Erreur lors de la deconnexion'
      console.error('[AuthStore] Logout error:', e)
      throw e
    }
  }

  async function logoutLocal(): Promise<void> {
    await authService.logoutLocal()
    user.value = null
  }

  async function refreshToken(): Promise<void> {
    try {
      user.value = await authService.refreshToken()
    } catch (e) {
      console.error('[AuthStore] Refresh token error:', e)
    }
  }

  return {
    // State
    user,
    isLoading,
    error,

    // Getters
    isAuthenticated,
    accessToken,
    userProfile,

    // Actions
    initialize,
    login,
    handleCallback,
    logout,
    logoutLocal,
    refreshToken,
  }
})
