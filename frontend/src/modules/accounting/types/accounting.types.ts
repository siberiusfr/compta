export interface JournalEntry {
  id: string
  date: Date
  reference: string
  description: string
  debit: number
  credit: number
  accountId: string
  accountName: string
}

export interface LedgerAccount {
  id: string
  code: string
  name: string
  type: 'asset' | 'liability' | 'equity' | 'revenue' | 'expense'
  balance: number
}

export interface Invoice {
  id: string
  number: string
  customerId: string
  customerName: string
  date: Date
  dueDate: Date
  amount: number
  tax: number
  total: number
  status: 'draft' | 'sent' | 'paid' | 'overdue' | 'cancelled'
  items: InvoiceItem[]
}

export interface InvoiceItem {
  id: string
  description: string
  quantity: number
  unitPrice: number
  taxRate: number
  total: number
}

export interface Expense {
  id: string
  date: Date
  description: string
  amount: number
  tax: number
  category: string
  vendor: string
  status: 'pending' | 'validated' | 'paid'
  receiptUrl?: string
}
