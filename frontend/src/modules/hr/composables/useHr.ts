import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useHrStore } from '../stores/hrStore'

export function useHr() {
  const store = useHrStore()
  const {
    employees,
    contracts,
    leaveRequests,
    payrollEntries,
    isLoading,
    activeEmployees,
    pendingLeaves,
    totalPayroll,
    employeesByDepartment,
  } = storeToRefs(store)

  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
    }).format(value)
  }

  const formatDate = (date: Date): string => {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(date)
  }

  const getStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      active: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      inactive: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      onLeave: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
      terminated: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      pending: 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/30',
      approved: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
      rejected: 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/30',
      cancelled: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      draft: 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/30',
      validated: 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/30',
      paid: 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/30',
    }
    return colors[status] ?? colors.inactive!
  }

  const getStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      active: 'Actif',
      inactive: 'Inactif',
      onLeave: 'En conge',
      terminated: 'Termine',
      pending: 'En attente',
      approved: 'Approuve',
      rejected: 'Refuse',
      cancelled: 'Annule',
      draft: 'Brouillon',
      validated: 'Valide',
      paid: 'Paye',
    }
    return labels[status] ?? status
  }

  const getContractTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      cdi: 'CDI',
      cdd: 'CDD',
      internship: 'Stage',
      freelance: 'Freelance',
      apprenticeship: 'Apprentissage',
    }
    return labels[type] ?? type
  }

  const getLeaveTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      paid: 'Conge paye',
      sick: 'Maladie',
      parental: 'Parental',
      unpaid: 'Sans solde',
      other: 'Autre',
    }
    return labels[type] ?? type
  }

  const getInitials = (name: string): string => {
    const parts = name.split(' ')
    if (parts.length >= 2 && parts[0] && parts[1]) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase()
    }
    return name.slice(0, 2).toUpperCase()
  }

  onMounted(() => {
    store.fetchEmployees()
    store.fetchLeaveRequests()
  })

  return {
    employees,
    contracts,
    leaveRequests,
    payrollEntries,
    isLoading,
    activeEmployees,
    pendingLeaves,
    totalPayroll,
    employeesByDepartment,
    formatCurrency,
    formatDate,
    getStatusColor,
    getStatusLabel,
    getContractTypeLabel,
    getLeaveTypeLabel,
    getInitials,
    approveLeave: store.approveLeave,
    rejectLeave: store.rejectLeave,
  }
}
