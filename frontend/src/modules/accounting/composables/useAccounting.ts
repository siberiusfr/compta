import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAccountingStore } from '../stores/accountingStore'

export function useAccounting() {
  const store = useAccountingStore()
  const {
    accounts,
    journalEntries,
    trialBalance,
    fiscalYears,
    isLoading,
    currentFiscalYear,
    activeAccounts,
    accountsByType,
    draftEntries,
    postedEntries,
    totalAssets,
    totalLiabilities,
    totalEquity,
    totalRevenue,
    totalExpenses,
    netIncome
  } = storeToRefs(store)

  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(Math.abs(value))
  }

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date)
  }

  const getAccountTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      asset: 'Actif',
      liability: 'Passif',
      equity: 'Capitaux propres',
      revenue: 'Produits',
      expense: 'Charges'
    }
    return labels[type] ?? type
  }

  const getAccountTypeColor = (type: string): string => {
    const colors: Record<string, string> = {
      asset: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
      liability: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      equity: 'text-purple-600 bg-purple-100 dark:text-purple-400 dark:bg-purple-900/30',
      revenue: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      expense: 'text-orange-600 bg-orange-100 dark:text-orange-400 dark:bg-orange-900/30'
    }
    return colors[type] ?? ''
  }

  const getStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      draft: 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/30',
      posted: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      cancelled: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      open: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      closed: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30'
    }
    return colors[status] ?? ''
  }

  const getStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      draft: 'Brouillon',
      posted: 'Comptabilise',
      cancelled: 'Annule',
      open: 'Ouvert',
      closed: 'Cloture'
    }
    return labels[status] ?? status
  }

  onMounted(() => {
    store.fetchAccounts()
    store.fetchJournalEntries()
  })

  return {
    accounts,
    journalEntries,
    trialBalance,
    fiscalYears,
    isLoading,
    currentFiscalYear,
    activeAccounts,
    accountsByType,
    draftEntries,
    postedEntries,
    totalAssets,
    totalLiabilities,
    totalEquity,
    totalRevenue,
    totalExpenses,
    netIncome,
    formatCurrency,
    formatDate,
    getAccountTypeLabel,
    getAccountTypeColor,
    getStatusColor,
    getStatusLabel,
    postEntry: store.postEntry,
    getAccountBalance: store.getAccountBalance
  }
}
