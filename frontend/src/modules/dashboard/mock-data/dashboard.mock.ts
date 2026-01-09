import type { DashboardStats, RecentActivity, QuickAction } from '../types/dashboard.types'

export const mockDashboardStats: DashboardStats = {
  totalCompanies: 12,
  totalEmployees: 156,
  totalDocuments: 1247,
  pendingInvoices: 23,
  revenue: 458750.00,
  expenses: 312450.00
}

export const mockRecentActivities: RecentActivity[] = [
  {
    id: '1',
    type: 'invoice',
    title: 'Facture FA-2024-0156 creee',
    description: 'Client: Entreprise ABC - 12,500.00 EUR',
    timestamp: new Date(Date.now() - 1000 * 60 * 5),
    user: 'Marie Dupont'
  },
  {
    id: '2',
    type: 'employee',
    title: 'Nouvel employe ajoute',
    description: 'Jean Martin - Developpeur Senior',
    timestamp: new Date(Date.now() - 1000 * 60 * 30),
    user: 'Admin RH'
  },
  {
    id: '3',
    type: 'payment',
    title: 'Paiement recu',
    description: 'Facture FA-2024-0142 - 8,750.00 EUR',
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
    user: 'Systeme'
  },
  {
    id: '4',
    type: 'document',
    title: 'Document televerse',
    description: 'Contrat de service - Client XYZ',
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4),
    user: 'Pierre Bernard'
  },
  {
    id: '5',
    type: 'company',
    title: 'Nouvelle entreprise creee',
    description: 'Tech Solutions SARL',
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
    user: 'Admin'
  }
]

export const mockQuickActions: QuickAction[] = [
  {
    icon: 'Plus',
    label: 'Nouvelle facture',
    route: '/documents/invoices/new',
    color: 'bg-blue-500'
  },
  {
    icon: 'UserPlus',
    label: 'Ajouter employe',
    route: '/hr/employees/new',
    color: 'bg-green-500'
  },
  {
    icon: 'Building2',
    label: 'Nouvelle entreprise',
    route: '/companies/new',
    color: 'bg-purple-500'
  },
  {
    icon: 'FileText',
    label: 'Nouveau document',
    route: '/documents/new',
    color: 'bg-orange-500'
  }
]

export const mockMonthlyRevenue = {
  labels: ['Jan', 'Fev', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Aout', 'Sep', 'Oct', 'Nov', 'Dec'],
  data: [42000, 48000, 45000, 52000, 49000, 58000, 55000, 62000, 59000, 68000, 72000, 78000]
}
