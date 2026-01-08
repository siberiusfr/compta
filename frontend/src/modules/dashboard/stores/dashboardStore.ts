import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mockDashboardStats, mockRecentActivities, mockRevenueData } from '../mock-data/dashboard.mock'
import type { DashboardStats, RecentActivity, RevenueData } from '../types/dashboard.types'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref<DashboardStats>(mockDashboardStats)
  const recentActivities = ref<RecentActivity[]>(mockRecentActivities)
  const revenueData = ref<RevenueData[]>(mockRevenueData)

  const totalExpenses = computed(() => 
    revenueData.value.reduce((sum, item) => sum + item.expenses, 0)
  )

  const netProfit = computed(() => 
    revenueData.value.reduce((sum, item) => sum + item.revenue - item.expenses, 0)
  )

  function addActivity(activity: RecentActivity) {
    recentActivities.value.unshift(activity)
  }

  function updateStats(newStats: Partial<DashboardStats>) {
    stats.value = { ...stats.value, ...newStats }
  }

  return {
    stats,
    recentActivities,
    revenueData,
    totalExpenses,
    netProfit,
    addActivity,
    updateStats
  }
})
