import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Invoice, InvoiceSummary } from '../types/invoices.types'
import { mockInvoices } from '../mock-data/invoices.mock'

export const useInvoicesStore = defineStore('invoices', () => {
  const invoices = ref<Invoice[]>(mockInvoices)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const draftInvoices = computed(() => invoices.value.filter((inv) => inv.status === 'draft'))

  const sentInvoices = computed(() => invoices.value.filter((inv) => inv.status === 'sent'))

  const paidInvoices = computed(() => invoices.value.filter((inv) => inv.status === 'paid'))

  const overdueInvoices = computed(() => invoices.value.filter((inv) => inv.status === 'overdue'))

  const cancelledInvoices = computed(() =>
    invoices.value.filter((inv) => inv.status === 'cancelled')
  )

  const totalRevenue = computed(() => paidInvoices.value.reduce((sum, inv) => sum + inv.total, 0))

  const pendingRevenue = computed(() => sentInvoices.value.reduce((sum, inv) => sum + inv.total, 0))

  const overdueAmount = computed(() =>
    overdueInvoices.value.reduce((sum, inv) => sum + inv.total, 0)
  )

  const invoiceSummaries = computed<InvoiceSummary[]>(() =>
    invoices.value.map((inv) => ({
      id: inv.id,
      invoiceNumber: inv.invoiceNumber,
      customerName: inv.customerName,
      date: inv.date,
      dueDate: inv.dueDate,
      total: inv.total,
      status: inv.status,
      amountDue: inv.status === 'paid' ? 0 : inv.total,
    }))
  )

  async function fetchInvoices() {
    isLoading.value = true
    error.value = null
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))
      invoices.value = mockInvoices
    } catch (e) {
      error.value = 'Erreur lors du chargement des factures'
      console.error('[InvoicesStore] Fetch error:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchInvoiceById(id: string): Promise<Invoice | undefined> {
    isLoading.value = true
    try {
      await new Promise((resolve) => setTimeout(resolve, 200))
      return invoices.value.find((inv) => inv.id === id)
    } finally {
      isLoading.value = false
    }
  }

  async function createInvoice(
    invoiceData: Omit<Invoice, 'id' | 'invoiceNumber' | 'createdAt' | 'updatedAt'>
  ): Promise<Invoice> {
    isLoading.value = true
    error.value = null
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))

      const newInvoice: Invoice = {
        ...invoiceData,
        id: `inv-${Date.now()}`,
        invoiceNumber: `FAC-${new Date().getFullYear()}-${String(invoices.value.length + 1).padStart(3, '0')}`,
        createdAt: new Date(),
        updatedAt: new Date(),
      }

      invoices.value.unshift(newInvoice)
      return newInvoice
    } catch (e) {
      error.value = 'Erreur lors de la création de la facture'
      console.error('[InvoicesStore] Create error:', e)
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function updateInvoice(
    id: string,
    updates: Partial<Invoice>
  ): Promise<Invoice | undefined> {
    isLoading.value = true
    error.value = null
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))

      const index = invoices.value.findIndex((inv) => inv.id === id)
      if (index !== -1) {
        const existingInvoice = invoices.value[index]!
        invoices.value[index] = {
          ...existingInvoice,
          ...updates,
          id: existingInvoice.id,
          invoiceNumber: existingInvoice.invoiceNumber,
          type: updates.type ?? existingInvoice.type,
          status: updates.status ?? existingInvoice.status,
          customerId: updates.customerId ?? existingInvoice.customerId,
          customerName: updates.customerName ?? existingInvoice.customerName,
          date: updates.date ?? existingInvoice.date,
          dueDate: updates.dueDate ?? existingInvoice.dueDate,
          items: updates.items ?? existingInvoice.items,
          subtotal: updates.subtotal ?? existingInvoice.subtotal,
          taxTotal: updates.taxTotal ?? existingInvoice.taxTotal,
          discountTotal: updates.discountTotal ?? existingInvoice.discountTotal,
          total: updates.total ?? existingInvoice.total,
          currency: updates.currency ?? existingInvoice.currency,
          createdBy: existingInvoice.createdBy,
          createdAt: existingInvoice.createdAt,
          updatedAt: new Date(),
        }
        return invoices.value[index]
      }
      return undefined
    } catch (e) {
      error.value = 'Erreur lors de la mise à jour de la facture'
      console.error('[InvoicesStore] Update error:', e)
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function deleteInvoice(id: string): Promise<boolean> {
    isLoading.value = true
    error.value = null
    try {
      await new Promise((resolve) => setTimeout(resolve, 200))
      const index = invoices.value.findIndex((inv) => inv.id === id)
      if (index !== -1) {
        invoices.value.splice(index, 1)
        return true
      }
      return false
    } catch (e) {
      error.value = 'Erreur lors de la suppression de la facture'
      console.error('[InvoicesStore] Delete error:', e)
      throw e
    } finally {
      isLoading.value = false
    }
  }

  function getInvoiceById(id: string): Invoice | undefined {
    return invoices.value.find((inv) => inv.id === id)
  }

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
    fetchInvoices,
    fetchInvoiceById,
    createInvoice,
    updateInvoice,
    deleteInvoice,
    getInvoiceById,
  }
})
