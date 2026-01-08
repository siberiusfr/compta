<script setup lang="ts">
import { computed } from 'vue'
import { useDashboard } from '../composables/useDashboard'
import { formatCurrency } from '@/shared/utils/format'
import {
  TrendingUp,
  TrendingDown,
  Building2,
  Users,
  FileText,
  DollarSign,
  Activity,
  ArrowUpRight
} from 'lucide-vue-next'

const {
  stats,
  recentActivities,
  revenueData,
  totalExpenses,
  netProfit
} = useDashboard()

const recentActivitiesFormatted = computed(() => {
  if (!recentActivities.value || !Array.isArray(recentActivities.value)) {
    return []
  }
  return recentActivities.value.slice(0, 5).map((activity) => ({
    ...activity,
    timeAgo: getTimeAgo(activity.timestamp)
  }))
})

function getTimeAgo(date: Date): string {
  const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000)
  
  if (seconds < 60) return 'à l\'instant'
  if (seconds < 3600) return `il y a ${Math.floor(seconds / 60)} min`
  if (seconds < 86400) return `il y a ${Math.floor(seconds / 3600)} h`
  return `il y a ${Math.floor(seconds / 86400)} j`
}
</script>

<template>
  <div v-if="!stats || !revenueData || !recentActivities" class="flex items-center justify-center h-64">
    <div class="text-center">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent mx-auto mb-4"></div>
      <p class="text-gray-600">Chargement des données...</p>
    </div>
  </div>

  <div v-else class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Dashboard</h1>
      <p class="text-gray-600 mt-1">Vue d'ensemble de votre activité</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 font-medium">Entreprises</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.totalCompanies }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Building2 class="text-blue-600" :size="24" />
          </div>
        </div>
        <div class="flex items-center gap-1 mt-4 text-sm">
          <TrendingUp class="text-green-500" :size="16" />
          <span class="text-green-500 font-medium">+8%</span>
          <span class="text-gray-500">vs mois dernier</span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 font-medium">Employés actifs</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.activeEmployees }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <Users class="text-purple-600" :size="24" />
          </div>
        </div>
        <div class="flex items-center gap-1 mt-4 text-sm">
          <TrendingUp class="text-green-500" :size="16" />
          <span class="text-green-500 font-medium">+12%</span>
          <span class="text-gray-500">vs mois dernier</span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 font-medium">Factures en attente</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.pendingInvoices }}</p>
          </div>
          <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <FileText class="text-yellow-600" :size="24" />
          </div>
        </div>
        <div class="flex items-center gap-1 mt-4 text-sm">
          <TrendingDown class="text-red-500" :size="16" />
          <span class="text-red-500 font-medium">-5%</span>
          <span class="text-gray-500">vs mois dernier</span>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500 font-medium">Chiffre d'affaires</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ formatCurrency(stats.totalRevenue) }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <DollarSign class="text-green-600" :size="24" />
          </div>
        </div>
        <div class="flex items-center gap-1 mt-4 text-sm">
          <TrendingUp class="text-green-500" :size="16" />
          <span class="text-green-500 font-medium">+15%</span>
          <span class="text-gray-500">vs mois dernier</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold text-gray-900">Revenus et Dépenses</h2>
          <Activity class="text-gray-400" :size="20" />
        </div>
        <div class="space-y-4">
          <div v-for="item in revenueData" :key="item.month" class="space-y-2">
            <div class="flex justify-between text-sm">
              <span class="font-medium text-gray-700">{{ item.month }}</span>
              <div class="flex gap-4">
                <span class="text-green-600">{{ formatCurrency(item.revenue) }}</span>
                <span class="text-red-600">{{ formatCurrency(item.expenses) }}</span>
              </div>
            </div>
            <div class="flex gap-1 h-2">
              <div
                class="bg-green-500 rounded-full"
                :style="{
                  width: `${(item.revenue / 150000) * 100}%`
                }"
              ></div>
              <div
                class="bg-red-500 rounded-full"
                :style="{
                  width: `${(item.expenses / 150000) * 100}%`
                }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold text-gray-900">Activité récente</h2>
          <ArrowUpRight class="text-gray-400" :size="20" />
        </div>
        <div class="space-y-4">
          <div
            v-for="activity in recentActivitiesFormatted"
            :key="activity.id"
            class="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div
              :class="[
                'w-8 h-8 rounded-full flex items-center justify-center',
                activity.type === 'invoice' ? 'bg-blue-100 text-blue-600' :
                activity.type === 'employee' ? 'bg-purple-100 text-purple-600' :
                activity.type === 'payment' ? 'bg-green-100 text-green-600' :
                'bg-yellow-100 text-yellow-600'
              ]"
            >
              <FileText v-if="activity.type === 'invoice'" :size="16" />
              <Users v-else-if="activity.type === 'employee'" :size="16" />
              <DollarSign v-else-if="activity.type === 'payment'" :size="16" />
              <Activity v-else :size="16" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">
                {{ activity.message }}
              </p>
              <p class="text-xs text-gray-500 mt-0.5">
                {{ activity.user }} · {{ activity.timeAgo }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-gradient-to-br from-green-500 to-green-600 rounded-xl shadow-sm p-6 text-white">
        <p class="text-sm opacity-90">Profit net</p>
        <p class="text-3xl font-bold mt-2">{{ formatCurrency(netProfit) }}</p>
        <p class="text-sm opacity-75 mt-2">+18% vs période précédente</p>
      </div>

      <div class="bg-gradient-to-br from-red-500 to-red-600 rounded-xl shadow-sm p-6 text-white">
        <p class="text-sm opacity-90">Total dépenses</p>
        <p class="text-3xl font-bold mt-2">{{ formatCurrency(totalExpenses) }}</p>
        <p class="text-sm opacity-75 mt-2">-3% vs période précédente</p>
      </div>

      <div class="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-sm p-6 text-white">
        <p class="text-sm opacity-90">Taux de conversion</p>
        <p class="text-3xl font-bold mt-2">67%</p>
        <p class="text-sm opacity-75 mt-2">+5% vs période précédente</p>
      </div>
    </div>
  </div>
</template>
