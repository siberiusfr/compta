export interface Employee {
  id: string
  firstName: string
  lastName: string
  email: string
  phone?: string
  position: string
  department: Department
  salary: number
  hireDate: string
  birthDate?: string
  address?: Address
  emergencyContact?: EmergencyContact
  status: EmployeeStatus
  managerId?: string
  notes?: string
  createdAt?: string
  updatedAt?: string
}

export type Department =
  | 'administration'
  | 'finance'
  | 'hr'
  | 'it'
  | 'sales'
  | 'marketing'
  | 'operations'
  | 'other'

export type EmployeeStatus = 'active' | 'on_leave' | 'suspended' | 'terminated'

export interface Address {
  street: string
  city: string
  state?: string
  postalCode: string
  country: string
}

export interface EmergencyContact {
  name: string
  relationship: string
  phone: string
  email?: string
}

export interface PayrollEntry {
  id: string
  employeeId: string
  period: string
  baseSalary: number
  bonuses?: number
  deductions?: number
  socialCharges: number
  netSalary: number
  status: 'pending' | 'processed' | 'paid'
  paidAt?: string
}

export interface HRStats {
  totalEmployees: number
  activeEmployees: number
  totalPayroll: number
  averageSalary: number
  employeesByDepartment: Record<Department, number>
}

export interface HRState {
  employees: Employee[]
  loading: boolean
  stats?: HRStats
}
