import { defineStore } from 'pinia'
import type { User } from '@app-types/index'

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem('auth_token'),
    isAuthenticated: !!localStorage.getItem('auth_token'),
  }),

  getters: {
    currentUser: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
  },

  actions: {
    async login(email: string, password: string) {
      try {
        // TODO: Replace with actual API call
        const mockToken = 'mock-jwt-token'
        const mockUser: User = {
          id: '1',
          email,
          name: 'John Doe',
          role: 'admin',
        }

        this.token = mockToken
        this.user = mockUser
        this.isAuthenticated = true

        localStorage.setItem('auth_token', mockToken)
        localStorage.setItem('user', JSON.stringify(mockUser))

        return { success: true }
      } catch (error) {
        console.error('Login error:', error)
        return { success: false, error }
      }
    },

    async logout() {
      this.token = null
      this.user = null
      this.isAuthenticated = false

      localStorage.removeItem('auth_token')
      localStorage.removeItem('user')
    },

    async register(email: string, password: string, name: string) {
      try {
        // TODO: Replace with actual API call
        return { success: true }
      } catch (error) {
        console.error('Register error:', error)
        return { success: false, error }
      }
    },

    async checkAuth() {
      const token = localStorage.getItem('auth_token')
      const userStr = localStorage.getItem('user')

      if (token && userStr) {
        this.token = token
        this.user = JSON.parse(userStr)
        this.isAuthenticated = true
      }
    },
  },
})
