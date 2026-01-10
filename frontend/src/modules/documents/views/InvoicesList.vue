<script setup lang="ts">
import { useDocuments } from '../composables/useDocuments'
import { Button } from '@/components/ui/button'
import {
  Receipt,
  Plus,
  Search,
  Filter,
  Download,
  Eye,
  Send,
  MoreHorizontal,
  AlertCircle,
  CheckCircle
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  invoices,
  isLoading,
  totalInvoiced,
  totalPending,
  formatCurrency,
  formatDate,
  getStatusColor
} = useDocuments()

const statusLabels: Record<string, string> = {
  draft: 'Brouillon',
  pending: 'En attente',
  approved: 'Payee',
  rejected: 'Annulee',
  archived: 'Archivee'
}

const isOverdue = (dueDate: Date, paidAt?: Date) => {
  if (paidAt) return false
  return new Date() > new Date(dueDate)
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <Receipt class="h-6 w-6" />
          Factures
        </h1>
        <p class="text-muted-foreground">
          Gerez vos factures clients
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle facture
      </Button>
    </div>

    <!-- Stats -->
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-xl border bg-card p-4">
        <p class="text-sm text-muted-foreground">Total facture</p>
        <p class="text-2xl font-bold">{{ formatCurrency(totalInvoiced) }}</p>
      </div>
      <div class="rounded-xl border bg-card p-4">
        <p class="text-sm text-muted-foreground">En attente</p>
        <p class="text-2xl font-bold text-yellow-600">{{ formatCurrency(totalPending) }}</p>
      </div>
      <div class="rounded-xl border bg-card p-4">
        <p class="text-sm text-muted-foreground">Nombre de factures</p>
        <p class="text-2xl font-bold">{{ invoices.length }}</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher une facture..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button variant="outline" size="icon">
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Invoices List -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="invoice in invoices"
        :key="invoice.id"
        class="rounded-xl border bg-card p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-center gap-4">
          <!-- Icon -->
          <div :class="cn(
            'flex h-12 w-12 items-center justify-center rounded-lg',
            invoice.paidAt ? 'bg-green-100 dark:bg-green-900/30' : 'bg-yellow-100 dark:bg-yellow-900/30'
          )">
            <CheckCircle v-if="invoice.paidAt" class="h-6 w-6 text-green-600 dark:text-green-400" />
            <AlertCircle v-else-if="isOverdue(invoice.dueDate)" class="h-6 w-6 text-red-600 dark:text-red-400" />
            <Receipt v-else class="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold">{{ invoice.invoiceNumber }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(invoice.status))">
                {{ statusLabels[invoice.status] }}
              </span>
              <span
                v-if="isOverdue(invoice.dueDate, invoice.paidAt)"
                class="text-xs px-2 py-1 rounded-full bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400"
              >
                En retard
              </span>
            </div>
            <p class="text-sm text-muted-foreground">{{ invoice.clientName }}</p>
            <div class="flex items-center gap-4 mt-1 text-xs text-muted-foreground">
              <span>Emise le {{ formatDate(invoice.createdAt) }}</span>
              <span>Echeance: {{ formatDate(invoice.dueDate) }}</span>
              <span v-if="invoice.paidAt">Payee le {{ formatDate(invoice.paidAt) }}</span>
            </div>
          </div>

          <!-- Amount -->
          <div class="text-right">
            <p class="text-lg font-bold">{{ formatCurrency(invoice.totalAmount) }}</p>
            <p class="text-xs text-muted-foreground">
              HT: {{ formatCurrency(invoice.amount) }}
            </p>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1">
            <Button variant="ghost" size="icon-sm" title="Voir">
              <Eye class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Telecharger">
              <Download class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Envoyer">
              <Send class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm">
              <MoreHorizontal class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
