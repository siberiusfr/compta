export interface DashboardStats {
  totalCompanies: number
  totalEmployees: number
  totalDocuments: number
  pendingInvoices: number
  revenue: number
  expenses: number
}

export interface RecentActivity {
  id: string
  type: 'invoice' | 'document' | 'employee' | 'company' | 'payment'
  title: string
  description: string
  timestamp: Date
  user: string
}

export interface ChartData {
  labels: string[]
  datasets: {
    label: string
    data: number[]
    backgroundColor?: string
    borderColor?: string
  }[]
}

export interface QuickAction {
  icon: string
  label: string
  route: string
  color: string
}
