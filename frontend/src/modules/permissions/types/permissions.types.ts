export type UserStatus = 'active' | 'inactive' | 'pending' | 'suspended'

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  fullName: string
  avatar?: string
  status: UserStatus
  roles: string[]
  groups: string[]
  lastLogin?: Date
  createdAt: Date
  updatedAt: Date
}

export interface Role {
  id: string
  name: string
  description: string
  permissions: Permission[]
  userCount: number
  isSystem: boolean
  createdAt: Date
  updatedAt: Date
}

export interface Permission {
  id: string
  name: string
  description: string
  resource: string
  action: 'create' | 'read' | 'update' | 'delete' | 'manage'
}

export interface Group {
  id: string
  name: string
  description: string
  members: string[]
  roles: string[]
  createdAt: Date
  updatedAt: Date
}

export interface UserFilter {
  status?: UserStatus
  role?: string
  group?: string
  search?: string
}
