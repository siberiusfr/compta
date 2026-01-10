// Re-export types from generated API
export type {
  DocumentResponse,
  DocumentUpdateRequest,
  DocumentUploadRequest,
  DocumentVersionResponse,
  DocumentVersionUploadRequest,
  DocumentShareRequest,
  DocumentShareResponse,
  DocumentSearchRequest,
  CategoryResponse,
  CategoryRequest,
  TagResponse,
  TagRequest,
  MetadataRequest,
  DocumentShareRequestPermission,
} from '../api/generated'

// UI-specific types
export type ViewMode = 'grid' | 'list'

export interface DocumentFilter {
  categoryId?: number
  search?: string
  uploadedBy?: string
  tag?: string
}
