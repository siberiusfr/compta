export type InvoiceStatus = 'draft' | 'sent' | 'paid' | 'cancelled' | 'overdue'

export interface Invoice {
  id: string
  number: string
  client: string
  clientEmail?: string
  clientAddress?: string
  amount: number
  tax?: number
  totalAmount?: number
  status: InvoiceStatus
  date: string
  dueDate?: string
  items?: InvoiceItem[]
  notes?: string
  createdAt?: string
  updatedAt?: string
}

export interface InvoiceItem {
  id: string
  description: string
  quantity: number
  unitPrice: number
  total: number
  tax?: number
}

export interface Expense {
  id: string
  description: string
  amount: number
  category: ExpenseCategory
  date: string
  vendor?: string
  receipt?: string
  status?: 'pending' | 'approved' | 'rejected'
  approvedBy?: string
  notes?: string
  createdAt?: string
  updatedAt?: string
}

export type ExpenseCategory =
  | 'office'
  | 'travel'
  | 'equipment'
  | 'software'
  | 'utilities'
  | 'marketing'
  | 'other'

export interface AccountingStats {
  totalRevenue: number
  totalExpenses: number
  balance: number
  pendingInvoices: number
  overdueInvoices: number
}

export interface AccountingState {
  invoices: Invoice[]
  expenses: Expense[]
  loading: boolean
  stats?: AccountingStats
}
