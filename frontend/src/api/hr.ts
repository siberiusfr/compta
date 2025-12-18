import apiClient from './client'
import type { ApiResponse, PaginatedResponse } from '@types/index'

export interface Employee {
  id: string
  firstName: string
  lastName: string
  email: string
  position: string
  department: string
  salary: number
  hireDate: string
}

export const hrApi = {
  async getEmployees(page = 1, pageSize = 10): Promise<PaginatedResponse<Employee>> {
    const response = await apiClient.get<PaginatedResponse<Employee>>('/hr/employees', {
      params: { page, pageSize },
    })
    return response.data
  },

  async getEmployee(id: string): Promise<ApiResponse<Employee>> {
    const response = await apiClient.get<ApiResponse<Employee>>(`/hr/employees/${id}`)
    return response.data
  },

  async createEmployee(data: Omit<Employee, 'id'>): Promise<ApiResponse<Employee>> {
    const response = await apiClient.post<ApiResponse<Employee>>('/hr/employees', data)
    return response.data
  },

  async updateEmployee(id: string, data: Partial<Employee>): Promise<ApiResponse<Employee>> {
    const response = await apiClient.put<ApiResponse<Employee>>(`/hr/employees/${id}`, data)
    return response.data
  },

  async deleteEmployee(id: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete<ApiResponse<void>>(`/hr/employees/${id}`)
    return response.data
  },

  async getPayroll(): Promise<ApiResponse<any>> {
    const response = await apiClient.get<ApiResponse<any>>('/hr/payroll')
    return response.data
  },
}
