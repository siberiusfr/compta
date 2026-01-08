import { useOAuthStore } from '../stores/oauthStore'
import { toRefs } from 'vue'

export function useOAuth() {
  const store = useOAuthStore()

  return {
    ...toRefs(store)
  }
}
