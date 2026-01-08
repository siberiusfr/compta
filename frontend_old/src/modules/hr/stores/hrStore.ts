import { defineStore } from 'pinia'

interface Employee {
  id: string
  firstName: string
  lastName: string
  email: string
  position: string
  department: string
  salary: number
  hireDate: string
}

interface HRState {
  employees: Employee[]
  loading: boolean
}

export const useHRStore = defineStore('hr', {
  state: (): HRState => ({
    employees: [],
    loading: false,
  }),

  getters: {
    employeeCount: (state) => state.employees.length,
    totalPayroll: (state) => {
      return state.employees.reduce((sum, emp) => sum + emp.salary, 0)
    },
    employeesByDepartment: (state) => {
      return state.employees.reduce(
        (acc, emp) => {
          if (!acc[emp.department]) {
            acc[emp.department] = []
          }
          acc[emp.department].push(emp)
          return acc
        },
        {} as Record<string, Employee[]>
      )
    },
  },

  actions: {
    async fetchEmployees() {
      this.loading = true
      try {
        // TODO: Replace with actual API call
        this.employees = []
      } catch (error) {
        console.error('Error fetching employees:', error)
      } finally {
        this.loading = false
      }
    },

    async createEmployee(employee: Omit<Employee, 'id'>) {
      try {
        // TODO: Replace with actual API call
        return { success: true }
      } catch (error) {
        console.error('Error creating employee:', error)
        return { success: false, error }
      }
    },

    async updateEmployee(id: string, employee: Partial<Employee>) {
      try {
        // TODO: Replace with actual API call
        return { success: true }
      } catch (error) {
        console.error('Error updating employee:', error)
        return { success: false, error }
      }
    },

    async deleteEmployee(id: string) {
      try {
        // TODO: Replace with actual API call
        return { success: true }
      } catch (error) {
        console.error('Error deleting employee:', error)
        return { success: false, error }
      }
    },
  },
})
