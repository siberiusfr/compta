import type { MenuItem } from '@/shared/types/common.types'

export const menuItems: MenuItem[] = [
  {
    icon: 'LayoutDashboard',
    label: 'Dashboard',
    route: '/dashboard',
  },
  {
    icon: 'Bell',
    label: 'Notifications',
    children: [
      { label: 'Boite de reception', route: '/notifications/inbox' },
      { label: 'Envoyees', route: '/notifications/sent' },
      { label: 'Parametres', route: '/notifications/settings' },
      { label: 'Modeles', route: '/notifications/templates' },
    ],
  },
  {
    icon: 'Building2',
    label: 'Entreprises',
    children: [
      { label: 'Liste', route: '/companies' },
      { label: 'Nouvelle', route: '/companies/new' },
    ],
  },
  {
    icon: 'Users',
    label: 'Ressources Humaines',
    children: [
      { label: 'Employes', route: '/hr/employees' },
      { label: 'Contrats', route: '/hr/contracts' },
      { label: 'Conges', route: '/hr/leaves' },
      { label: 'Paie', route: '/hr/payroll' },
    ],
  },
  {
    icon: 'Calculator',
    label: 'Comptabilite',
    children: [
      { label: 'Plan comptable', route: '/accounting/chart-of-accounts' },
      { label: 'Ecritures', route: '/accounting/journal-entries' },
      { label: 'Grand livre', route: '/accounting/general-ledger' },
      { label: 'Balance', route: '/accounting/trial-balance' },
      { label: 'Bilan', route: '/accounting/balance-sheet' },
      { label: 'Compte de resultat', route: '/accounting/income-statement' },
    ],
  },
  {
    icon: 'FileText',
    label: 'Documents',
    children: [
      { label: 'Tous les documents', route: '/documents' },
      { label: 'Factures', route: '/documents/invoices' },
      { label: 'Devis', route: '/documents/quotes' },
      { label: 'Contrats', route: '/documents/contracts' },
    ],
  },
  {
    icon: 'Shield',
    label: 'Permissions',
    children: [
      { label: 'Utilisateurs', route: '/permissions/users' },
      { label: 'Roles', route: '/permissions/roles' },
      { label: 'Groupes', route: '/permissions/groups' },
    ],
  },
  {
    icon: 'Settings',
    label: 'Parametres',
    route: '/settings',
  },
]
