export type InvoiceStatus = 'draft' | 'sent' | 'paid' | 'overdue' | 'cancelled'

export type InvoiceType = 'sale' | 'purchase' | 'credit_note' | 'debit_note'

export interface InvoiceItem {
  id: string
  productId?: string
  productName: string
  productCode?: string
  description?: string
  quantity: number
  unitPrice: number
  taxRate: number
  discount: number
  amount: number
  accountId: string
}

export interface Invoice {
  id: string
  invoiceNumber: string
  type: InvoiceType
  status: InvoiceStatus
  customerId: string
  customerName: string
  customerEmail?: string
  customerAddress?: {
    street: string
    city: string
    postalCode: string
    country: string
  }
  date: Date
  dueDate: Date
  items: InvoiceItem[]
  subtotal: number
  taxTotal: number
  discountTotal: number
  total: number
  currency: string
  notes?: string
  internalNotes?: string
  createdBy: string
  createdAt: Date
  updatedAt: Date
  sentAt?: Date
  paidAt?: Date
  cancelledAt?: Date
}

export interface InvoiceSummary {
  id: string
  invoiceNumber: string
  customerName: string
  date: Date
  dueDate: Date
  total: number
  status: InvoiceStatus
  amountDue: number
}
