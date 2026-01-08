import { defineStore } from 'pinia'

interface Invoice {
  id: string
  number: string
  client: string
  amount: number
  status: 'draft' | 'sent' | 'paid'
  date: string
}

interface Expense {
  id: string
  description: string
  amount: number
  category: string
  date: string
}

interface AccountingState {
  invoices: Invoice[]
  expenses: Expense[]
  loading: boolean
}

export const useAccountingStore = defineStore('accounting', {
  state: (): AccountingState => ({
    invoices: [],
    expenses: [],
    loading: false,
  }),

  getters: {
    totalRevenue: (state) => {
      return state.invoices
        .filter((inv) => inv.status === 'paid')
        .reduce((sum, inv) => sum + inv.amount, 0)
    },
    totalExpenses: (state) => {
      return state.expenses.reduce((sum, exp) => sum + exp.amount, 0)
    },
    balance: (state) => {
      const revenue = state.invoices
        .filter((inv) => inv.status === 'paid')
        .reduce((sum, inv) => sum + inv.amount, 0)
      const expenses = state.expenses.reduce((sum, exp) => sum + exp.amount, 0)
      return revenue - expenses
    },
  },

  actions: {
    async fetchInvoices() {
      this.loading = true
      try {
        // TODO: Replace with actual API call
        this.invoices = []
      } catch (error) {
        console.error('Error fetching invoices:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchExpenses() {
      this.loading = true
      try {
        // TODO: Replace with actual API call
        this.expenses = []
      } catch (error) {
        console.error('Error fetching expenses:', error)
      } finally {
        this.loading = false
      }
    },

    async createInvoice(invoice: Omit<Invoice, 'id'>) {
      try {
        // TODO: Replace with actual API call
        return { success: true }
      } catch (error) {
        console.error('Error creating invoice:', error)
        return { success: false, error }
      }
    },

    async createExpense(expense: Omit<Expense, 'id'>) {
      try {
        // TODO: Replace with actual API call
        return { success: true }
      } catch (error) {
        console.error('Error creating expense:', error)
        return { success: false, error }
      }
    },
  },
})
