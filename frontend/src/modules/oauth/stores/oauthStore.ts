import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockOAuthApplications, mockOAuthTokens, mockOAuthConsents } from '../mock-data/oauth.mock'
import type { OAuthApplication, OAuthToken, OAuthConsent } from '../types/oauth.types'

export const useOAuthStore = defineStore('oauth', () => {
  const applications = ref<OAuthApplication[]>(mockOAuthApplications)
  const tokens = ref<OAuthToken[]>(mockOAuthTokens)
  const consents = ref<OAuthConsent[]>(mockOAuthConsents)

  const activeApplications = computed(() =>
    applications.value.filter(app => app.active)
  )

  const activeTokens = computed(() =>
    tokens.value.filter(token => new Date(token.expiresAt) > new Date())
  )

  const expiredTokens = computed(() =>
    tokens.value.filter(token => new Date(token.expiresAt) <= new Date())
  )

  function createApplication(application: Omit<OAuthApplication, 'id' | 'createdAt'>) {
    const newApp: OAuthApplication = {
      ...application,
      id: Date.now().toString(),
      createdAt: new Date()
    }
    applications.value.push(newApp)
    return newApp
  }

  function updateApplication(id: string, updates: Partial<OAuthApplication>) {
    const index = applications.value.findIndex(app => app.id === id)
    if (index !== -1) {
      applications.value[index] = { ...applications.value[index], ...updates } as OAuthApplication
    }
  }

  function deleteApplication(id: string) {
    const index = applications.value.findIndex(app => app.id === id)
    if (index !== -1) {
      applications.value.splice(index, 1)
    }
  }

  function revokeToken(id: string) {
    const index = tokens.value.findIndex(token => token.id === id)
    if (index !== -1) {
      tokens.value.splice(index, 1)
    }
  }

  function revokeConsent(id: string) {
    const index = consents.value.findIndex(consent => consent.id === id)
    if (index !== -1) {
      consents.value.splice(index, 1)
    }
  }

  return {
    applications,
    tokens,
    consents,
    activeApplications,
    activeTokens,
    expiredTokens,
    createApplication,
    updateApplication,
    deleteApplication,
    revokeToken,
    revokeConsent
  }
})
