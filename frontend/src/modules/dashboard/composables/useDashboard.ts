import { useDashboardStore } from '../stores/dashboardStore'
import { toRefs } from 'vue'

export function useDashboard() {
  const store = useDashboardStore()

  return {
    ...toRefs(store)
  }
}
