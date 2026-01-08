import { defineStore } from 'pinia'
import { ref } from 'vue'
import { mockCompanies, mockCompanySettings } from '../mock-data/companies.mock'
import type { Company, CompanySettings } from '../types/companies.types'

export const useCompaniesStore = defineStore('companies', () => {
  const companies = ref<Company[]>(mockCompanies)
  const settings = ref<CompanySettings>(mockCompanySettings)

  function createCompany(company: Omit<Company, 'id' | 'createdAt'>) {
    const newCompany: Company = {
      ...company,
      id: Date.now().toString(),
      createdAt: new Date()
    }
    companies.value.push(newCompany)
    return newCompany
  }

  function updateCompany(id: string, updates: Partial<Company>) {
    const index = companies.value.findIndex(c => c.id === id)
    if (index !== -1) {
      companies.value[index] = { ...companies.value[index], ...updates } as Company
    }
  }

  function deleteCompany(id: string) {
    const index = companies.value.findIndex(c => c.id === id)
    if (index !== -1) {
      companies.value.splice(index, 1)
    }
  }

  function updateSettings(newSettings: Partial<CompanySettings>) {
    settings.value = { ...settings.value, ...newSettings }
  }

  return {
    companies,
    settings,
    createCompany,
    updateCompany,
    deleteCompany,
    updateSettings
  }
})
