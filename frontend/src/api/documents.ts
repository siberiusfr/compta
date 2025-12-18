import apiClient from './client'
import type { ApiResponse, PaginatedResponse } from '@types/index'

export interface Document {
  id: string
  name: string
  type: string
  size: number
  uploadedAt: string
  uploadedBy: string
  category: string
  url: string
}

export const documentsApi = {
  async getDocuments(page = 1, pageSize = 10): Promise<PaginatedResponse<Document>> {
    const response = await apiClient.get<PaginatedResponse<Document>>('/documents', {
      params: { page, pageSize },
    })
    return response.data
  },

  async getDocument(id: string): Promise<ApiResponse<Document>> {
    const response = await apiClient.get<ApiResponse<Document>>(`/documents/${id}`)
    return response.data
  },

  async uploadDocument(file: File, category: string): Promise<ApiResponse<Document>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('category', category)

    const response = await apiClient.post<ApiResponse<Document>>('/documents', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  async deleteDocument(id: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete<ApiResponse<void>>(`/documents/${id}`)
    return response.data
  },

  async downloadDocument(id: string): Promise<Blob> {
    const response = await apiClient.get(`/documents/${id}/download`, {
      responseType: 'blob',
    })
    return response.data
  },
}
