import type { Role, User, AuditLog } from '../types/permissions.types'

export const mockRoles: Role[] = [
  {
    id: '1',
    name: 'Administrateur',
    description: 'Accès complet à toutes les fonctionnalités',
    permissions: ['read:*', 'write:*', 'delete:*', 'admin:*'],
    userCount: 2,
    createdAt: new Date('2024-01-01T00:00:00')
  },
  {
    id: '2',
    name: 'Comptable',
    description: 'Accès aux fonctionnalités comptables',
    permissions: ['read:accounting', 'write:accounting', 'read:invoices', 'write:invoices'],
    userCount: 5,
    createdAt: new Date('2024-01-01T00:00:00')
  },
  {
    id: '3',
    name: 'RH',
    description: 'Accès aux fonctionnalités RH',
    permissions: ['read:hr', 'write:hr', 'read:employees', 'write:employees'],
    userCount: 3,
    createdAt: new Date('2024-01-01T00:00:00')
  },
  {
    id: '4',
    name: 'Utilisateur',
    description: 'Accès en lecture seule',
    permissions: ['read:*'],
    userCount: 12,
    createdAt: new Date('2024-01-01T00:00:00')
  }
]

export const mockUsers: User[] = [
  {
    id: '1',
    email: 'admin@compta.com',
    firstName: 'Marie',
    lastName: 'Dupont',
    roleId: '1',
    roleName: 'Administrateur',
    lastLogin: new Date('2024-01-09T08:30:00'),
    createdAt: new Date('2024-01-01T00:00:00'),
    active: true
  },
  {
    id: '2',
    email: 'pierre@compta.com',
    firstName: 'Pierre',
    lastName: 'Bernard',
    roleId: '2',
    roleName: 'Comptable',
    lastLogin: new Date('2024-01-08T16:45:00'),
    createdAt: new Date('2024-01-02T00:00:00'),
    active: true
  },
  {
    id: '3',
    email: 'sophie@compta.com',
    firstName: 'Sophie',
    lastName: 'Leroy',
    roleId: '3',
    roleName: 'RH',
    lastLogin: new Date('2024-01-09T09:15:00'),
    createdAt: new Date('2024-01-03T00:00:00'),
    active: true
  },
  {
    id: '4',
    email: 'jean@compta.com',
    firstName: 'Jean',
    lastName: 'Martin',
    roleId: '4',
    roleName: 'Utilisateur',
    lastLogin: new Date('2024-01-07T14:20:00'),
    createdAt: new Date('2024-01-04T00:00:00'),
    active: false
  }
]

export const mockAuditLogs: AuditLog[] = [
  {
    id: '1',
    action: 'UPDATE',
    userId: '1',
    userName: 'Marie Dupont',
    resource: 'Invoice',
    resourceId: 'INV-2024-001',
    timestamp: new Date('2024-01-09T10:30:00'),
    ip: '192.168.1.100'
  },
  {
    id: '2',
    action: 'CREATE',
    userId: '2',
    userName: 'Pierre Bernard',
    resource: 'Expense',
    resourceId: 'EXP-2024-001',
    timestamp: new Date('2024-01-09T09:15:00'),
    ip: '192.168.1.101'
  },
  {
    id: '3',
    action: 'DELETE',
    userId: '1',
    userName: 'Marie Dupont',
    resource: 'Document',
    resourceId: 'DOC-001',
    timestamp: new Date('2024-01-08T16:45:00'),
    ip: '192.168.1.100'
  },
  {
    id: '4',
    action: 'LOGIN',
    userId: '3',
    userName: 'Sophie Leroy',
    resource: 'Auth',
    resourceId: 'N/A',
    timestamp: new Date('2024-01-09T09:00:00'),
    ip: '192.168.1.102'
  }
]
