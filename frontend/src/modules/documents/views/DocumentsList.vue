<script setup lang="ts">
import { useDocuments } from '../composables/useDocuments'
import { Button } from '@/components/ui/button'
import {
  FileText,
  Plus,
  Search,
  Filter,
  Download,
  Eye,
  Trash2,
  Building2,
  Calendar
} from 'lucide-vue-next'
import { cn } from '@/lib/utils'

const {
  allDocuments,
  isLoading,
  formatDate,
  formatFileSize,
  getStatusColor
} = useDocuments()

const statusLabels: Record<string, string> = {
  draft: 'Brouillon',
  pending: 'En attente',
  approved: 'Approuve',
  rejected: 'Rejete',
  archived: 'Archive'
}

const typeLabels: Record<string, string> = {
  invoice: 'Facture',
  quote: 'Devis',
  contract: 'Contrat',
  report: 'Rapport',
  other: 'Autre'
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold flex items-center gap-2">
          <FileText class="h-6 w-6" />
          Documents
        </h1>
        <p class="text-muted-foreground">
          Gerez tous vos documents
        </p>
      </div>
      <Button>
        <Plus class="h-4 w-4 mr-2" />
        Nouveau document
      </Button>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Rechercher un document..."
          class="w-full pl-10 pr-4 py-2 rounded-lg border bg-background focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <Button variant="outline" size="icon">
        <Filter class="h-4 w-4" />
      </Button>
    </div>

    <!-- Documents List -->
    <div v-if="isLoading" class="text-center py-12 text-muted-foreground">
      Chargement...
    </div>

    <div v-else-if="allDocuments.length === 0" class="text-center py-12">
      <FileText class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-lg font-medium">Aucun document</p>
      <p class="text-muted-foreground">Ajoutez votre premier document</p>
    </div>

    <div v-else class="rounded-xl border bg-card overflow-hidden">
      <table class="w-full">
        <thead class="bg-muted/50">
          <tr>
            <th class="text-left p-4 font-medium">Document</th>
            <th class="text-left p-4 font-medium">Type</th>
            <th class="text-left p-4 font-medium">Statut</th>
            <th class="text-left p-4 font-medium">Taille</th>
            <th class="text-left p-4 font-medium">Date</th>
            <th class="text-right p-4 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr
            v-for="doc in allDocuments"
            :key="doc.id"
            class="hover:bg-muted/30 transition-colors"
          >
            <td class="p-4">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <FileText class="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p class="font-medium">{{ doc.name }}</p>
                  <div class="flex items-center gap-2 text-xs text-muted-foreground">
                    <span v-if="doc.companyName" class="flex items-center gap-1">
                      <Building2 class="h-3 w-3" />
                      {{ doc.companyName }}
                    </span>
                  </div>
                </div>
              </div>
            </td>
            <td class="p-4">
              <span class="text-sm">{{ typeLabels[doc.type] }}</span>
            </td>
            <td class="p-4">
              <span :class="cn('text-xs px-2 py-1 rounded-full', getStatusColor(doc.status))">
                {{ statusLabels[doc.status] }}
              </span>
            </td>
            <td class="p-4 text-sm text-muted-foreground">
              {{ formatFileSize(doc.fileSize) }}
            </td>
            <td class="p-4">
              <div class="flex items-center gap-1 text-sm text-muted-foreground">
                <Calendar class="h-4 w-4" />
                {{ formatDate(doc.createdAt) }}
              </div>
            </td>
            <td class="p-4">
              <div class="flex items-center justify-end gap-1">
                <Button variant="ghost" size="icon-sm" title="Voir">
                  <Eye class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon-sm" title="Telecharger">
                  <Download class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="icon-sm" title="Supprimer" class="text-destructive hover:text-destructive">
                  <Trash2 class="h-4 w-4" />
                </Button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
