/**
 * Status constants for various entities
 */

export const INVOICE_STATUS = {
  DRAFT: 'draft',
  SENT: 'sent',
  PAID: 'paid',
  CANCELLED: 'cancelled',
  OVERDUE: 'overdue',
} as const

export const INVOICE_STATUS_LABELS: Record<string, string> = {
  [INVOICE_STATUS.DRAFT]: 'Brouillon',
  [INVOICE_STATUS.SENT]: 'Envoyée',
  [INVOICE_STATUS.PAID]: 'Payée',
  [INVOICE_STATUS.CANCELLED]: 'Annulée',
  [INVOICE_STATUS.OVERDUE]: 'En retard',
}

export const DOCUMENT_CATEGORIES = {
  INVOICES: 'invoices',
  CONTRACTS: 'contracts',
  REPORTS: 'reports',
  HR: 'hr',
  OTHER: 'other',
} as const

export const DOCUMENT_CATEGORY_LABELS: Record<string, string> = {
  [DOCUMENT_CATEGORIES.INVOICES]: 'Factures',
  [DOCUMENT_CATEGORIES.CONTRACTS]: 'Contrats',
  [DOCUMENT_CATEGORIES.REPORTS]: 'Rapports',
  [DOCUMENT_CATEGORIES.HR]: 'RH',
  [DOCUMENT_CATEGORIES.OTHER]: 'Autres',
}

export const USER_ROLES = {
  ADMIN: 'admin',
  MANAGER: 'manager',
  EMPLOYEE: 'employee',
  ACCOUNTANT: 'accountant',
} as const

export const USER_ROLE_LABELS: Record<string, string> = {
  [USER_ROLES.ADMIN]: 'Administrateur',
  [USER_ROLES.MANAGER]: 'Manager',
  [USER_ROLES.EMPLOYEE]: 'Employé',
  [USER_ROLES.ACCOUNTANT]: 'Comptable',
}
