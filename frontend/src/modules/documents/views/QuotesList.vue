<script setup lang="ts">
import { useDocuments } from '../composables/useDocuments'
import { Button } from '@/components/ui/button'
import {
  FileText,
  Plus,
  Search,
  Eye,
  Download,
  ArrowRight,
  Clock,
  CheckCircle
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const { quotes, isLoading, formatCurrency, formatDate, getStatusColor } = useDocuments()

const statusLabels: Record<string, string> = {
  draft: 'Brouillon',
  pending: 'En attente',
  approved: 'Accepte',
  rejected: 'Refuse',
  archived: 'Archive'
}

const isExpired = (validUntil: Date) => new Date() > new Date(validUntil)
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FileText class="h-6 w-6" />
          Devis
        </h1>
        <p class="text-muted-foreground">
          Gerez vos devis clients
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau devis
      </Button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        type="text"
        placeholder="Rechercher un devis..."
        class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
      />
    </div>

    <!-- Quotes Grid -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="quote in quotes"
        :key="quote.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-4">
          <div>
            <h3 class="font-semibold">{{ quote.quoteNumber }}</h3>
            <p class="text-sm text-muted-foreground">{{ quote.clientName }}</p>
          </div>
          <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(quote.status))">
            {{ statusLabels[quote.status] }}
          </span>
        </div>

        <!-- Amount -->
        <div class="mb-4">
          <p class="text-2xl font-bold">{{ formatCurrency(quote.amount) }}</p>
        </div>

        <!-- Meta -->
        <div class="space-y-2 text-sm">
          <div class="flex items-center gap-2 text-muted-foreground">
            <Clock class="h-4 w-4" />
            <span>Valide jusqu'au {{ formatDate(quote.validUntil) }}</span>
          </div>
          <div v-if="quote.convertedToInvoice" class="flex items-center gap-2 text-green-600">
            <CheckCircle class="h-4 w-4" />
            <span>Converti en {{ quote.convertedToInvoice }}</span>
          </div>
          <div v-else-if="isExpired(quote.validUntil)" class="flex items-center gap-2 text-red-600">
            <Clock class="h-4 w-4" />
            <span>Expire</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-2 mt-4 pt-4 border-t">
          <Button variant="ghost" size="sm">
            <Eye class="h-4 w-4 mr-1" />
            Voir
          </Button>
          <Button variant="ghost" size="sm">
            <Download class="h-4 w-4 mr-1" />
            PDF
          </Button>
          <Button
            v-if="quote.status === 'approved' && !quote.convertedToInvoice"
            variant="outline"
            size="sm"
            class="ml-auto"
          >
            Convertir
            <ArrowRight class="h-4 w-4 ml-1" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
