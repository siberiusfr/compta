import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'
import type { App } from 'vue'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Cache pendant 5 minutes
      staleTime: 1000 * 60 * 5,

      // Garbage collection après 10 minutes
      gcTime: 1000 * 60 * 10,

      // Refetch quand l'utilisateur revient sur l'onglet
      refetchOnWindowFocus: true,

      // Refetch après reconnexion Internet
      refetchOnReconnect: true,

      // Retry une fois en cas d'erreur
      retry: 1
    }
  }
})

export default {
  install(app: App) {
    app.use(VueQueryPlugin, { queryClient })
  }
}
