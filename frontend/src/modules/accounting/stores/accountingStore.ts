import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockJournalEntries, mockLedgerAccounts, mockInvoices, mockExpenses } from '../mock-data/accounting.mock'
import type { JournalEntry, LedgerAccount, Invoice, Expense } from '../types/accounting.types'

export const useAccountingStore = defineStore('accounting', () => {
  const journalEntries = ref<JournalEntry[]>(mockJournalEntries)
  const ledgerAccounts = ref<LedgerAccount[]>(mockLedgerAccounts)
  const invoices = ref<Invoice[]>(mockInvoices)
  const expenses = ref<Expense[]>(mockExpenses)

  const pendingInvoices = computed(() => invoices.value.filter(i => i.status === 'sent'))
  const paidInvoices = computed(() => invoices.value.filter(i => i.status === 'paid'))
  const totalRevenue = computed(() => invoices.value.filter(i => i.status === 'paid').reduce((sum, i) => sum + i.total, 0))

  function createInvoice(invoice: Omit<Invoice, 'id'>) {
    const newInvoice: Invoice = { ...invoice, id: Date.now().toString() }
    invoices.value.push(newInvoice)
    return newInvoice
  }

  function updateInvoice(id: string, updates: Partial<Invoice>) {
    const index = invoices.value.findIndex(i => i.id === id)
    if (index !== -1) {
      invoices.value[index] = { ...invoices.value[index], ...updates } as Invoice
    }
  }

  function createExpense(expense: Omit<Expense, 'id'>) {
    const newExpense: Expense = { ...expense, id: Date.now().toString() }
    expenses.value.push(newExpense)
    return newExpense
  }

  function updateExpense(id: string, updates: Partial<Expense>) {
    const index = expenses.value.findIndex(e => e.id === id)
    if (index !== -1) {
      expenses.value[index] = { ...expenses.value[index], ...updates } as Expense
    }
  }

  return {
    journalEntries,
    ledgerAccounts,
    invoices,
    expenses,
    pendingInvoices,
    paidInvoices,
    totalRevenue,
    createInvoice,
    updateInvoice,
    createExpense,
    updateExpense
  }
})
