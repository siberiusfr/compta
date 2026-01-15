import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ReferentielFilter } from '../types/referentiel.types'

export const useReferentielStore = defineStore('referentiel', () => {
  const companyId = ref<number>(1)
  const filter = ref<ReferentielFilter>({})
  const activeTab = ref<'produits' | 'clients' | 'fournisseurs' | 'familles'>('produits')

  const setCompanyId = (id: number) => {
    companyId.value = id
  }

  const setFilter = (newFilter: ReferentielFilter) => {
    filter.value = { ...filter.value, ...newFilter }
  }

  const clearFilter = () => {
    filter.value = {}
  }

  const setActiveTab = (tab: typeof activeTab.value) => {
    activeTab.value = tab
  }

  return {
    companyId,
    filter,
    activeTab,
    setCompanyId,
    setFilter,
    clearFilter,
    setActiveTab,
  }
})
