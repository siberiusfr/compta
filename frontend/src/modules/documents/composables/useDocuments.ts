import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useDocumentsStore } from '../stores/documentsStore'

export function useDocuments() {
  const store = useDocumentsStore()
  const {
    documents,
    invoices,
    quotes,
    contracts,
    isLoading,
    filter,
    allDocuments,
    pendingInvoices,
    pendingQuotes,
    activeContracts,
    totalInvoiced,
    totalPending
  } = storeToRefs(store)

  const formatCurrency = (value: number, currency = 'EUR'): string => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency
    }).format(value)
  }

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date)
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
  }

  const getStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      draft: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      pending: 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/30',
      approved: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      rejected: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      archived: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30'
    }
    return colors[status] ?? colors.draft!
  }

  const getTypeIcon = (type: string): string => {
    const icons: Record<string, string> = {
      invoice: 'Receipt',
      quote: 'FileText',
      contract: 'FileSignature',
      report: 'BarChart3',
      other: 'File'
    }
    return icons[type] ?? icons.other!
  }

  onMounted(() => {
    store.fetchDocuments()
    store.fetchInvoices()
    store.fetchQuotes()
    store.fetchContracts()
  })

  return {
    documents,
    invoices,
    quotes,
    contracts,
    isLoading,
    filter,
    allDocuments,
    pendingInvoices,
    pendingQuotes,
    activeContracts,
    totalInvoiced,
    totalPending,
    formatCurrency,
    formatDate,
    formatFileSize,
    getStatusColor,
    getTypeIcon,
    setFilter: store.setFilter,
    clearFilter: store.clearFilter
  }
}
