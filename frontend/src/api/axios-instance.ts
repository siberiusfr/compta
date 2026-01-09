import Axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { authService } from '@/services/authService'

// Instance Axios principale
export const AXIOS_INSTANCE = Axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Flag pour éviter les boucles infinies de refresh
let isRefreshing = false
// File d'attente des requêtes en attente pendant le refresh
let failedQueue: Array<{
  resolve: (token: string | null) => void
  reject: (error: unknown) => void
}> = []

/**
 * Traite la file d'attente après un refresh réussi ou échoué
 */
const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

/**
 * Redirige vers la page de login
 */
const redirectToLogin = () => {
  // Sauvegarde l'URL actuelle pour redirection après login
  const currentPath = window.location.pathname + window.location.search
  if (currentPath !== '/login' && currentPath !== '/authorized') {
    sessionStorage.setItem('returnUrl', currentPath)
  }
  window.location.href = '/login'
}

// ============================================
// REQUEST INTERCEPTOR - Injection du token JWT
// ============================================
AXIOS_INSTANCE.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    // Ne pas ajouter de token pour les endpoints publics
    const publicEndpoints = ['/health', '/actuator']
    const isPublicEndpoint = publicEndpoints.some((endpoint) =>
      config.url?.includes(endpoint)
    )

    if (!isPublicEndpoint) {
      try {
        const token = await authService.getAccessToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
      } catch (error) {
        console.warn('[Axios] Failed to get access token:', error)
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ============================================
// RESPONSE INTERCEPTOR - Gestion des erreurs 401
// ============================================
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean
    }

    // Si pas de config ou déjà retry, rejeter directement
    if (!originalRequest) {
      return Promise.reject(error)
    }

    // Gestion de l'erreur 401 - Token expiré ou invalide
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Si un refresh est déjà en cours, mettre la requête en file d'attente
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            if (token) {
              originalRequest.headers.Authorization = `Bearer ${token}`
            }
            return AXIOS_INSTANCE(originalRequest)
          })
          .catch((err) => {
            return Promise.reject(err)
          })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // Tenter le refresh du token
        const user = await authService.refreshToken()

        if (user?.access_token) {
          const newToken = user.access_token
          // Mettre à jour le header de la requête originale
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          // Traiter les requêtes en attente
          processQueue(null, newToken)
          // Relancer la requête originale
          return AXIOS_INSTANCE(originalRequest)
        } else {
          // Refresh a échoué - pas de nouveau token
          processQueue(new Error('Token refresh failed'), null)
          // Déconnexion locale et redirection
          await authService.logoutLocal()
          redirectToLogin()
          return Promise.reject(error)
        }
      } catch (refreshError) {
        // Erreur lors du refresh
        processQueue(refreshError, null)
        // Déconnexion locale et redirection
        await authService.logoutLocal()
        redirectToLogin()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // Gestion de l'erreur 403 - Accès refusé
    if (error.response?.status === 403) {
      console.warn('[Axios] Access denied (403)')
      // Optionnel: rediriger vers une page unauthorized
      // window.location.href = '/unauthorized'
    }

    return Promise.reject(error)
  }
)

// ============================================
// CUSTOM INSTANCE - Pour Orval
// ============================================
/**
 * Custom instance utilisée par Orval pour les appels API générés
 * @param config - Configuration Axios
 * @returns Promise avec les données de la réponse
 */
export const customInstance = <T>(config: AxiosRequestConfig): Promise<T> => {
  const source = Axios.CancelToken.source()

  const promise = AXIOS_INSTANCE({
    ...config,
    cancelToken: source.token,
  }).then(({ data }) => data)

  // Attache la méthode cancel pour permettre l'annulation
  // @ts-expect-error - Ajout de cancel sur la promise pour Vue Query
  promise.cancel = () => {
    source.cancel('Query was cancelled')
  }

  return promise
}

// ============================================
// TYPES - Pour une meilleure inférence de type
// ============================================
export type ErrorType<Error> = AxiosError<Error>
export type BodyType<BodyData> = BodyData
