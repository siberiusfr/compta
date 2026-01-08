export interface DashboardStats {
  totalCompanies: number
  activeEmployees: number
  pendingInvoices: number
  totalRevenue: number
}

export interface RecentActivity {
  id: string
  type: 'invoice' | 'employee' | 'document' | 'payment'
  message: string
  timestamp: Date
  user: string
}

export interface RevenueData {
  month: string
  revenue: number
  expenses: number
}
