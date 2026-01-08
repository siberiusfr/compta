import type { DashboardStats, RecentActivity, RevenueData } from '../types/dashboard.types'

export const mockDashboardStats: DashboardStats = {
  totalCompanies: 12,
  activeEmployees: 156,
  pendingInvoices: 23,
  totalRevenue: 1250000
}

export const mockRecentActivities: RecentActivity[] = [
  {
    id: '1',
    type: 'invoice',
    message: 'Facture #INV-2024-001 créée pour TechCorp',
    timestamp: new Date('2024-01-09T10:30:00'),
    user: 'Marie Dupont'
  },
  {
    id: '2',
    type: 'employee',
    message: 'Nouvel employé Jean Martin ajouté',
    timestamp: new Date('2024-01-09T09:15:00'),
    user: 'Pierre Bernard'
  },
  {
    id: '3',
    type: 'payment',
    message: 'Paiement reçu de StartupXYZ (5 000€)',
    timestamp: new Date('2024-01-08T16:45:00'),
    user: 'Sophie Leroy'
  },
  {
    id: '4',
    type: 'document',
    message: 'Document "Contrat Vendeur" uploadé',
    timestamp: new Date('2024-01-08T14:20:00'),
    user: 'Marie Dupont'
  },
  {
    id: '5',
    type: 'invoice',
    message: 'Facture #INV-2024-005 marquée comme payée',
    timestamp: new Date('2024-01-08T11:00:00'),
    user: 'Pierre Bernard'
  }
]

export const mockRevenueData: RevenueData[] = [
  { month: 'Août', revenue: 95000, expenses: 72000 },
  { month: 'Septembre', revenue: 110000, expenses: 78000 },
  { month: 'Octobre', revenue: 125000, expenses: 85000 },
  { month: 'Novembre', revenue: 118000, expenses: 82000 },
  { month: 'Décembre', revenue: 142000, expenses: 91000 },
  { month: 'Janvier', revenue: 128000, expenses: 87000 }
]
