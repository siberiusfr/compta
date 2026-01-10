import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Document, Invoice, Quote, Contract, DocumentFilter } from '../types/documents.types'
import { mockDocuments, mockInvoices, mockQuotes, mockContracts } from '../mock-data/documents.mock'

export const useDocumentsStore = defineStore('documents', () => {
  const documents = ref<Document[]>(mockDocuments)
  const invoices = ref<Invoice[]>(mockInvoices)
  const quotes = ref<Quote[]>(mockQuotes)
  const contracts = ref<Contract[]>(mockContracts)
  const isLoading = ref(false)
  const filter = ref<DocumentFilter>({})

  const allDocuments = computed(() => [
    ...documents.value,
    ...invoices.value,
    ...quotes.value,
    ...contracts.value
  ])

  const pendingInvoices = computed(() =>
    invoices.value.filter(inv => inv.status === 'pending')
  )

  const pendingQuotes = computed(() =>
    quotes.value.filter(q => q.status === 'pending')
  )

  const activeContracts = computed(() =>
    contracts.value.filter(c => c.status === 'approved')
  )

  const totalInvoiced = computed(() =>
    invoices.value.reduce((sum, inv) => sum + inv.totalAmount, 0)
  )

  const totalPending = computed(() =>
    pendingInvoices.value.reduce((sum, inv) => sum + inv.totalAmount, 0)
  )

  async function fetchDocuments() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      documents.value = mockDocuments
    } finally {
      isLoading.value = false
    }
  }

  async function fetchInvoices() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      invoices.value = mockInvoices
    } finally {
      isLoading.value = false
    }
  }

  async function fetchQuotes() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      quotes.value = mockQuotes
    } finally {
      isLoading.value = false
    }
  }

  async function fetchContracts() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      contracts.value = mockContracts
    } finally {
      isLoading.value = false
    }
  }

  function setFilter(newFilter: DocumentFilter) {
    filter.value = { ...filter.value, ...newFilter }
  }

  function clearFilter() {
    filter.value = {}
  }

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
    fetchDocuments,
    fetchInvoices,
    fetchQuotes,
    fetchContracts,
    setFilter,
    clearFilter
  }
})
