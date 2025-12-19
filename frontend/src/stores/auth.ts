// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const currentCompanyId = ref<number | null>(null)
  const user = ref<any>(null)

  const isAuthenticated = computed(() => !!token.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser: any) {
    user.value = newUser
  }

  function setCurrentCompany(companyId: number) {
    currentCompanyId.value = companyId
  }

  function logout() {
    token.value = null
    user.value = null
    currentCompanyId.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    currentCompanyId,
    user,
    isAuthenticated,
    setToken,
    setUser,
    setCurrentCompany,
    logout
  }
})
