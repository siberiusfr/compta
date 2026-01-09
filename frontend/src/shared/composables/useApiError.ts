import { AxiosError } from 'axios'
import { useToast } from './useToast'
import { useRouter } from 'vue-router'

export interface ApiErrorResponse {
  message?: string
  error?: string
  errors?: Record<string, string[]>
  status?: number
  path?: string
  timestamp?: string
}

export interface ApiErrorOptions {
  showToast?: boolean
  redirectOn401?: boolean
  redirectOn403?: boolean
  customMessages?: Record<number, string>
}

const defaultMessages: Record<number, string> = {
  400: 'Requete invalide',
  401: 'Session expiree, veuillez vous reconnecter',
  403: 'Acces refuse',
  404: 'Ressource introuvable',
  409: 'Conflit de donnees',
  422: 'Donnees invalides',
  429: 'Trop de requetes, veuillez reessayer plus tard',
  500: 'Erreur serveur',
  502: 'Service temporairement indisponible',
  503: 'Service en maintenance',
}

export function useApiError(options: ApiErrorOptions = {}) {
  const {
    showToast = true,
    redirectOn401 = false,
    redirectOn403 = false,
    customMessages = {},
  } = options

  const toast = useToast()
  const router = useRouter()

  const messages = { ...defaultMessages, ...customMessages }

  /**
   * Extrait le message d'erreur d'une reponse Axios
   */
  const extractErrorMessage = (error: AxiosError<ApiErrorResponse>): string => {
    const response = error.response
    const data = response?.data
    const status = response?.status

    // Message specifique de l'API
    if (data?.message) {
      return data.message
    }

    // Erreur generique de l'API
    if (data?.error) {
      return data.error
    }

    // Erreurs de validation
    if (data?.errors) {
      const firstError = Object.values(data.errors)[0]
      if (firstError?.[0]) {
        return firstError[0]
      }
    }

    // Message par defaut selon le status
    if (status && messages[status]) {
      return messages[status]
    }

    // Message d'erreur Axios
    if (error.message) {
      return error.message
    }

    return 'Une erreur est survenue'
  }

  /**
   * Gere une erreur API de maniere centralisee
   */
  const handleError = (
    error: unknown,
    context?: string
  ): { message: string; status?: number } => {
    // Si ce n'est pas une erreur Axios, on retourne un message generique
    if (!(error instanceof AxiosError)) {
      const message = error instanceof Error ? error.message : 'Une erreur est survenue'
      if (showToast) {
        toast.error(context ?? 'Erreur', message)
      }
      return { message }
    }

    const status = error.response?.status
    const message = extractErrorMessage(error)

    // Gestion des redirections
    if (status === 401 && redirectOn401) {
      router.push({ name: 'login' })
      return { message, status }
    }

    if (status === 403 && redirectOn403) {
      router.push({ name: 'unauthorized' })
      return { message, status }
    }

    // Affichage du toast
    if (showToast) {
      const title = context ?? getErrorTitle(status)
      toast.error(title, message)
    }

    return { message, status }
  }

  /**
   * Retourne un titre d'erreur selon le status HTTP
   */
  const getErrorTitle = (status?: number): string => {
    if (!status) return 'Erreur'
    if (status >= 500) return 'Erreur serveur'
    if (status === 404) return 'Non trouve'
    if (status === 403) return 'Acces refuse'
    if (status === 401) return 'Non authentifie'
    if (status >= 400) return 'Erreur de validation'
    return 'Erreur'
  }

  /**
   * Wrapper pour les appels API avec gestion d'erreur
   */
  const withErrorHandling = async <T>(
    apiCall: () => Promise<T>,
    context?: string
  ): Promise<{ data?: T; error?: { message: string; status?: number } }> => {
    try {
      const data = await apiCall()
      return { data }
    } catch (error) {
      const errorResult = handleError(error, context)
      return { error: errorResult }
    }
  }

  /**
   * Verifie si une erreur est une erreur reseau
   */
  const isNetworkError = (error: unknown): boolean => {
    if (error instanceof AxiosError) {
      return !error.response && error.code !== 'ERR_CANCELED'
    }
    return false
  }

  /**
   * Verifie si une requete a ete annulee
   */
  const isCancelledError = (error: unknown): boolean => {
    if (error instanceof AxiosError) {
      return error.code === 'ERR_CANCELED'
    }
    return false
  }

  return {
    handleError,
    extractErrorMessage,
    getErrorTitle,
    withErrorHandling,
    isNetworkError,
    isCancelledError,
  }
}
