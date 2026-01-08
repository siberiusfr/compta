import { usePermissionsStore } from '../stores/permissionsStore'
import { toRefs } from 'vue'

export function usePermissions() {
  const store = usePermissionsStore()
  return { ...toRefs(store) }
}
