<script setup lang="ts">
import { useInvoices } from '../composables/useInvoices'
import { Button } from '@/components/ui/button'
import {
  FileText,
  Plus,
  Search,
  Filter,
  Eye,
  Edit,
  Trash2,
  Calendar,
  CheckCircle2,
  Clock,
  AlertCircle,
  XCircle,
  FilePenLine,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'
import { useRouter } from 'vue-router'

const { invoiceSummaries, isLoading, formatCurrency, formatDate, getInvoiceStatusColor } =
  useInvoices()

const router = useRouter()

const statusIcons = {
  draft: FilePenLine,
  sent: Clock,
  paid: CheckCircle2,
  overdue: AlertCircle,
  cancelled: XCircle,
}

function handleViewInvoice(id: string) {
  router.push({ name: 'invoices-detail', params: { id } })
}

function handleEditInvoice(id: string) {
  router.push({ name: 'invoices-edit', params: { id } })
}

function handleCreateInvoice() {
  router.push({ name: 'invoices-create' })
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FileText class="h-6 w-6" />
          Factures
        </h1>
        <p class="text-muted-foreground">Gestion des factures clients</p>
      </div>
      <Button @click="handleCreateInvoice">
        <Plus class="h-4 w-4 mr-2" />
        Nouvelle facture
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button
        variant="outline"
        size="icon"
      >
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Invoices List -->
    <div
      v-if="isLoading"
      class="text-center py-12 text-muted-foreground"
    >
      Chargement...
    </div>

    <div
      v-else
      class="space-y-3"
    >
      <div
        v-for="invoice in invoiceSummaries"
        :key="invoice.id"
        class="rounded-xl border bg-card p-4 hover:bg-accent/50 transition-colors"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 flex-1">
            <div :class="cn('p-2 rounded-lg', getInvoiceStatusColor(invoice.status))">
              <component
                :is="statusIcons[invoice.status]"
                class="h-5 w-5"
              />
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3">
                <span class="font-mono font-medium text-sm">{{ invoice.invoiceNumber }}</span>
                <span class="text-sm text-muted-foreground">{{ invoice.customerName }}</span>
              </div>
              <div class="flex items-center gap-4 mt-1 text-xs text-muted-foreground">
                <span class="flex items-center gap-1">
                  <Calendar class="h-3 w-3" />
                  {{ formatDate(invoice.date) }}
                </span>
                <span v-if="invoice.amountDue > 0"> Due: {{ formatDate(invoice.dueDate) }} </span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-6">
            <div class="text-right">
              <div class="font-semibold">
                {{ formatCurrency(invoice.total) }}
              </div>
              <div
                v-if="invoice.amountDue > 0"
                class="text-xs text-muted-foreground"
              >
                À payer: {{ formatCurrency(invoice.amountDue) }}
              </div>
            </div>
            <div class="flex items-center gap-1">
              <Button
                variant="ghost"
                size="icon-sm"
                @click="handleViewInvoice(invoice.id)"
              >
                <Eye class="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon-sm"
                @click="handleEditInvoice(invoice.id)"
              >
                <Edit class="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon-sm"
                class="text-destructive hover:text-destructive"
              >
                <Trash2 class="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="!isLoading && invoiceSummaries.length === 0"
      class="text-center py-12 text-muted-foreground"
    >
      Aucune facture trouvée
    </div>
  </div>
</template>
