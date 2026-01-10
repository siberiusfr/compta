import type { Document, Invoice, Quote, Contract } from '../types/documents.types'

export const mockDocuments: Document[] = [
  {
    id: '1',
    name: 'Rapport annuel 2024.pdf',
    type: 'report',
    status: 'approved',
    description: 'Rapport annuel de l\'entreprise',
    fileUrl: '/documents/rapport-2024.pdf',
    fileSize: 2456789,
    mimeType: 'application/pdf',
    companyId: '1',
    companyName: 'Tech Solutions SARL',
    tags: ['annuel', '2024', 'rapport'],
    createdBy: 'Marie Dupont',
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date('2024-01-20')
  },
  {
    id: '2',
    name: 'Conditions generales.docx',
    type: 'other',
    status: 'approved',
    fileUrl: '/documents/cgv.docx',
    fileSize: 145678,
    mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    tags: ['legal', 'cgv'],
    createdBy: 'Admin',
    createdAt: new Date('2024-02-10'),
    updatedAt: new Date('2024-02-10')
  }
]

export const mockInvoices: Invoice[] = [
  {
    id: 'inv-1',
    name: 'Facture FA-2024-0156.pdf',
    type: 'invoice',
    status: 'pending',
    invoiceNumber: 'FA-2024-0156',
    clientName: 'Entreprise ABC',
    clientEmail: 'contact@abc.com',
    amount: 10000,
    taxAmount: 2000,
    totalAmount: 12000,
    currency: 'EUR',
    dueDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
    fileUrl: '/invoices/FA-2024-0156.pdf',
    fileSize: 98765,
    mimeType: 'application/pdf',
    companyId: '1',
    companyName: 'Tech Solutions SARL',
    tags: ['facture', 'client'],
    createdBy: 'Marie Dupont',
    createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    items: [
      {
        id: 'item-1',
        description: 'Developpement application web',
        quantity: 40,
        unitPrice: 200,
        taxRate: 20,
        total: 8000
      },
      {
        id: 'item-2',
        description: 'Maintenance mensuelle',
        quantity: 1,
        unitPrice: 2000,
        taxRate: 20,
        total: 2000
      }
    ]
  },
  {
    id: 'inv-2',
    name: 'Facture FA-2024-0155.pdf',
    type: 'invoice',
    status: 'approved',
    invoiceNumber: 'FA-2024-0155',
    clientName: 'Client XYZ',
    amount: 5000,
    taxAmount: 1000,
    totalAmount: 6000,
    currency: 'EUR',
    dueDate: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000),
    paidAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    fileUrl: '/invoices/FA-2024-0155.pdf',
    fileSize: 87654,
    mimeType: 'application/pdf',
    tags: ['facture', 'paye'],
    createdBy: 'Pierre Martin',
    createdAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    items: [
      {
        id: 'item-3',
        description: 'Consulting IT',
        quantity: 25,
        unitPrice: 200,
        taxRate: 20,
        total: 5000
      }
    ]
  }
]

export const mockQuotes: Quote[] = [
  {
    id: 'quote-1',
    name: 'Devis DEV-2024-0045.pdf',
    type: 'quote',
    status: 'pending',
    quoteNumber: 'DEV-2024-0045',
    clientName: 'Nouveau Client SA',
    amount: 25000,
    validUntil: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000),
    fileUrl: '/quotes/DEV-2024-0045.pdf',
    fileSize: 76543,
    mimeType: 'application/pdf',
    tags: ['devis', 'prospect'],
    createdBy: 'Sophie Bernard',
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
  },
  {
    id: 'quote-2',
    name: 'Devis DEV-2024-0044.pdf',
    type: 'quote',
    status: 'approved',
    quoteNumber: 'DEV-2024-0044',
    clientName: 'Entreprise ABC',
    amount: 12000,
    validUntil: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    convertedToInvoice: 'FA-2024-0156',
    fileUrl: '/quotes/DEV-2024-0044.pdf',
    fileSize: 65432,
    mimeType: 'application/pdf',
    tags: ['devis', 'converti'],
    createdBy: 'Marie Dupont',
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
    updatedAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000)
  }
]

export const mockContracts: Contract[] = [
  {
    id: 'contract-1',
    name: 'Contrat de maintenance.pdf',
    type: 'contract',
    status: 'approved',
    contractNumber: 'CTR-2024-0012',
    partyName: 'Entreprise ABC',
    startDate: new Date('2024-01-01'),
    endDate: new Date('2024-12-31'),
    value: 24000,
    renewalType: 'auto',
    fileUrl: '/contracts/CTR-2024-0012.pdf',
    fileSize: 234567,
    mimeType: 'application/pdf',
    companyId: '1',
    companyName: 'Tech Solutions SARL',
    tags: ['maintenance', 'annuel'],
    createdBy: 'Admin',
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-01')
  }
]
