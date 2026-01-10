export type DocumentType = 'invoice' | 'quote' | 'contract' | 'report' | 'other'
export type DocumentStatus = 'draft' | 'pending' | 'approved' | 'rejected' | 'archived'

export interface Document {
  id: string
  name: string
  type: DocumentType
  status: DocumentStatus
  description?: string
  fileUrl: string
  fileSize: number
  mimeType: string
  companyId?: string
  companyName?: string
  tags: string[]
  createdBy: string
  createdAt: Date
  updatedAt: Date
}

export interface Invoice extends Document {
  type: 'invoice'
  invoiceNumber: string
  clientName: string
  clientEmail?: string
  amount: number
  taxAmount: number
  totalAmount: number
  currency: string
  dueDate: Date
  paidAt?: Date
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

export interface Quote extends Document {
  type: 'quote'
  quoteNumber: string
  clientName: string
  amount: number
  validUntil: Date
  convertedToInvoice?: string
}

export interface Contract extends Document {
  type: 'contract'
  contractNumber: string
  partyName: string
  startDate: Date
  endDate?: Date
  value?: number
  renewalType: 'none' | 'auto' | 'manual'
}

export interface DocumentFilter {
  type?: DocumentType
  status?: DocumentStatus
  companyId?: string
  dateFrom?: Date
  dateTo?: Date
  search?: string
}
