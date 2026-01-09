export type AccountType = 'asset' | 'liability' | 'equity' | 'revenue' | 'expense'

export interface Account {
  id: string
  code: string
  name: string
  type: AccountType
  parentId?: string
  balance: number
  isActive: boolean
  description?: string
  createdAt: Date
  updatedAt: Date
}

export interface JournalEntry {
  id: string
  date: Date
  reference: string
  description: string
  lines: JournalLine[]
  status: 'draft' | 'posted' | 'cancelled'
  totalDebit: number
  totalCredit: number
  createdBy: string
  createdAt: Date
  postedAt?: Date
}

export interface JournalLine {
  id: string
  accountId: string
  accountCode: string
  accountName: string
  debit: number
  credit: number
  description?: string
}

export interface LedgerEntry {
  date: Date
  reference: string
  description: string
  debit: number
  credit: number
  balance: number
}

export interface TrialBalanceEntry {
  accountCode: string
  accountName: string
  accountType: AccountType
  openingDebit: number
  openingCredit: number
  periodDebit: number
  periodCredit: number
  closingDebit: number
  closingCredit: number
}

export interface BalanceSheetItem {
  code: string
  name: string
  amount: number
  children?: BalanceSheetItem[]
}

export interface IncomeStatementItem {
  code: string
  name: string
  amount: number
  children?: IncomeStatementItem[]
}

export interface FiscalYear {
  id: string
  name: string
  startDate: Date
  endDate: Date
  status: 'open' | 'closed'
  isCurrent: boolean
}
