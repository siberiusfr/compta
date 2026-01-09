import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Account, JournalEntry, TrialBalanceEntry, FiscalYear } from '../types/accounting.types'
import { mockAccounts, mockJournalEntries, mockTrialBalance, mockFiscalYears } from '../mock-data/accounting.mock'

export const useAccountingStore = defineStore('accounting', () => {
  const accounts = ref<Account[]>(mockAccounts)
  const journalEntries = ref<JournalEntry[]>(mockJournalEntries)
  const trialBalance = ref<TrialBalanceEntry[]>(mockTrialBalance)
  const fiscalYears = ref<FiscalYear[]>(mockFiscalYears)
  const isLoading = ref(false)

  const currentFiscalYear = computed(() =>
    fiscalYears.value.find(fy => fy.isCurrent)
  )

  const activeAccounts = computed(() =>
    accounts.value.filter(a => a.isActive)
  )

  const accountsByType = computed(() => {
    const groups: Record<string, Account[]> = {
      asset: [],
      liability: [],
      equity: [],
      revenue: [],
      expense: []
    }
    accounts.value.forEach(acc => {
      groups[acc.type]!.push(acc)
    })
    return groups
  })

  const draftEntries = computed(() =>
    journalEntries.value.filter(e => e.status === 'draft')
  )

  const postedEntries = computed(() =>
    journalEntries.value.filter(e => e.status === 'posted')
  )

  const totalAssets = computed(() =>
    accounts.value
      .filter(a => a.type === 'asset')
      .reduce((sum, a) => sum + a.balance, 0)
  )

  const totalLiabilities = computed(() =>
    Math.abs(accounts.value
      .filter(a => a.type === 'liability')
      .reduce((sum, a) => sum + a.balance, 0))
  )

  const totalEquity = computed(() =>
    Math.abs(accounts.value
      .filter(a => a.type === 'equity')
      .reduce((sum, a) => sum + a.balance, 0))
  )

  const totalRevenue = computed(() =>
    Math.abs(accounts.value
      .filter(a => a.type === 'revenue')
      .reduce((sum, a) => sum + a.balance, 0))
  )

  const totalExpenses = computed(() =>
    accounts.value
      .filter(a => a.type === 'expense')
      .reduce((sum, a) => sum + a.balance, 0)
  )

  const netIncome = computed(() => totalRevenue.value - totalExpenses.value)

  async function fetchAccounts() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      accounts.value = mockAccounts
    } finally {
      isLoading.value = false
    }
  }

  async function fetchJournalEntries() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      journalEntries.value = mockJournalEntries
    } finally {
      isLoading.value = false
    }
  }

  function postEntry(id: string) {
    const entry = journalEntries.value.find(e => e.id === id)
    if (entry && entry.status === 'draft') {
      entry.status = 'posted'
      entry.postedAt = new Date()
    }
  }

  function getAccountBalance(accountId: string): number {
    const account = accounts.value.find(a => a.id === accountId)
    return account?.balance ?? 0
  }

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
    fetchAccounts,
    fetchJournalEntries,
    postEntry,
    getAccountBalance
  }
})
