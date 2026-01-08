import { useAccountingStore } from '../stores/accountingStore'
import { toRefs } from 'vue'

export function useAccounting() {
  const store = useAccountingStore()
  return { ...toRefs(store) }
}
