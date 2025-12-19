import Axios, { type AxiosRequestConfig, type AxiosError } from 'axios'

import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import router from '@/router'

export const AXIOS_INSTANCE = Axios.create({
  baseURL: import.meta.env.PROD
    ? import.meta.env.VITE_API_URL
    : '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor - ajoute le token
AXIOS_INSTANCE.interceptors.request.use((config) => {
  const authStore = useAuthStore()

  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }

  if (authStore.currentCompanyId) {
    config.headers['X-Company-Id'] = authStore.currentCompanyId
  }

  return config
})

// Interceptor - gère les erreurs
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const notificationStore = useNotificationStore()

    if (error.response?.status === 401) {
      useAuthStore().logout()
      router.push('/login')
      notificationStore.error('Session expirée')
    } else if (error.response?.status === 403) {
      notificationStore.error('Accès refusé')
    } else if (error.response?.status === 404) {
      notificationStore.error('Ressource non trouvée')
    } else if (error.response?.status === 500) {
      notificationStore.error('Erreur serveur')
    }

    return Promise.reject(error)
  }
)

// Fonction mutator pour Orval
export const customAxios = <T>(config: AxiosRequestConfig): Promise<T> => {
  return AXIOS_INSTANCE.request(config).then(({ data }) => data)
}

export default customAxios
