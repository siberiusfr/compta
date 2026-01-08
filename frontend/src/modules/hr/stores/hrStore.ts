import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockEmployees, mockContracts, mockLeaveRequests, mockPayroll } from '../mock-data/hr.mock'
import type { Employee, Contract, LeaveRequest, Payroll } from '../types/hr.types'

export const useHrStore = defineStore('hr', () => {
  const employees = ref<Employee[]>(mockEmployees)
  const contracts = ref<Contract[]>(mockContracts)
  const leaveRequests = ref<LeaveRequest[]>(mockLeaveRequests)
  const payroll = ref<Payroll[]>(mockPayroll)

  const activeEmployees = computed(() => employees.value.filter(e => e.status === 'active'))
  const pendingLeaves = computed(() => leaveRequests.value.filter(l => l.status === 'pending'))

  function createEmployee(employee: Omit<Employee, 'id'>) {
    const newEmployee: Employee = { ...employee, id: Date.now().toString() }
    employees.value.push(newEmployee)
    return newEmployee
  }

  function updateEmployee(id: string, updates: Partial<Employee>) {
    const index = employees.value.findIndex(e => e.id === id)
    if (index !== -1) {
      employees.value[index] = { ...employees.value[index], ...updates } as Employee
    }
  }

  function createLeaveRequest(request: Omit<LeaveRequest, 'id'>) {
    const newRequest: LeaveRequest = { ...request, id: Date.now().toString() }
    leaveRequests.value.push(newRequest)
    return newRequest
  }

  function updateLeaveRequest(id: string, updates: Partial<LeaveRequest>) {
    const index = leaveRequests.value.findIndex(l => l.id === id)
    if (index !== -1) {
      leaveRequests.value[index] = { ...leaveRequests.value[index], ...updates } as LeaveRequest
    }
  }

  return {
    employees,
    contracts,
    leaveRequests,
    payroll,
    activeEmployees,
    pendingLeaves,
    createEmployee,
    updateEmployee,
    createLeaveRequest,
    updateLeaveRequest
  }
})
