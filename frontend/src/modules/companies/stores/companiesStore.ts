import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Company, CompanyContact, CompanyFilter } from '../types/companies.types'
import { mockCompanies, mockContacts } from '../mock-data/companies.mock'

export const useCompaniesStore = defineStore('companies', () => {
  const companies = ref<Company[]>(mockCompanies)
  const contacts = ref<CompanyContact[]>(mockContacts)
  const isLoading = ref(false)
  const filter = ref<CompanyFilter>({})
  const selectedCompany = ref<Company | null>(null)

  const activeCompanies = computed(() => companies.value.filter((c) => c.status === 'active'))

  const totalEmployees = computed(() =>
    companies.value.reduce((sum, c) => sum + c.employeeCount, 0)
  )

  const filteredCompanies = computed(() => {
    let result = [...companies.value]

    if (filter.value.status) {
      result = result.filter((c) => c.status === filter.value.status)
    }

    if (filter.value.type) {
      result = result.filter((c) => c.type === filter.value.type)
    }

    if (filter.value.search) {
      const search = filter.value.search.toLowerCase()
      result = result.filter(
        (c) =>
          c.name.toLowerCase().includes(search) ||
          c.legalName.toLowerCase().includes(search) ||
          c.siret.includes(search)
      )
    }

    return result
  })

  async function fetchCompanies() {
    isLoading.value = true
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))
      companies.value = mockCompanies
    } finally {
      isLoading.value = false
    }
  }

  function getCompanyContacts(companyId: string) {
    return contacts.value.filter((c) => c.companyId === companyId)
  }

  function setFilter(newFilter: CompanyFilter) {
    filter.value = { ...filter.value, ...newFilter }
  }

  function clearFilter() {
    filter.value = {}
  }

  function selectCompany(company: Company | null) {
    selectedCompany.value = company
  }

  return {
    companies,
    contacts,
    isLoading,
    filter,
    selectedCompany,
    activeCompanies,
    totalEmployees,
    filteredCompanies,
    fetchCompanies,
    getCompanyContacts,
    setFilter,
    clearFilter,
    selectCompany,
  }
})
