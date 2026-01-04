import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { UserManager, User } from 'oidc-client-ts'

const authority = import.meta.env.VITE_OAUTH2_AUTHORITY || 'http://localhost:9000'
const redirectUri = import.meta.env.VITE_OAUTH2_REDIRECT_URI || 'http://localhost:3000/authorized'

console.log('OAuth2 Configuration:', { authority, redirectUri })

const userManager = new UserManager({
  authority,
  client_id: 'public-client',
  redirect_uri: redirectUri,
  post_logout_redirect_uri: import.meta.env.VITE_OAUTH2_POST_LOGOUT_REDIRECT_URI || 'http://localhost:3000',
  response_type: 'code',
  scope: 'openid read write',
  automaticSilentRenew: true,
  silent_redirect_uri: import.meta.env.VITE_OAUTH2_SILENT_REDIRECT_URI || 'http://localhost:3000/silent-refresh',
  includeIdTokenInSilentRenew: true,
  loadUserInfo: true,
  monitorSession: false,
})

export interface OAuth2User {
  id: string
  username: string
  email: string
  roles: string[]
  accessToken: string
  refreshToken: string
  expiresAt: number
}

export const useOAuth2AuthStore = defineStore('oauth2Auth', () => {
  const user = ref<User | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!user.value && !user.value.expired)
  const accessToken = computed(() => user.value?.access_token)
  const idToken = computed(() => user.value?.id_token)

  async function login() {
    try {
      console.log('Starting OAuth2 login flow...')
      await userManager.signinRedirect()
    } catch (err) {
      console.error('OAuth2 login error:', err)
      error.value = err instanceof Error ? err.message : 'Login failed'
      throw err
    }
  }

  async function handleCallback() {
    try {
      isLoading.value = true
      user.value = await userManager.signinRedirectCallback()
      return user.value
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Callback failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function logout() {
    try {
      await userManager.signoutRedirect()
      user.value = null
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Logout failed'
      throw err
    }
  }

  async function refreshSession() {
    try {
      user.value = await userManager.signinSilent()
      return user.value
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Refresh failed'
      await logout()
      throw err
    }
  }

  async function checkAuth() {
    try {
      user.value = await userManager.getUser()
      return isAuthenticated.value
    } catch (err) {
      user.value = null
      return false
    }
  }

  function getUserInfo(): OAuth2User | null {
    if (!user.value?.profile) return null

    const profile = user.value.profile as any

    return {
      id: profile.sub || '',
      username: profile.preferred_username || profile.name || '',
      email: profile.email || '',
      roles: Array.isArray(profile.roles) ? profile.roles : [],
      accessToken: user.value.access_token,
      refreshToken: user.value.refresh_token || '',
      expiresAt: user.value.expires_at || 0,
    }
  }

  return {
    user,
    isLoading,
    error,
    isAuthenticated,
    accessToken,
    idToken,
    login,
    logout,
    handleCallback,
    refreshSession,
    checkAuth,
    getUserInfo,
  }
})
