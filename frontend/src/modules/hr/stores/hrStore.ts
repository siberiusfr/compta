import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Employee, EmployeeContract, LeaveRequest, PayrollEntry } from '../types/hr.types'
import { mockEmployees, mockContracts, mockLeaveRequests, mockPayrollEntries } from '../mock-data/hr.mock'

export const useHrStore = defineStore('hr', () => {
  const employees = ref<Employee[]>(mockEmployees)
  const contracts = ref<EmployeeContract[]>(mockContracts)
  const leaveRequests = ref<LeaveRequest[]>(mockLeaveRequests)
  const payrollEntries = ref<PayrollEntry[]>(mockPayrollEntries)
  const isLoading = ref(false)

  const activeEmployees = computed(() =>
    employees.value.filter(e => e.status === 'active')
  )

  const pendingLeaves = computed(() =>
    leaveRequests.value.filter(l => l.status === 'pending')
  )

  const totalPayroll = computed(() =>
    payrollEntries.value
      .filter(p => p.status === 'paid')
      .reduce((sum, p) => sum + p.grossSalary, 0)
  )

  const employeesByDepartment = computed(() => {
    const groups: Record<string, Employee[]> = {}
    employees.value.forEach(emp => {
      if (!groups[emp.department]) {
        groups[emp.department] = []
      }
      groups[emp.department]!.push(emp)
    })
    return groups
  })

  async function fetchEmployees() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      employees.value = mockEmployees
    } finally {
      isLoading.value = false
    }
  }

  async function fetchLeaveRequests() {
    isLoading.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 300))
      leaveRequests.value = mockLeaveRequests
    } finally {
      isLoading.value = false
    }
  }

  function approveLeave(id: string) {
    const leave = leaveRequests.value.find(l => l.id === id)
    if (leave) {
      leave.status = 'approved'
      leave.approvedAt = new Date()
      leave.approvedBy = 'Admin'
    }
  }

  function rejectLeave(id: string) {
    const leave = leaveRequests.value.find(l => l.id === id)
    if (leave) {
      leave.status = 'rejected'
    }
  }

  return {
    employees,
    contracts,
    leaveRequests,
    payrollEntries,
    isLoading,
    activeEmployees,
    pendingLeaves,
    totalPayroll,
    employeesByDepartment,
    fetchEmployees,
    fetchLeaveRequests,
    approveLeave,
    rejectLeave
  }
})
