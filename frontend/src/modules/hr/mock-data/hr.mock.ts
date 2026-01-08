import type { Employee, Contract, LeaveRequest, Payroll } from '../types/hr.types'

export const mockEmployees: Employee[] = [
  {
    id: '1',
    firstName: 'Thomas',
    lastName: 'Petit',
    email: 'thomas.petit@compta.com',
    phone: '+33 6 12 34 56 78',
    position: 'Développeur Senior',
    department: 'IT',
    startDate: new Date('2023-01-15'),
    salary: 55000,
    contractType: 'CDI',
    status: 'active'
  },
  {
    id: '2',
    firstName: 'Emma',
    lastName: 'Moreau',
    email: 'emma.moreau@compta.com',
    phone: '+33 6 98 76 54 32',
    position: 'Comptable',
    department: 'Finance',
    startDate: new Date('2023-03-01'),
    salary: 42000,
    contractType: 'CDI',
    status: 'active'
  },
  {
    id: '3',
    firstName: 'Lucas',
    lastName: 'Dubois',
    email: 'lucas.dubois@compta.com',
    phone: '+33 6 45 67 89 01',
    position: 'Stagiaire',
    department: 'Marketing',
    startDate: new Date('2024-01-08'),
    salary: 15000,
    contractType: 'Intern',
    status: 'active'
  }
]

export const mockContracts: Contract[] = [
  {
    id: 'c1',
    employeeId: '1',
    employeeName: 'Thomas Petit',
    type: 'CDI',
    startDate: new Date('2023-01-15'),
    salary: 55000,
    hoursPerWeek: 35,
    status: 'active'
  },
  {
    id: 'c2',
    employeeId: '2',
    employeeName: 'Emma Moreau',
    type: 'CDI',
    startDate: new Date('2023-03-01'),
    salary: 42000,
    hoursPerWeek: 35,
    status: 'active'
  }
]

export const mockLeaveRequests: LeaveRequest[] = [
  {
    id: 'l1',
    employeeId: '1',
    employeeName: 'Thomas Petit',
    type: 'paid',
    startDate: new Date('2024-01-15'),
    endDate: new Date('2024-01-26'),
    days: 10,
    reason: 'Vacances d\'hiver',
    status: 'approved',
    reviewedBy: 'Marie Dupont',
    reviewedAt: new Date('2024-01-08T10:00:00')
  },
  {
    id: 'l2',
    employeeId: '2',
    employeeName: 'Emma Moreau',
    type: 'sick',
    startDate: new Date('2024-01-10'),
    endDate: new Date('2024-01-12'),
    days: 3,
    reason: 'Maladie',
    status: 'pending'
  }
]

export const mockPayroll: Payroll[] = [
  {
    id: 'p1',
    employeeId: '1',
    employeeName: 'Thomas Petit',
    month: '2024-01',
    grossSalary: 55000,
    netSalary: 42000,
    taxes: 5000,
    socialContributions: 8000,
    status: 'validated'
  },
  {
    id: 'p2',
    employeeId: '2',
    employeeName: 'Emma Moreau',
    month: '2024-01',
    grossSalary: 42000,
    netSalary: 32000,
    taxes: 3500,
    socialContributions: 6500,
    status: 'draft'
  }
]
