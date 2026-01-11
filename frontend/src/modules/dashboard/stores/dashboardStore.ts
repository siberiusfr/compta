import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { DashboardStats, RecentActivity } from '../types/dashboard.types'
import {
  mockDashboardStats,
  mockRecentActivities,
  mockMonthlyRevenue,
} from '../mock-data/dashboard.mock'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref<DashboardStats>(mockDashboardStats)
  const recentActivities = ref<RecentActivity[]>(mockRecentActivities)
  const monthlyRevenue = ref(mockMonthlyRevenue)
  const isLoading = ref(false)

  const netIncome = computed(() => stats.value.revenue - stats.value.expenses)

  const profitMargin = computed(() => {
    if (stats.value.revenue === 0) return 0
    return ((netIncome.value / stats.value.revenue) * 100).toFixed(1)
  })

  async function fetchStats() {
    isLoading.value = true
    try {
      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 500))
      stats.value = mockDashboardStats
    } finally {
      isLoading.value = false
    }
  }

  async function fetchRecentActivities() {
    isLoading.value = true
    try {
      await new Promise((resolve) => setTimeout(resolve, 300))
      recentActivities.value = mockRecentActivities
    } finally {
      isLoading.value = false
    }
  }

  return {
    stats,
    recentActivities,
    monthlyRevenue,
    isLoading,
    netIncome,
    profitMargin,
    fetchStats,
    fetchRecentActivities,
  }
})
