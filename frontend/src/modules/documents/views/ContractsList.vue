<script setup lang="ts">
import { computed } from 'vue'
import { useDocuments } from '../composables/useDocuments'
import { Button } from '@/components/ui/button'
import {
  FileSignature,
  Plus,
  Search,
  Eye,
  Download,
  Calendar,
  RefreshCw,
  AlertTriangle,
  Loader2,
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'
import type { DocumentResponse, TagResponse } from '@/api/documents'

const { documents, isLoading, formatDate, getStatusColor, openUploadModal, selectDocument } =
  useDocuments()

// Filter documents by contract category
const contracts = computed(() => {
  const docs = documents.value as DocumentResponse[] | undefined
  if (!docs) return []
  return docs.filter(
    (doc) =>
      doc.categoryName?.toLowerCase().includes('contrat') ||
      doc.categoryName?.toLowerCase().includes('contract') ||
      doc.tags?.some((tag: TagResponse) => tag.name?.toLowerCase().includes('contrat'))
  )
})

const formatCurrency = (value: number, currency = 'EUR'): string => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency,
  }).format(value)
}

const renewalLabels: Record<string, string> = {
  none: 'Sans renouvellement',
  auto: 'Renouvellement automatique',
  manual: 'Renouvellement manuel',
}

const daysUntilEnd = (endDate?: string) => {
  if (!endDate) return null
  const diff = new Date(endDate).getTime() - new Date().getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
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
          <FileSignature class="h-6 w-6" />
          Contrats
        </h1>
        <p class="text-muted-foreground">Gerez vos contrats</p>
      </div>
      <Button @click="openUploadModal">
        <Plus class="h-4 w-4 mr-2" />
        Nouveau contrat
      </Button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <input
        type="text"
        placeholder="Rechercher un contrat..."
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
      v-else-if="contracts.length === 0"
      class="text-center py-12"
    >
      <FileSignature class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun contrat</p>
      <p class="text-muted-foreground mb-4">Ajoutez votre premier contrat</p>
      <Button @click="openUploadModal">
        <Plus class="h-4 w-4 mr-2" />
        Ajouter un contrat
      </Button>
    </div>

    <!-- Contracts List -->
    <div
      v-else
      class="space-y-3"
    >
      <div
        v-for="doc in contracts"
        :key="doc.id"
        class="rounded-xl border bg-card p-5 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start gap-4">
          <!-- Icon -->
          <div
            class="flex h-12 w-12 items-center justify-center rounded-lg bg-purple-100 dark:bg-purple-900/30"
          >
            <FileSignature class="h-6 w-6 text-purple-600 dark:text-purple-400" />
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-semibold">{{ doc.title }}</h3>
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(doc.isPublic))">
                {{ doc.isPublic ? 'Public' : 'Actif' }}
              </span>
              <span
                v-if="
                  doc.metadata?.endDate &&
                  daysUntilEnd(doc.metadata.endDate)! <= 30 &&
                  daysUntilEnd(doc.metadata.endDate)! > 0
                "
                class="text-xs px-2 py-1 rounded-full bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400"
              >
                <AlertTriangle class="h-3 w-3 inline mr-1" />
                Expire bientot
              </span>
            </div>

            <p class="text-sm font-medium">{{ doc.metadata?.partyName || doc.fileName }}</p>

            <div class="flex flex-wrap items-center gap-4 mt-2 text-sm text-muted-foreground">
              <span
                v-if="doc.metadata?.startDate"
                class="flex items-center gap-1"
              >
                <Calendar class="h-4 w-4" />
                {{ formatDate(doc.metadata.startDate) }}
                <template v-if="doc.metadata?.endDate">
                  - {{ formatDate(doc.metadata.endDate) }}
                </template>
              </span>
              <span
                v-else
                class="flex items-center gap-1"
              >
                <Calendar class="h-4 w-4" />
                {{ formatDate(doc.createdAt) }}
              </span>
              <span
                v-if="doc.metadata?.renewalType"
                class="flex items-center gap-1"
              >
                <RefreshCw class="h-4 w-4" />
                {{ renewalLabels[doc.metadata.renewalType] || doc.metadata.renewalType }}
              </span>
            </div>
          </div>

          <!-- Value -->
          <div
            v-if="doc.metadata?.value"
            class="text-right"
          >
            <p class="text-lg font-bold">{{ formatCurrency(parseFloat(doc.metadata.value)) }}</p>
            <p class="text-xs text-muted-foreground">Valeur du contrat</p>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1">
            <Button
              variant="ghost"
              size="icon-sm"
              title="Voir"
              @click="selectDocument(doc.id!)"
            >
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
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
