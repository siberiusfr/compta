<script setup lang="ts">
import { useDashboard } from '../composables/useDashboard'
import { mockQuickActions } from '../mock-data/dashboard.mock'
import { Button } from '@/components/ui/button'
import {
  Building2,
  Users,
  FileText,
  Receipt,
  TrendingUp,
  TrendingDown,
  RefreshCw,
  Plus,
  UserPlus,
  ArrowRight
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  stats,
  recentActivities,
  isLoading,
  netIncome,
  profitMargin,
  formatCurrency,
  formatRelativeTime,
  refresh
} = useDashboard()

const iconMap: Record<string, any> = {
  Plus,
  UserPlus,
  Building2,
  FileText
}

const activityTypeStyles: Record<string, string> = {
  invoice: 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400',
  document: 'bg-purple-100 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400',
  employee: 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400',
  company: 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400',
  payment: 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400'
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">Tableau de bord</h1>
        <p class="text-muted-foreground">Vue d'ensemble de votre activite</p>
      </div>
      <Button variant="outline" size="sm" @click="refresh" :disabled="isLoading">
        <RefreshCw :class="cn('h-4 w-4 mr-2', isLoading && 'animate-spin')" />
        Actualiser
      </Button>
    </div>

    <!-- Stats Grid -->
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-900/30">
            <Building2 class="h-6 w-6 text-blue-600 dark:text-blue-400" />
          </div>
          <span class="text-xs text-muted-foreground">+2 ce mois</span>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold">{{ stats.totalCompanies }}</p>
          <p class="text-sm text-muted-foreground">Entreprises</p>
        </div>
      </div>

      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-green-100 dark:bg-green-900/30">
            <Users class="h-6 w-6 text-green-600 dark:text-green-400" />
          </div>
          <span class="text-xs text-muted-foreground">+12 ce mois</span>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold">{{ stats.totalEmployees }}</p>
          <p class="text-sm text-muted-foreground">Employes</p>
        </div>
      </div>

      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-purple-100 dark:bg-purple-900/30">
            <FileText class="h-6 w-6 text-purple-600 dark:text-purple-400" />
          </div>
          <span class="text-xs text-muted-foreground">+47 ce mois</span>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold">{{ stats.totalDocuments }}</p>
          <p class="text-sm text-muted-foreground">Documents</p>
        </div>
      </div>

      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-orange-100 dark:bg-orange-900/30">
            <Receipt class="h-6 w-6 text-orange-600 dark:text-orange-400" />
          </div>
          <span class="text-xs text-red-500">{{ stats.pendingInvoices }} en attente</span>
        </div>
        <div class="mt-4">
          <p class="text-2xl font-bold">{{ formatCurrency(stats.revenue) }}</p>
          <p class="text-sm text-muted-foreground">Chiffre d'affaires</p>
        </div>
      </div>
    </div>

    <!-- Financial Overview -->
    <div class="grid gap-4 lg:grid-cols-3">
      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-green-100 dark:bg-green-900/30">
            <TrendingUp class="h-5 w-5 text-green-600 dark:text-green-400" />
          </div>
          <div>
            <p class="text-sm text-muted-foreground">Revenus</p>
            <p class="text-xl font-bold text-green-600 dark:text-green-400">
              {{ formatCurrency(stats.revenue) }}
            </p>
          </div>
        </div>
      </div>

      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-red-100 dark:bg-red-900/30">
            <TrendingDown class="h-5 w-5 text-red-600 dark:text-red-400" />
          </div>
          <div>
            <p class="text-sm text-muted-foreground">Depenses</p>
            <p class="text-xl font-bold text-red-600 dark:text-red-400">
              {{ formatCurrency(stats.expenses) }}
            </p>
          </div>
        </div>
      </div>

      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-900/30">
            <TrendingUp class="h-5 w-5 text-blue-600 dark:text-blue-400" />
          </div>
          <div>
            <p class="text-sm text-muted-foreground">Benefice net ({{ profitMargin }}%)</p>
            <p class="text-xl font-bold text-blue-600 dark:text-blue-400">
              {{ formatCurrency(netIncome) }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions & Recent Activity -->
    <div class="grid gap-6 lg:grid-cols-2">
      <!-- Quick Actions -->
      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <h2 class="text-lg font-semibold mb-4">Actions rapides</h2>
        <div class="grid grid-cols-2 gap-3">
          <RouterLink
            v-for="action in mockQuickActions"
            :key="action.label"
            :to="action.route"
            class="flex items-center gap-3 rounded-lg border p-4 transition-colors hover:bg-accent"
          >
            <div :class="cn('flex h-10 w-10 items-center justify-center rounded-lg text-white', action.color)">
              <component :is="iconMap[action.icon] || Plus" class="h-5 w-5" />
            </div>
            <span class="text-sm font-medium">{{ action.label }}</span>
          </RouterLink>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="rounded-xl border bg-card p-6 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold">Activite recente</h2>
          <Button variant="ghost" size="sm">
            Voir tout
            <ArrowRight class="h-4 w-4 ml-1" />
          </Button>
        </div>
        <div class="space-y-4">
          <div
            v-for="activity in recentActivities.slice(0, 5)"
            :key="activity.id"
            class="flex items-start gap-3"
          >
            <div :class="cn('flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium', activityTypeStyles[activity.type])">
              {{ activity.type.charAt(0).toUpperCase() }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium truncate">{{ activity.title }}</p>
              <p class="text-xs text-muted-foreground truncate">{{ activity.description }}</p>
            </div>
            <span class="text-xs text-muted-foreground whitespace-nowrap">
              {{ formatRelativeTime(activity.timestamp) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
