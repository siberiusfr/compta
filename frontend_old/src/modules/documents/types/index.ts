export type DocumentCategory = 'invoices' | 'contracts' | 'reports' | 'hr' | 'other'

export type DocumentStatus = 'pending' | 'approved' | 'rejected' | 'archived'

export interface Document {
  id: string
  name: string
  originalName: string
  type: string
  mimeType: string
  size: number
  category: DocumentCategory
  status: DocumentStatus
  url: string
  thumbnailUrl?: string
  uploadedAt: string
  uploadedBy: string
  uploadedByName?: string
  lastModified?: string
  tags?: string[]
  metadata?: DocumentMetadata
  version?: number
  parentId?: string
  isPublic?: boolean
}

export interface DocumentMetadata {
  description?: string
  author?: string
  relatedTo?: string
  expiresAt?: string
  [key: string]: any
}

export interface DocumentUploadOptions {
  category: DocumentCategory
  tags?: string[]
  description?: string
  metadata?: Record<string, any>
}

export interface DocumentFilter {
  category?: DocumentCategory
  status?: DocumentStatus
  uploadedBy?: string
  dateFrom?: string
  dateTo?: string
  search?: string
  tags?: string[]
}

export interface DocumentStats {
  totalDocuments: number
  totalSize: number
  documentsByCategory: Record<DocumentCategory, number>
  recentDocuments: Document[]
}

export interface DocumentsState {
  documents: Document[]
  loading: boolean
  uploadProgress?: number
  stats?: DocumentStats
}
