<script setup lang="ts">
import { computed } from 'vue'
import { useDocuments } from '../composables/useDocuments'
import { Button } from '@/components/ui/button'
import {
  Receipt,
  Plus,
  Search,
  Filter,
  Download,
  Eye,
  Share2,
  MoreHorizontal,
  Loader2
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'
import type { DocumentResponse, TagResponse } from "@/api/documents"

const {
  documents,
  isLoading,
  formatDate,
  getStatusColor,
  openUploadModal,
  selectDocument,
} = useDocuments()

// Filter documents by invoice category (assuming category name contains 'facture' or 'invoice')
const invoices = computed(() => {
  const docs = documents.value as DocumentResponse[] | undefined
  if (!docs) return []
  return docs.filter(doc =>
    doc.categoryName?.toLowerCase().includes('facture') ||
    doc.categoryName?.toLowerCase().includes('invoice') ||
    doc.tags?.some((tag: TagResponse) => tag.name?.toLowerCase().includes('facture'))
  )
})

// Stats from metadata if available
const totalInvoiced = computed(() => {
  return invoices.value.reduce((sum, doc) => {
    const amount = doc.metadata?.totalAmount ? parseFloat(doc.metadata.totalAmount) : 0
    return sum + amount
  }, 0)
})

const pendingInvoices = computed(() => {
  return invoices.value.filter(doc =>
    doc.metadata?.status === 'pending' || !doc.metadata?.paidAt
  )
})

const totalPending = computed(() => {
  return pendingInvoices.value.reduce((sum, doc) => {
    const amount = doc.metadata?.totalAmount ? parseFloat(doc.metadata.totalAmount) : 0
    return sum + amount
  }, 0)
})

const formatCurrency = (value: number, currency = 'EUR'): string => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency
  }).format(value)
}

const isOverdue = (doc: DocumentResponse) => {
  if (doc.metadata?.paidAt) return false
  if (!doc.metadata?.dueDate) return false
  return new Date() > new Date(doc.metadata.dueDate)
}

async function handleDownload(doc: DocumentResponse) {
  if (doc.downloadUrl) {
    window.open(doc.downloadUrl, '_blank')
  }
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
      <Button @click="openUploadModal">
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

    <!-- Loading State -->
    <div v-if="isLoading" class="flex items-center justify-center py-12 text-muted-foreground">
      <Loader2 class="h-6 w-6 animate-spin mr-2" />
      Chargement...
    </div>

    <!-- Empty State -->
    <div v-else-if="invoices.length === 0" class="text-center py-12">
      <Receipt class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucune facture</p>
      <p class="text-muted-foreground mb-4">
        Ajoutez votre premiere facture
      </p>
      <Button @click="openUploadModal">
        <Plus class="h-4 w-4 mr-2" />
        Ajouter une facture
      </Button>
    </div>

    <!-- Invoices List -->
    <div v-else class="space-y-3">
      <div
        v-for="doc in invoices"
        :key="doc.id"
        class="rounded-xl border bg-card p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex items-center gap-4">
          <!-- Icon -->
          <div :class="cn(
            'flex h-12 w-12 items-center justify-center rounded-lg',
            doc.metadata?.paidAt ? 'bg-green-100 dark:bg-green-900/30' : 'bg-yellow-100 dark:bg-yellow-900/30'
          )">
            <Receipt :class="cn(
              'h-6 w-6',
              doc.metadata?.paidAt ? 'text-green-600 dark:text-green-400' : 'text-yellow-600 dark:text-yellow-400'
            )" />
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold">{{ doc.title }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(doc.isPublic))">
                {{ doc.metadata?.paidAt ? 'Payee' : 'En attente' }}
              </span>
              <span
                v-if="isOverdue(doc)"
                class="text-xs px-2 py-1 rounded-full bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400"
              >
                En retard
              </span>
            </div>
            <p class="text-sm text-muted-foreground">{{ doc.metadata?.clientName || doc.fileName }}</p>
            <div class="flex items-center gap-4 mt-1 text-xs text-muted-foreground">
              <span>Emise le {{ formatDate(doc.createdAt) }}</span>
              <span v-if="doc.metadata?.dueDate">Echeance: {{ formatDate(doc.metadata.dueDate) }}</span>
              <span v-if="doc.metadata?.paidAt">Payee le {{ formatDate(doc.metadata.paidAt) }}</span>
            </div>
          </div>

          <!-- Amount -->
          <div v-if="doc.metadata?.totalAmount" class="text-right">
            <p class="text-lg font-bold">{{ formatCurrency(parseFloat(doc.metadata.totalAmount)) }}</p>
            <p v-if="doc.metadata?.amount" class="text-xs text-muted-foreground">
              HT: {{ formatCurrency(parseFloat(doc.metadata.amount)) }}
            </p>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1">
            <Button variant="ghost" size="icon-sm" title="Voir" @click="selectDocument(doc.id!)">
              <Eye class="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon-sm"
              title="Telecharger"
              :disabled="!doc.downloadUrl"
              @click="handleDownload(doc)"
            >
              <Download class="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon-sm" title="Partager">
              <Share2 class="h-4 w-4" />
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
