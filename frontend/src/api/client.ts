import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { ApiError } from '../types'
import { useOAuth2AuthStore } from '@/stores/oauth2Auth'

const getBaseUrl = () => {
  // Le proxy Vite s'attend à des chemins relatifs commençant par /api.
  // Orval génère déjà des URLs avec ce préfixe.
  // Laisser baseURL vide permet à Axios d'utiliser ces chemins correctement.
  return ''
}

export const apiClient: AxiosInstance = axios.create({
  baseURL: getBaseUrl(),
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - Add OAuth2 access token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useOAuth2AuthStore()
    const token = authStore.accessToken
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - Handle errors and token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const apiError: ApiError = {
      message: error.message,
      status: error.response?.status,
      code: error.code,
    }

    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // Handle 401 Unauthorized - try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const authStore = useOAuth2AuthStore()
        await authStore.refreshSession()

        // Retry the original request with the new token
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${authStore.accessToken}`
        }
        return apiClient(originalRequest)
      } catch (refreshError) {
        // Refresh failed, redirect to login
        const authStore = useOAuth2AuthStore()
        await authStore.logout()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(apiError)
  }
)

export default apiClient
