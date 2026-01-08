import { useHrStore } from '../stores/hrStore'
import { toRefs } from 'vue'

export function useHr() {
  const store = useHrStore()
  return { ...toRefs(store) }
}
