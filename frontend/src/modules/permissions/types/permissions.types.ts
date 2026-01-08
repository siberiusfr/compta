export interface Role {
  id: string
  name: string
  description: string
  permissions: string[]
  userCount: number
  createdAt: Date
}

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  roleId: string
  roleName: string
  avatar?: string
  lastLogin?: Date
  createdAt: Date
  active: boolean
}

export interface AuditLog {
  id: string
  action: string
  userId: string
  userName: string
  resource: string
  resourceId: string
  timestamp: Date
  ip: string
}
