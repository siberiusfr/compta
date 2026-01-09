// Types communs partagés entre tous les modules

export interface BaseEntity {
  id: string
  createdAt: Date
  updatedAt: Date
}

export interface PaginatedResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface ApiError {
  message: string
  code: string
  details?: Record<string, string[]>
}

export type Status = 'active' | 'inactive' | 'pending' | 'archived'

export interface SelectOption<T = string> {
  label: string
  value: T
  disabled?: boolean
}

export interface MenuItem {
  icon?: string
  label: string
  route?: string
  children?: MenuItem[]
  badge?: string | number
  badgeVariant?: 'default' | 'destructive' | 'secondary'
}

export interface BreadcrumbItem {
  label: string
  route?: string
}

export type SortDirection = 'asc' | 'desc'

export interface SortConfig {
  field: string
  direction: SortDirection
}

export interface FilterConfig {
  field: string
  operator: 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'contains' | 'startsWith' | 'endsWith'
  value: string | number | boolean | Date
}
