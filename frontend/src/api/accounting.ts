import apiClient from './client'
import type { ApiResponse, PaginatedResponse } from '@app-types/index'

export interface Invoice {
  id: string
  number: string
  client: string
  amount: number
  status: 'draft' | 'sent' | 'paid'
  date: string
}

export interface Expense {
  id: string
  description: string
  amount: number
  category: string
  date: string
}

export const accountingApi = {
  // Invoices
  async getInvoices(page = 1, pageSize = 10): Promise<PaginatedResponse<Invoice>> {
    const response = await apiClient.get<PaginatedResponse<Invoice>>('/accounting/invoices', {
      params: { page, pageSize },
    })
    return response.data
  },

  async getInvoice(id: string): Promise<ApiResponse<Invoice>> {
    const response = await apiClient.get<ApiResponse<Invoice>>(`/accounting/invoices/${id}`)
    return response.data
  },

  async createInvoice(data: Omit<Invoice, 'id'>): Promise<ApiResponse<Invoice>> {
    const response = await apiClient.post<ApiResponse<Invoice>>('/accounting/invoices', data)
    return response.data
  },

  async updateInvoice(id: string, data: Partial<Invoice>): Promise<ApiResponse<Invoice>> {
    const response = await apiClient.put<ApiResponse<Invoice>>(`/accounting/invoices/${id}`, data)
    return response.data
  },

  async deleteInvoice(id: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete<ApiResponse<void>>(`/accounting/invoices/${id}`)
    return response.data
  },

  // Expenses
  async getExpenses(page = 1, pageSize = 10): Promise<PaginatedResponse<Expense>> {
    const response = await apiClient.get<PaginatedResponse<Expense>>('/accounting/expenses', {
      params: { page, pageSize },
    })
    return response.data
  },

  async getExpense(id: string): Promise<ApiResponse<Expense>> {
    const response = await apiClient.get<ApiResponse<Expense>>(`/accounting/expenses/${id}`)
    return response.data
  },

  async createExpense(data: Omit<Expense, 'id'>): Promise<ApiResponse<Expense>> {
    const response = await apiClient.post<ApiResponse<Expense>>('/accounting/expenses', data)
    return response.data
  },

  async updateExpense(id: string, data: Partial<Expense>): Promise<ApiResponse<Expense>> {
    const response = await apiClient.put<ApiResponse<Expense>>(`/accounting/expenses/${id}`, data)
    return response.data
  },

  async deleteExpense(id: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete<ApiResponse<void>>(`/accounting/expenses/${id}`)
    return response.data
  },
}
