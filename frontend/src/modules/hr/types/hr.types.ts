export interface Employee {
  id: string
  firstName: string
  lastName: string
  email: string
  phone: string
  position: string
  department: string
  startDate: Date
  salary: number
  avatar?: string
  contractType: 'CDI' | 'CDD' | 'Freelance' | 'Intern'
  status: 'active' | 'on_leave' | 'terminated'
}

export interface Contract {
  id: string
  employeeId: string
  employeeName: string
  type: 'CDI' | 'CDD' | 'Freelance' | 'Intern'
  startDate: Date
  endDate?: Date
  salary: number
  hoursPerWeek: number
  documentUrl?: string
  status: 'active' | 'expired' | 'terminated'
}

export interface LeaveRequest {
  id: string
  employeeId: string
  employeeName: string
  type: 'paid' | 'unpaid' | 'sick' | 'personal'
  startDate: Date
  endDate: Date
  days: number
  reason: string
  status: 'pending' | 'approved' | 'rejected'
  reviewedBy?: string
  reviewedAt?: Date
}

export interface Payroll {
  id: string
  employeeId: string
  employeeName: string
  month: string
  grossSalary: number
  netSalary: number
  taxes: number
  socialContributions: number
  status: 'draft' | 'validated' | 'paid'
  paidAt?: Date
}
