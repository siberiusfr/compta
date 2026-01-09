export type EmployeeStatus = 'active' | 'inactive' | 'onLeave' | 'terminated'
export type ContractType = 'cdi' | 'cdd' | 'internship' | 'freelance' | 'apprenticeship'
export type LeaveType = 'paid' | 'sick' | 'parental' | 'unpaid' | 'other'
export type LeaveStatus = 'pending' | 'approved' | 'rejected' | 'cancelled'

export interface Employee {
  id: string
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  dateOfBirth?: Date
  hireDate: Date
  department: string
  position: string
  managerId?: string
  managerName?: string
  status: EmployeeStatus
  companyId: string
  companyName: string
  avatar?: string
  salary?: number
  contractType: ContractType
  createdAt: Date
  updatedAt: Date
}

export interface EmployeeContract {
  id: string
  employeeId: string
  type: ContractType
  startDate: Date
  endDate?: Date
  salary: number
  hoursPerWeek: number
  position: string
  department: string
  signedAt?: Date
  status: 'draft' | 'active' | 'expired' | 'terminated'
}

export interface LeaveRequest {
  id: string
  employeeId: string
  employeeName: string
  type: LeaveType
  startDate: Date
  endDate: Date
  days: number
  reason?: string
  status: LeaveStatus
  approvedBy?: string
  approvedAt?: Date
  createdAt: Date
}

export interface PayrollEntry {
  id: string
  employeeId: string
  employeeName: string
  period: string
  grossSalary: number
  netSalary: number
  deductions: number
  taxes: number
  status: 'draft' | 'validated' | 'paid'
  paidAt?: Date
  createdAt: Date
}
