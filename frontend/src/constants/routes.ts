/**
 * Route name constants
 */

export const ROUTE_NAMES = {
  // Auth
  LOGIN: 'login',
  REGISTER: 'register',

  // Accounting
  ACCOUNTING: 'accounting',
  ACCOUNTING_INVOICES: 'accounting-invoices',
  ACCOUNTING_EXPENSES: 'accounting-expenses',

  // HR
  HR: 'hr',
  HR_EMPLOYEES: 'hr-employees',
  HR_PAYROLL: 'hr-payroll',

  // Errors
  NOT_FOUND: 'not-found',
  FORBIDDEN: 'forbidden',
  SERVER_ERROR: 'server-error',
} as const

export const ROUTE_PATHS = {
  LOGIN: '/login',
  REGISTER: '/register',
  ACCOUNTING: '/accounting',
  HR: '/hr',
} as const
