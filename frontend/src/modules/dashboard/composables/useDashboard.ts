import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '../stores/dashboardStore'

export function useDashboard() {
  const store = useDashboardStore()
  const { stats, recentActivities, monthlyRevenue, isLoading, netIncome, profitMargin } =
    storeToRefs(store)

  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
    }).format(value)
  }

  const formatRelativeTime = (date: Date): string => {
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 1000 / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (minutes < 1) return "A l'instant"
    if (minutes < 60) return `Il y a ${minutes} min`
    if (hours < 24) return `Il y a ${hours}h`
    return `Il y a ${days}j`
  }

  const refresh = async () => {
    await Promise.all([store.fetchStats(), store.fetchRecentActivities()])
  }

  onMounted(() => {
    refresh()
  })

  return {
    stats,
    recentActivities,
    monthlyRevenue,
    isLoading,
    netIncome,
    profitMargin,
    formatCurrency,
    formatRelativeTime,
    refresh,
  }
}
