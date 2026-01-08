import { useCompaniesStore } from '../stores/companiesStore'
import { toRefs } from 'vue'

export function useCompanies() {
  const store = useCompaniesStore()
  return { ...toRefs(store) }
}
