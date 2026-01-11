<script setup lang="ts">
import { computed } from 'vue'
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
  CheckCircle,
  Loader2,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'
import type { DocumentResponse, TagResponse } from '@/api/documents'

const { documents, isLoading, formatDate, getStatusColor, openUploadModal, selectDocument } =
  useDocuments()

// Filter documents by quote category
const quotes = computed(() => {
  const docs = documents.value as DocumentResponse[] | undefined
  if (!docs) return []
  return docs.filter(
    (doc) =>
      doc.categoryName?.toLowerCase().includes('devis') ||
      doc.categoryName?.toLowerCase().includes('quote') ||
      doc.tags?.some((tag: TagResponse) => tag.name?.toLowerCase().includes('devis'))
  )
})

const formatCurrency = (value: number, currency = 'EUR'): string => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency,
  }).format(value)
}

const isExpired = (doc: DocumentResponse) => {
  if (!doc.metadata?.validUntil) return false
  return new Date() > new Date(doc.metadata.validUntil)
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
          <FileText class="h-6 w-6" />
          Devis
        </h1>
        <p class="text-muted-foreground">Gerez vos devis clients</p>
      </div>
      <Button @click="openUploadModal">
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

    <!-- Loading State -->
    <div
      v-if="isLoading"
      class="flex items-center justify-center py-12 text-muted-foreground"
    >
      <Loader2 class="h-6 w-6 animate-spin mr-2" />
      Chargement...
    </div>

    <!-- Empty State -->
    <div
      v-else-if="quotes.length === 0"
      class="text-center py-12"
    >
      <FileText class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun devis</p>
      <p class="text-muted-foreground mb-4">Ajoutez votre premier devis</p>
      <Button @click="openUploadModal">
        <Plus class="h-4 w-4 mr-2" />
        Ajouter un devis
      </Button>
    </div>

    <!-- Quotes Grid -->
    <div
      v-else
      class="grid gap-4 md:grid-cols-2 lg:grid-cols-3"
    >
      <div
        v-for="doc in quotes"
        :key="doc.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-4">
          <div>
            <h3 class="font-semibold">{{ doc.title }}</h3>
            <p class="text-sm text-muted-foreground">
              {{ doc.metadata?.clientName || doc.fileName }}
            </p>
          </div>
          <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(doc.isPublic))">
            {{
              doc.metadata?.convertedToInvoice ? 'Converti' : isExpired(doc) ? 'Expire' : 'En cours'
            }}
          </span>
        </div>

        <!-- Amount -->
        <div
          v-if="doc.metadata?.amount"
          class="mb-4"
        >
          <p class="text-2xl font-bold">{{ formatCurrency(parseFloat(doc.metadata.amount)) }}</p>
        </div>

        <!-- Meta -->
        <div class="space-y-2 text-sm">
          <div
            v-if="doc.metadata?.validUntil"
            class="flex items-center gap-2 text-muted-foreground"
          >
            <Clock class="h-4 w-4" />
            <span>Valide jusqu'au {{ formatDate(doc.metadata.validUntil) }}</span>
          </div>
          <div
            v-if="doc.metadata?.convertedToInvoice"
            class="flex items-center gap-2 text-green-600"
          >
            <CheckCircle class="h-4 w-4" />
            <span>Converti en {{ doc.metadata.convertedToInvoice }}</span>
          </div>
          <div
            v-else-if="isExpired(doc)"
            class="flex items-center gap-2 text-red-600"
          >
            <Clock class="h-4 w-4" />
            <span>Expire</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-2 mt-4 pt-4 border-t">
          <Button
            variant="ghost"
            size="sm"
            @click="selectDocument(doc.id!)"
          >
            <Eye class="h-4 w-4 mr-1" />
            Voir
          </Button>
          <Button
            variant="ghost"
            size="sm"
            :disabled="!doc.downloadUrl"
            @click="handleDownload(doc)"
          >
            <Download class="h-4 w-4 mr-1" />
            PDF
          </Button>
          <Button
            v-if="!doc.metadata?.convertedToInvoice && !isExpired(doc)"
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
