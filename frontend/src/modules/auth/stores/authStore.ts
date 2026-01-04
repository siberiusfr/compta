import { defineStore } from 'pinia'
import {
  login as apiLogin,
  logout as apiLogout,
  createSocieteUser as apiRegister,
  getCurrentUser,
} from '@/modules/auth/api/generated/auth-api'
import type { LoginRequest, CreateUserRequest, UserResponse } from '@/modules/auth/api/generated/auth-api'
import { apiClient } from '@/api/client'

interface AuthState {
  user: UserResponse | null
  token: string | null
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem('auth_token'),
    isAuthenticated: false, // Initialiser à false jusqu'à vérification
  }),

  getters: {
    currentUser: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
  },

  actions: {
    async login(credentials: LoginRequest) {
      try {
        const response = await apiLogin(credentials)
        const token = response.token

        if (!token) {
          throw new Error("Token non fourni par l'API")
        }

        this.token = token
        localStorage.setItem('auth_token', token)
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`

        await this.fetchUser()

        return { success: true }
      } catch (error) {
        console.error('Erreur de connexion :', error)
        await this.logout()
        return { success: false, error }
      }
    },

    async logout() {
      try {
        await apiLogout()
      } catch (error) {
        console.error("Erreur lors de la déconnexion de l'API, nettoyage local forcé.", error)
      } finally {
        this.token = null
        this.user = null
        this.isAuthenticated = false
        localStorage.removeItem('auth_token')
        localStorage.removeItem('user')
        delete apiClient.defaults.headers.common['Authorization']
      }
    },

    async register(data: CreateUserRequest) {
      try {
        await apiRegister(data)
        return { success: true }
      } catch (error) {
        console.error("Erreur d'inscription :", error)
        return { success: false, error }
      }
    },

    async fetchUser() {
      if (!this.token) return

      try {
        const response = await getCurrentUser()
        this.user = response
        this.isAuthenticated = true
      } catch (error) {
        console.error("Impossible de récupérer l'utilisateur, déconnexion.", error)
        await this.logout()
      }
    },

    async checkAuth() {
      const token = localStorage.getItem('auth_token')
      if (token) {
        this.token = token
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`
        await this.fetchUser()
      } else {
        this.isAuthenticated = false
      }
    },
  },
})
