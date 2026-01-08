import type { JournalEntry, LedgerAccount, Invoice, Expense } from '../types/accounting.types'

export const mockJournalEntries: JournalEntry[] = [
  {
    id: '1',
    date: new Date('2024-01-09'),
    reference: 'JE-2024-001',
    description: 'Vente de services',
    debit: 12500,
    credit: 0,
    accountId: '411',
    accountName: 'Clients'
  },
  {
    id: '2',
    date: new Date('2024-01-09'),
    reference: 'JE-2024-001',
    description: 'Vente de services',
    debit: 0,
    credit: 12500,
    accountId: '707',
    accountName: 'Ventes de services'
  },
  {
    id: '3',
    date: new Date('2024-01-08'),
    reference: 'JE-2024-002',
    description: 'Achat de fournitures',
    debit: 0,
    credit: 2500,
    accountId: '401',
    accountName: 'Fournisseurs'
  },
  {
    id: '4',
    date: new Date('2024-01-08'),
    reference: 'JE-2024-002',
    description: 'Achat de fournitures',
    debit: 2500,
    credit: 0,
    accountId: '606',
    accountName: 'Fournitures'
  }
]

export const mockLedgerAccounts: LedgerAccount[] = [
  {
    id: '1',
    code: '101',
    name: 'Capital',
    type: 'equity',
    balance: 100000
  },
  {
    id: '2',
    code: '401',
    name: 'Fournisseurs',
    type: 'liability',
    balance: -45000
  },
  {
    id: '3',
    code: '411',
    name: 'Clients',
    type: 'asset',
    balance: 85000
  },
  {
    id: '4',
    code: '512',
    name: 'Banque',
    type: 'asset',
    balance: 125000
  },
  {
    id: '5',
    code: '606',
    name: 'Fournitures',
    type: 'expense',
    balance: 12000
  },
  {
    id: '6',
    code: '707',
    name: 'Ventes de services',
    type: 'revenue',
    balance: -156000
  }
]

export const mockInvoices: Invoice[] = [
  {
    id: '1',
    number: 'FAC-2024-001',
    customerId: '1',
    customerName: 'TechCorp',
    date: new Date('2024-01-09'),
    dueDate: new Date('2024-02-08'),
    amount: 10000,
    tax: 2000,
    total: 12000,
    status: 'sent',
    items: [
      {
        id: 'i1',
        description: 'Développement web',
        quantity: 40,
        unitPrice: 250,
        taxRate: 20,
        total: 12000
      }
    ]
  },
  {
    id: '2',
    number: 'FAC-2024-002',
    customerId: '2',
    customerName: 'StartupXYZ',
    date: new Date('2024-01-05'),
    dueDate: new Date('2024-02-04'),
    amount: 5000,
    tax: 1000,
    total: 6000,
    status: 'paid',
    items: [
      {
        id: 'i2',
        description: 'Consulting',
        quantity: 20,
        unitPrice: 250,
        taxRate: 20,
        total: 6000
      }
    ]
  }
]

export const mockExpenses: Expense[] = [
  {
    id: '1',
    date: new Date('2024-01-09'),
    description: 'Achat de matériel informatique',
    amount: 2500,
    tax: 500,
    category: 'Équipement',
    vendor: 'TechStore',
    status: 'validated'
  },
  {
    id: '2',
    date: new Date('2024-01-08'),
    description: 'Abonnement logiciel',
    amount: 150,
    tax: 30,
    category: 'Logiciels',
    vendor: 'SaaS Corp',
    status: 'paid'
  },
  {
    id: '3',
    date: new Date('2024-01-07'),
    description: 'Note de restaurant',
    amount: 85,
    tax: 17,
    category: 'Déplacements',
    vendor: 'Restaurant Paris',
    status: 'pending'
  }
]
