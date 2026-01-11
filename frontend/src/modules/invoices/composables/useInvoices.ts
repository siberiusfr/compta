import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useInvoicesStore } from '../stores/invoicesStore'

export function useInvoices() {
  const store = useInvoicesStore()
  const {
    invoices,
    isLoading,
    error,
    draftInvoices,
    sentInvoices,
    paidInvoices,
    overdueInvoices,
    cancelledInvoices,
    totalRevenue,
    pendingRevenue,
    overdueAmount,
    invoiceSummaries,
  } = storeToRefs(store)

  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
    }).format(value)
  }

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(date)
  }

  const getInvoiceTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      sale: 'Vente',
      purchase: 'Achat',
      credit_note: 'Avoir',
      debit_note: 'Débit',
    }
    return labels[type] ?? type
  }

  const getInvoiceStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      draft: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      sent: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
      paid: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      overdue: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      cancelled: 'text-orange-600 bg-orange-100 dark:text-orange-400 dark:bg-orange-900/30',
    }
    return colors[status] ?? ''
  }

  const getInvoiceStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      draft: 'Brouillon',
      sent: 'Envoyée',
      paid: 'Payée',
      overdue: 'En retard',
      cancelled: 'Annulée',
    }
    return labels[status] ?? status
  }

  const calculateItemAmount = (quantity: number, unitPrice: number, discount: number): number => {
    const subtotal = quantity * unitPrice
    const discountAmount = (subtotal * discount) / 100
    return subtotal - discountAmount
  }

  const calculateItemTax = (amount: number, taxRate: number): number => {
    return (amount * taxRate) / 100
  }

  const calculateTotals = (items: Array<{ amount: number; taxRate: number }>) => {
    const subtotal = items.reduce((sum, item) => sum + item.amount, 0)
    const taxTotal = items.reduce(
      (sum, item) => sum + calculateItemTax(item.amount, item.taxRate),
      0
    )
    const total = subtotal + taxTotal
    return { subtotal, taxTotal, total }
  }

  onMounted(() => {
    store.fetchInvoices()
  })

  return {
    invoices,
    isLoading,
    error,
    draftInvoices,
    sentInvoices,
    paidInvoices,
    overdueInvoices,
    cancelledInvoices,
    totalRevenue,
    pendingRevenue,
    overdueAmount,
    invoiceSummaries,
    formatCurrency,
    formatDate,
    getInvoiceTypeLabel,
    getInvoiceStatusColor,
    getInvoiceStatusLabel,
    calculateItemAmount,
    calculateItemTax,
    calculateTotals,
    fetchInvoices: store.fetchInvoices,
    fetchInvoiceById: store.fetchInvoiceById,
    createInvoice: store.createInvoice,
    updateInvoice: store.updateInvoice,
    deleteInvoice: store.deleteInvoice,
    getInvoiceById: store.getInvoiceById,
  }
}
