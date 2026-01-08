import { useNotificationsStore } from '../stores/notificationsStore'
import { toRefs } from 'vue'

export function useNotifications() {
  const store = useNotificationsStore()

  return {
    ...toRefs(store)
  }
}
