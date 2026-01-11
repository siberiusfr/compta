import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useCompaniesStore } from '../stores/companiesStore'

export function useCompanies() {
  const store = useCompaniesStore()
  const {
    companies,
    contacts,
    isLoading,
    filter,
    selectedCompany,
    activeCompanies,
    totalEmployees,
    filteredCompanies,
  } = storeToRefs(store)

  const formatCurrency = (value?: number): string => {
    if (!value) return '-'
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
      maximumFractionDigits: 0,
    }).format(value)
  }

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(date)
  }

  const getStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      active: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      inactive: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      pending: 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/30',
    }
    return colors[status] ?? colors.inactive!
  }

  const getStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      active: 'Active',
      inactive: 'Inactive',
      pending: 'En attente',
    }
    return labels[status] ?? status
  }

  const getTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      sarl: 'SARL',
      sas: 'SAS',
      sa: 'SA',
      eurl: 'EURL',
      ei: 'EI',
      'auto-entrepreneur': 'Auto-entrepreneur',
    }
    return labels[type] ?? type.toUpperCase()
  }

  const getInitials = (name: string): string => {
    const parts = name.split(' ')
    if (parts.length >= 2 && parts[0] && parts[1]) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase()
    }
    return name.slice(0, 2).toUpperCase()
  }

  onMounted(() => {
    store.fetchCompanies()
  })

  return {
    companies,
    contacts,
    isLoading,
    filter,
    selectedCompany,
    activeCompanies,
    totalEmployees,
    filteredCompanies,
    formatCurrency,
    formatDate,
    getStatusColor,
    getStatusLabel,
    getTypeLabel,
    getInitials,
    getCompanyContacts: store.getCompanyContacts,
    setFilter: store.setFilter,
    clearFilter: store.clearFilter,
    selectCompany: store.selectCompany,
  }
}
